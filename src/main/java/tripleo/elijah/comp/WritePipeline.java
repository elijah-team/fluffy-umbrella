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
import tripleo.elijah.stages.gen_c.CDependencyRef;
import tripleo.elijah.stages.gen_c.OutputFileC;
import tripleo.elijah.stages.gen_generic.*;
import tripleo.elijah.stages.generate.ElSystem;
import tripleo.elijah.stages.generate.OutputStrategy;
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
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 8/21/21 10:19 PM
 */
public class WritePipeline implements PipelineMember, @NotNull Consumer<Supplier<GenerateResult>> {
	private final CompletedItemsHandler               cih;
	private final WritePipelineSharedState            st;
	private final Promise<GenerateResult, Void, Void> prom;
	private       Supplier<GenerateResult>            grs;

	public WritePipeline(final @NotNull Compilation aCompilation,
						 final @NotNull ProcessRecord aPr,
						 final @NotNull Promise<PipelineLogic, Void, Void> ppl) {
		st = new WritePipelineSharedState();

		// given
		st.c = aCompilation;
		//st.setGr(aGr);

		// computed
		st.file_prefix = new File("COMP", st.c.getCompilationNumberString());

		// created
/*
		// TODO should we be doing this? see below comment
		st.os = new OutputStrategy();
		st.os.per(OutputStrategy.Per.PER_CLASS); // TODO this needs to be configured per lsp
*/

		// state
		st.mmb         = ArrayListMultimap.create();
		st.lsp_outputs = ArrayListMultimap.create();

		// ??
		st.sys = new ElSystem(false, st.c, this::createOutputStratgy);
/*
		st.sys.verbose = false; // TODO flag? ie CompilationOptions
		st.sys.setCompilation(st.c);
		st.sys.setOutputStrategy(st.os);
*/
/*
		st.sys.generateOutputs(gr);
*/

		ppl.then((aPipelineLogic) -> {
			st.setGr(aPipelineLogic.gr);

			//NotImplementedException.raise();
		});

		cih = new CompletedItemsHandler(st);

		//		pr.consumeGenerateResult(wpl.consumer());
		prom = aPr.generateResultPromise();
		prom.then(gr1 -> {
			NotImplementedException.raise();
			;
			Objects.requireNonNull(gr1);

			// FIXME also setGr here ...

			gr1.subscribeCompletedItems(cih.observer());
		});
		//st.gr.subscribeCompletedItems(cih.observer());

		//new BlackHoleChannel().write("Stupidity and Bullshit");
	}

	OutputStrategy createOutputStratgy() {
		final OutputStrategy os = new OutputStrategy();
		os.per(OutputStrategy.Per.PER_CLASS); // TODO this needs to be configured per lsp

		return os;
	}


	@Override
	public void run() throws Exception {
/*
		latch = new Guard();

		final Guard[] guards = {latch, hasGr};

		int which = new Alternative(guards)
				.select();
*/

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
		s.add(new WPIS_WriteInputs());
		s.add(new WPIS_WriteFiles());
		s.add(new WPIS_WriteBuffers());


		final WP_Flow f = new WP_Flow(s);
		try {
			f.act();
		} catch (Exception aE) {
			throw new RuntimeException(aE);
		}
	}

	class WPIS_WriteBuffers implements WP_Indiviual_Step {
		@Override
		public void act(final WritePipelineSharedState st, final WP_State_Control sc) {
			// 5. write buffers
			// TODO flag?
			try {
				st.file_prefix.mkdirs();

				debug_buffers();
			} catch (FileNotFoundException aE) {
				sc.exception(aE);
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

						final List<GenerateResultItem> generateResultItems = result.results();

						for (final GenerateResultItem ab : generateResultItems) {
							db_stream.println("---------------------------------------------------------------");
							db_stream.println(ab.counter);
							db_stream.println(ab.ty);
							db_stream.println(ab.output);
							db_stream.println(ab.node.identityString());
							db_stream.println(ab.buffer.getText());
							db_stream.println("---------------------------------------------------------------");
						}
					} catch (FileNotFoundException aE) {
						throw new RuntimeException(aE);
					} finally {
						if (db_stream != null)
							db_stream.close();
					}
				}
			});
		}
	}

	class WPIS_WriteFiles implements WP_Indiviual_Step {
		@Override
		public void act(final WritePipelineSharedState st, final WP_State_Control sc) {
			// 4. write files
			Multimap<String, Buffer> mb = ArrayListMultimap.create();

			final List<GenerateResultItem> generateResultItems = st.getGr().results();

			prom.then(new DoneCallback<GenerateResult>() {
				@Override
				public void onDone(final GenerateResult result) {
	/*
					for (final GenerateResultItem ab : generateResultItems) {
						mb.put(((CDependencyRef) ab.getDependency().getRef()).getHeaderFile(), ab.buffer); // TODO see above
					}

					assert st.mmb.equals(mb);
	*/

					try {
						final String prefix = st.file_prefix.toString();

						for (final Map.Entry<String, Collection<Buffer>> entry : mb.asMap().entrySet()) {
							final String                       key = entry.getKey();
							final Supplier<Collection<Buffer>> e   = entry::getValue;

							write_files_helper_each(prefix, key, e);
						}
					} catch (Exception aE) {
						throw new RuntimeException(aE);
					}
				}

				@Contract("_, null, _ -> fail")
				private void write_files_helper_each(final String prefix,
													 final String key,
													 final @NotNull Supplier<Collection<Buffer>> e) throws Exception {
					assert key != null;

					final Path path = FileSystems.getDefault().getPath(prefix, key);
					// final BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

					path.getParent().toFile().mkdirs();

					// TODO functionality
					System.out.println("201 Writing path: " + path);
					try (final DisposableCharSink fileCharSink = st.c.getIO().openWrite(path)) {
						for (final Buffer buffer : e.get()) {
							fileCharSink.accept(buffer.getText());
						}
					}
				}
			});

/*
			for (final GenerateResultItem ab : generateResultItems) {
				mb.put(((CDependencyRef) ab.getDependency().getRef()).getHeaderFile(), ab.buffer); // TODO see above
			}

			assert st.mmb.equals(mb);

			write_files_helper(mb);
*/
		}
	}

	class WPIS_WriteInputs implements WP_Indiviual_Step {
		@Override
		public void act(final WritePipelineSharedState st, final WP_State_Control sc) {
			// 3. write inputs
			// TODO ... 1/ output(s) per input and 2/ exceptions ... and 3/ plan
			//  "plan", effects; input(s), output(s)
			// TODO flag?
			try {
				final String fn1 = new File(WritePipeline.this.st.file_prefix, "inputs.txt").toString();

				DefaultBuffer buf = new DefaultBuffer("");
//			FileBackedBuffer buf = new FileBackedBuffer(fn1);
//			for (OS_Module module : modules) {
//				final String fn = module.getFileName();
//
//				append_hash(buf, fn);
//			}
//
//			for (CompilerInstructions ci : cis) {
//				final String fn = ci.getFilename();
//
//				append_hash(buf, fn);
//			}
				for (File file : WritePipeline.this.st.c.getIO().recordedreads) {
					final String fn = file.toString();

					append_hash(buf, fn, WritePipeline.this.st.c.getErrSink());
				}
				String s = buf.getText();
				Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fn1, true)));
				w.write(s);
				w.close();
			} catch (IOException aE) {
				throw new RuntimeException(aE);
			}
		}
	}

	class WPIS_MakeOutputDirectory implements WP_Indiviual_Step {

		@Override
		public void act(final WritePipelineSharedState st, final WP_State_Control sc) {
			// 2. make output directory
			// TODO check first
			boolean made = st.file_prefix.mkdirs();
		}
	}

	class WPIS_GenerateOutputs implements WP_Indiviual_Step {

		private final GenerateResult result;

		WPIS_GenerateOutputs(final GenerateResult aResult) {
			// 1. GenerateOutputs with ElSystem
			result = aResult;
		}

		@Override
		public void act(final WritePipelineSharedState st, final WP_State_Control sc) {


			result.results().stream()
					.forEach(gri -> {
						System.err.println(gri.ty);
						System.err.println("" + gri.buffer.getText());
					});


			st.sys.generateOutputs(result);
		}
	}


	interface WP_State_Control {
		void exception(final Exception e);

		void clear();

		boolean hasException();

		Exception getException();
	}

	class WP_State_Control_1 implements WP_State_Control {
		private Exception e;

		@Override
		public void exception(final Exception ee) {
			e = ee;
		}

		@Override
		public void clear() {
			e = null;
		}

		@Override
		public boolean hasException() {
			return e != null;
		}

		// TODO DiagnosticException
		@Override
		public Exception getException() {
			return e;
		}
	}

	interface WP_Indiviual_Step {
		void act(final WritePipelineSharedState st, final WP_State_Control sc);
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

//	@Override
//	public void accept(final Supplier<GenerateResult> aGenerateResultSupplier) {
//		grs = aGenerateResultSupplier;
//	}

	//public class AltingBarrierGadget0 implements CSProcess {
	//	private final AltingChannelInput click;
	//	private final AltingBarrier      group;
	//	private final ChannelOutput      configure;
	//
	//	public AltingBarrierGadget0(
	//			AltingChannelInput click, AltingBarrier group, ChannelOutput configure
	//							   ) {
	//		this.click     = click;
	//		this.group     = group;
	//		this.configure = configure;
	//	}
	//
	//	@Override
	//	public void run() {
	//
	//		final Alternative clickGroup =
	//				new Alternative(new Guard[]{click, group});
	//
	//		final int CLICK = 0, GROUP = 1;
	//
	//		int n = 0;
	//		configure.write(String.valueOf(n));
	//
	//		while (true) {
	//
	//			configure.write(Color.green);                // pretty
	//
	//			while (!click.pending()) {                  // individual work mode
	//				n++;                                       // work on our own
	//				configure.write(String.valueOf(n));      // work on our own
	//			}
	//			click.read();                               // must consume the click
	//
	//			configure.write(Color.red);                 // pretty
	//
	//			boolean group = true;                        // group work mode
	//			while (group) {
	//				switch (clickGroup.priSelect()) {         // offer to work with the group
	//				case CLICK:
	//					click.read();                         // must consume the click
	//					group = false;                         // back to individual work mode
	//					break;
	//				case GROUP:
	//					n--;                                   // work with the group
	//					configure.write(String.valueOf(n));  // work with the group
	//					break;
	//				}
	//			}
	//
	//		}
	//
	//	}
	//
	//}

	/*
	private void write_files_helper(@NotNull Multimap<String, Buffer> mb) throws IOException {
		String prefix = st.file_prefix.toString();

		for (Map.Entry<String, Collection<Buffer>> entry : mb.asMap().entrySet()) {
			final String key = entry.getKey();
			assert key != null;
			Path path = FileSystems.getDefault().getPath(prefix, key);
//			BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

			path.getParent().toFile().mkdirs();

			// TODO functionality
			System.out.println("201 Writing path: " + path);
			CharSink x = st.c.getIO().openWrite(path);
			for (Buffer buffer : entry.getValue()) {
				x.accept(buffer.getText());
			}
			((FileCharSink) x).close();
		}
	}
*/

	static class HashBufferList extends DefaultBuffer {
		public HashBufferList(final String string) {
			super(string);
		}
	}


	/*
	 * intent: HashBuffer
	 *  - contains 3 sub-buffers: hash, space, and filename
	 *  - has all logic to update and present hash
	 *    - codec: MTL sha2 here
	 *    - encoding: reg or multihash (hint hint...)
	 */
	static class HashBuffer extends DefaultBuffer {
		private final HashBufferList parent;

		public HashBuffer(final String string) {
			super(string);

			parent = null;
		}

		public HashBuffer(final String aFileName, final HashBufferList aHashBufferList, final Executor aExecutor, final ErrSink errSink) {
			super("");

			String[] y = new String[1];
			DoubleLatch<String> dl = new DoubleLatch<>(aFilename -> {
				y[0] = aFilename;

				final HashBuffer outputBuffer = this;

				@Nullable final String hh;
				try {
					hh = Helpers.getHashForFilename(aFilename, errSink);
				} catch (IOException aE) {
					throw new RuntimeException(aE);
				}

				if (hh != null) {
					outputBuffer.append(hh);
					outputBuffer.append(" ");
					outputBuffer.append_ln(aFilename);
				}
			});

			dl.notify(aFileName);

			parent = aHashBufferList;
			//parent.setNext(this);
		}
	}

	private void append_hash(TextBuffer outputBuffer, String aFilename, ErrSink errSink) throws IOException {
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
		int y=2;
	}

	public Consumer<Supplier<GenerateResult>> consumer() {
		return new Consumer<Supplier<GenerateResult>>() {
			@Override
			public void accept(final Supplier<GenerateResult> aGenerateResultSupplier) {
				grs = aGenerateResultSupplier;
				//final GenerateResult gr = aGenerateResultSupplier.get();
			}
		};
	}

	/**
	 * Really a record, but state is not all set at once
	 */
	private final static class WritePipelineSharedState {
		//private @NotNull /*final*/ OutputStrategy os;
		private @NotNull /*final*/ ElSystem                               sys;
		private @NotNull /*final*/ Multimap<CompilerInstructions, String> lsp_outputs;
		private /*final*/ @NotNull Compilation                            c;
		private /*final*/ @NotNull GenerateResult                         gr;
		private /*final*/ @NotNull File                                   file_prefix;
		private /*final*/ @NotNull Multimap<String, Buffer>               mmb;

		@Contract(pure = true)
		public @NotNull GenerateResult getGr() {
			return gr;
		}

		@Contract(mutates = "this")
		public void setGr(final @NotNull GenerateResult aGr) {
			gr = aGr;
		}
	}

	private static class CompletedItemsHandler {
		//private final Compilation c;
		//private final Multimap<String, Buffer> mmb;
		//private final Multimap<CompilerInstructions, String> lsp_outputs;
		//private final GenerateResult gr;
		//private final File file_prefix;

		// region state
		final         Multimap<Dependency, GenerateResultItem> gris = ArrayListMultimap.create();
		// README debugging purposes
		final         List<GenerateResultItem>                 abs  = new ArrayList<>();
		private final WritePipelineSharedState                 sharedState;
		private       Observer<GenerateResultItem>             observer;

		public CompletedItemsHandler(final WritePipelineSharedState aSharedState) {
			sharedState = aSharedState;
		}

		// endregion state

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

		//@Contract(pure = true)
		//public void completeSequence(int y) {
		//	NotImplementedException.raise();
		//}

		private void ___completeSequence(final @NotNull Map<String, OutputFileC> outputFiles) {
			final String prefix = sharedState.file_prefix.toString();

			NotImplementedException.raise();

			for (final Map.Entry<String, OutputFileC> entry : outputFiles.entrySet()) {
				final String key = entry.getKey();
				assert key != null;

				final Path path = FileSystems.getDefault().getPath(prefix, key);

				boolean made = path.getParent().toFile().mkdirs();

				// TODO functionality
				System.out.println("201a Writing path: " + path);
				try (DisposableCharSink x = sharedState.c.getIO().openWrite(path)) {
					x.accept(entry.getValue().getOutput());

					//((FileCharSink) x).close();
					//x.dispose(); // README close automatically because of try-with-resources
				} catch (Exception aE) {
					sharedState.c.getErrSink().exception(aE);
				}
			}
		}

		public void completeSequence() {
			final @NotNull GenerateResult generateResult = sharedState.getGr();

			generateResult.outputFiles((final Map<String, OutputFileC> outputFiles) -> {
				___completeSequence(outputFiles);
			});
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
	}
}

//
//
//
