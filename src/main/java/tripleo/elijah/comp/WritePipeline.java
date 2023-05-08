/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.AccessBus.AB_GenerateResultListener;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.stages.gen_c.CDependencyRef;
import tripleo.elijah.stages.gen_c.OutputFileC;
import tripleo.elijah.stages.gen_generic.*;
import tripleo.elijah.stages.generate.ElSystem;
import tripleo.elijah.stages.generate.OutputStrategy;
import tripleo.elijah.stages.write_stage.functionality.f201a.WriteOutputFiles;
import tripleo.elijah.stages.write_stage.pipeline_impl.WPIS_GenerateOutputs;
import tripleo.elijah.stages.write_stage.pipeline_impl.WPIS_MakeOutputDirectory;
import tripleo.elijah.stages.write_stage.pipeline_impl.WPIS_WriteBuffers;
import tripleo.elijah.stages.write_stage.pipeline_impl.WPIS_WriteFiles;
import tripleo.elijah.stages.write_stage.pipeline_impl.WPIS_WriteInputs;
import tripleo.elijah.stages.write_stage.pipeline_impl.WP_Indiviual_Step;
import tripleo.elijah.stages.write_stage.pipeline_impl.WP_State_Control_1;
import tripleo.elijah.stages.write_stage.pipeline_impl.WritePipelineSharedState;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;
import tripleo.util.buffer.TextBuffer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 8/21/21 10:19 PM
 */
public class WritePipeline implements PipelineMember, @NotNull Consumer<Supplier<GenerateResult>>, AB_GenerateResultListener {
	public final  WritePipelineSharedState                   st;
	public final  DeferredObject<GenerateResult, Void, Void> prom = new DeferredObject<>();
	private final CompletedItemsHandler                      cih;
	private final DoubleLatch<GenerateResult>                latch;
	private       Supplier<GenerateResult>                   grs;


	public WritePipeline(final @NotNull IPipelineAccess pa) {
		st = new WritePipelineSharedState();

		// given
		st.c = pa.getCompilation();

		final @NotNull ProcessRecord                      pr  = pa.getProcessRecord();
		final @NotNull Promise<PipelineLogic, Void, Void> ppl = pa.getPipelineLogicPromise();

		// computed
		st.file_prefix = new File("COMP", st.c.getCompilationNumberString());

		// created
		latch = new DoubleLatch<GenerateResult>(gr -> {
			st.setGr(gr);

			// prepare to change to DoubleLatch instead of/an or in addition to Promise
			assert gr == gr;

			final WP_Indiviual_Step wpis_go = new WPIS_GenerateOutputs();
			final WP_Indiviual_Step wpis_mk = new WPIS_MakeOutputDirectory();
			final WP_Indiviual_Step wpis_wi = new WPIS_WriteInputs(this);
			final WP_Indiviual_Step wpis_wf = new WPIS_WriteFiles(this);
			final WP_Indiviual_Step wpis_wb = new WPIS_WriteBuffers(this);

			Operation<Boolean> op;

			// TODO: Do something with op, like set in {@code pa} to proceed to next pipeline
			try {
				final WP_Flow f = new WP_Flow(List_of(wpis_go, wpis_mk, wpis_wi, wpis_wf, wpis_wb));
				// TODO WP_FlowMember?
				// TODO each IndividualStep may return an op?
				//  - with type or Boolean?
				//  - are we modeing effects here?
				f.act();
				op = Operation.success(true);
			} catch (Exception aE) {
				op = Operation.failure(aE);
			}
		});

		// state
		st.mmb         = ArrayListMultimap.create();
		st.lsp_outputs = ArrayListMultimap.create();

		// ??
		st.sys = new ElSystem(false, st.c, this::createOutputStratgy);

		cih = new CompletedItemsHandler(st);

		pa.getAccessBus().subscribe_GenerateResult(this::gr_slot);
		pa.getAccessBus().subscribe_GenerateResult(prom::resolve);

		pa.setWritePipeline(this);
	}

	OutputStrategy createOutputStratgy() {
		final OutputStrategy os = new OutputStrategy();
		os.per(OutputStrategy.Per.PER_CLASS); // TODO this needs to be configured per lsp

		return os;
	}

	@Override
	public void gr_slot(final @NotNull GenerateResult gr1) {
		Objects.requireNonNull(gr1);
		latch.notify(gr1);
		gr1.subscribeCompletedItems(cih.observer());
	}

	@Override
	public void run() throws Exception {
		// final GenerateResult rs = grs.get(); // 04/15

//		prom.then((final GenerateResult result) -> {
		latch.notify(true);
//		});
	}

	public void append_hash(TextBuffer outputBuffer, String aFilename, ErrSink errSink) throws IOException {
		@Nullable final String hh = Helpers.getHashForFilename(aFilename, errSink);
		if (hh != null) {
			outputBuffer.append(hh);
			outputBuffer.append(" ");
			outputBuffer.append_ln(aFilename);
		}
	}

	@Override
	public void accept(final Supplier<GenerateResult> aGenerateResultSupplier) {
		final GenerateResult gr = aGenerateResultSupplier.get();
		grs = aGenerateResultSupplier;
		int y = 2;
	}

	public Consumer<Supplier<GenerateResult>> consumer() {
		if (false) {
			return new Consumer<Supplier<GenerateResult>>() {
				@Override
				public void accept(final Supplier<GenerateResult> aGenerateResultSupplier) {
					grs = aGenerateResultSupplier;
					// final GenerateResult gr = aGenerateResultSupplier.get();
				}
			};
		}

		return (x) -> {
			grs = x;
		};
	}

	class WP_Flow {
		private final List<WP_Indiviual_Step> steps = new ArrayList<>();

		WP_Flow(final Collection<? extends WP_Indiviual_Step> s) {
			steps.addAll(s);
		}

		void act() throws Exception {
			final WP_State_Control_1 sc = new WP_State_Control_1();

			for (final WP_Indiviual_Step step : steps) {
				sc.clear();

				step.act(st, sc);

				if (sc.hasException()) {
					throw sc.getException();
				}
			}
		}
	}

	private /* static */ class CompletedItemsHandler {
		final         Multimap<Dependency, GenerateResultItem> gris = ArrayListMultimap.create();
		// README debugging purposes
		final         List<GenerateResultItem>                 abs  = new ArrayList<>();
		private final WritePipelineSharedState                 sharedState;
		private       Observer<GenerateResultItem>             observer;

		public CompletedItemsHandler(final WritePipelineSharedState aSharedState) {
			sharedState = aSharedState;
		}

		@Contract(mutates = "this")
		public Observer<GenerateResultItem> observer() {
			if (observer == null) {
				observer = new Observer<GenerateResultItem>() {
					@Override
					public void onSubscribe(@NonNull Disposable d) {
					}

					@Override
					public void onNext(@NonNull GenerateResultItem ab) {
						addItem(ab);
					}

					@Override
					public void onError(@NonNull Throwable e) {
					}

					@Override
					public void onComplete() {
						completeSequence();
					}
				};
			}

			return observer;
		}

		public void addItem(final @NotNull GenerateResultItem ab) {
			NotImplementedException.raise();

			// README debugging purposes
			abs.add(ab);

			final Dependency dependency = ab.getDependency();

			// README debugging purposes
			final DependencyRef dependencyRef = dependency.getRef();

			if (dependencyRef == null) {
				gris.put(dependency, ab);
			} else {
				final String output = ((CDependencyRef) dependency.getRef()).getHeaderFile();
				sharedState.mmb.put(output, ab.buffer);
				sharedState.lsp_outputs.put(ab.lsp.getInstructions(), output);
				for (GenerateResultItem generateResultItem : gris.get(dependency)) {
					final String output1 = generateResultItem.output;
					sharedState.mmb.put(output1, generateResultItem.buffer);
					sharedState.lsp_outputs.put(generateResultItem.lsp.getInstructions(), output1);
				}
				gris.removeAll(dependency);
			}
		}

		public void completeSequence() {
			final @NotNull GenerateResult generateResult = sharedState.getGr();

			generateResult.outputFiles((final Map<String, OutputFileC> outputFiles) -> {
				___completeSequence(outputFiles);
			});
		}

		private void ___completeSequence(final @NotNull Map<String, OutputFileC> outputFiles) {
			final WriteOutputFiles wof = new WriteOutputFiles();
			wof.writeOutputFiles(sharedState, outputFiles);
		}
	}

}

//
//
//
