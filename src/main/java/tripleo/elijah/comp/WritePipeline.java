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
import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.stages.gen_c.CDependencyRef;
import tripleo.elijah.stages.gen_c.OutputFileC;
import tripleo.elijah.stages.gen_generic.*;
import tripleo.elijah.stages.generate.ElSystem;
import tripleo.elijah.stages.generate.OutputStrategy;
import tripleo.elijah.stages.write_stage.functionality.f201a.WriteOutputFiles;
import tripleo.elijah.stages.write_stage.pipeline_impl.*;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;
import tripleo.util.buffer.Buffer;
import tripleo.util.buffer.DefaultBuffer;
import tripleo.util.buffer.TextBuffer;
import tripleo.util.io.DisposableCharSink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 8/21/21 10:19 PM
 */
public class WritePipeline implements PipelineMember, @NotNull Consumer<Supplier<GenerateResult>> {
	private final CompletedItemsHandler               cih;
	public final  WritePipelineSharedState            st;
	public final  Promise<GenerateResult, Void, Void> prom;
	private final DoubleLatch<GenerateResult>         latch;
	private       Supplier<GenerateResult>            grs;

	public WritePipeline(final IPipelineAccess pa) {
	//public WritePipeline(final @NotNull Compilation aCompilation,
	//					 ,
	//					  ppl) {
		st = new WritePipelineSharedState();

		// given
		st.c = pa.getCompilation();



		final @NotNull ProcessRecord pr = pa.getProcessRecord();
		final @NotNull Promise<PipelineLogic, Void, Void> ppl = pa.getPipelineLogicPromise();




		// computed
		st.file_prefix = new File("COMP", st.c.getCompilationNumberString());

		// created
		latch = new DoubleLatch<GenerateResult>(gr -> st.setGr(gr));

		// state
		st.mmb         = ArrayListMultimap.create();
		st.lsp_outputs = ArrayListMultimap.create();

		// ??
		st.sys = new ElSystem(false, st.c, this::createOutputStratgy);

		/*
		ppl.then((aPipelineLogic) -> {
			latch.notify(aPipelineLogic.gr); // TODO doesn't seem right. Might work, but not right
		});
		*/
		
		cih = new CompletedItemsHandler(st);

		//		pr.consumeGenerateResult(wpl.consumer());
		prom = pr.generateResultPromise();
		prom.then(gr1 -> {
			latch.notify(gr1); // TODO doesn't seem right. Might work, but not right

			NotImplementedException.raise();
			;
			Objects.requireNonNull(gr1);

			// FIXME also setGr here ...

			gr1.subscribeCompletedItems(cih.observer());
		});
	}

	OutputStrategy createOutputStratgy() {
		final OutputStrategy os = new OutputStrategy();
		os.per(OutputStrategy.Per.PER_CLASS); // TODO this needs to be configured per lsp

		return os;
	}


	@Override
	public void run() throws Exception {
		final GenerateResult rs = grs.get();

		prom.then((final GenerateResult result) -> {
			__int__steps(result, rs);
		});
	}

	private void __int__steps(final GenerateResult result, final GenerateResult rs) {
		@NotNull final List<WP_Indiviual_Step> s = new ArrayList<>();

		// 0. prepare to change to DoubleLatch instead of/an or in addition to Promise
		assert result == rs;

		s.add(new WPIS_GenerateOutputs(result));
		s.add(new WPIS_MakeOutputDirectory());
		s.add(new WPIS_WriteInputs(this));
		s.add(new WPIS_WriteFiles(this));
		s.add(new WPIS_WriteBuffers(this));


		final WP_Flow f = new WP_Flow(s);
		try {
			f.act();
		} catch (Exception aE) {
			throw new RuntimeException(aE);
		}
	}


		private void debug_buffers() throws FileNotFoundException {
			// TODO can/should this fail??

			final List<GenerateResultItem> generateResultItems1 = st.getGr().results();

			prom.then(new DoneCallback<GenerateResult>() {
				@Override
				public void onDone(final GenerateResult result) {
					PrintStream db_stream = null;

					try {
						final File file = new File(st.file_prefix, "buffers.txt");
						db_stream = new PrintStream(file);
						XXPrintStream xps = new XXPrintStream(db_stream);

						debug_buffers_logic(result, xps);
					} catch (FileNotFoundException aE) {
						throw new RuntimeException(aE);
					} finally {
						if (db_stream != null)
							db_stream.close();
					}
				}


			});
		}

	public static void debug_buffers_logic(final GenerateResult result, final XPrintStream db_stream) {
		final List<GenerateResultItem> generateResultItems = result.results();
		debug_buffers_logic(generateResultItems, db_stream);
	}

	static void debug_buffers_logic(final List<GenerateResultItem> generateResultItems,
									final XPrintStream db_stream) {
		for (final GenerateResultItem ab : generateResultItems) {
			final String s = MessageFormat.format("{0} - {1} - {2}", ab.counter, ab.ty, ab.output);

			db_stream.println("---------------------------------------------------------------");
			db_stream.println(s);
			db_stream.println(ab.node.identityString());
			db_stream.println(ab.buffer.getText());
			db_stream.println("---------------------------------------------------------------");
		}
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
					//final GenerateResult gr = aGenerateResultSupplier.get();
				}
			};
		}

		return (x) -> { grs = x; };
	}

	class WP_Flow {
		private final List<WP_Indiviual_Step> steps = new ArrayList<>();

		WP_Flow(final Collection<? extends WP_Indiviual_Step> s) {
			steps.addAll(s);
		}

		WP_Flow() {
			//steps.addAll(s);
		}

		void act() throws Exception {
			final WP_State_Control_1 sc = new WP_State_Control_1();

			for (WP_Indiviual_Step step : steps) {
				sc.clear();

				step.act(st, sc);

				if (sc.hasException()) {
					throw sc.getException();
				}
			}
		}
	}

	private /*static*/ class CompletedItemsHandler {
		final         Multimap<Dependency, GenerateResultItem> gris = ArrayListMultimap.create();
		// README debugging purposes
		final         List<GenerateResultItem>                 abs  = new ArrayList<>();
		private final WritePipelineSharedState                 sharedState;
		private       Observer<GenerateResultItem>             observer;

		public CompletedItemsHandler(final WritePipelineSharedState aSharedState) {
			sharedState = aSharedState;
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

		private void ___completeSequence(final @NotNull Map<String, OutputFileC> outputFiles) {
			final WriteOutputFiles wof = new WriteOutputFiles();
			wof.writeOutputFiles(sharedState, outputFiles);
		}

		public void completeSequence() {
			final @NotNull GenerateResult generateResult = sharedState.getGr();

			generateResult.outputFiles((final Map<String, OutputFileC> outputFiles) -> {
				___completeSequence(outputFiles);
			});
		}
	}
}

//
//
//
