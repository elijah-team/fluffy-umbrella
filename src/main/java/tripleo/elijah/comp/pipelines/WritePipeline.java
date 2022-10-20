/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp.pipelines;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.apache.commons.lang3.tuple.Pair;
import org.jdeferred2.Deferred;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.internal.CCA;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.elijah.stages.generate.ElSystem;
import tripleo.elijah.stages.generate.OutputStrategy;
import tripleo.elijah.util.Helpers;
import tripleo.util.buffer.Buffer;
import tripleo.util.buffer.DefaultBuffer;
import tripleo.util.buffer.TextBuffer;
import tripleo.util.io.CharSink;
import tripleo.util.io.FileCharSink;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created 8/21/21 10:19 PM
 */
public class WritePipeline implements PipelineMember {
	final OutputStrategy os;
	final ElSystem sys;

	private final File file_prefix;

	private final Deferred<GenerateResult, Void, Void> event_hasGr; //= new DeferredObject<>();
	private final IO io;
	private final ErrSink errSink;

	public WritePipeline(CCA cca1) {
		// TODO extra work: fix type
		event_hasGr = (Deferred<GenerateResult, Void, Void>) cca1.getPipelineLogic().gr_promise();

		event_hasGr.done(new DoneCallback<GenerateResult>() {
			@Override
			public void onDone(GenerateResult result) {
				GenerateResult gr = result;
//				throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
			}
		});

		final Compilation c = cca1.getComp();

		file_prefix = new File("COMP", c.getCompilationNumberString());

		os = new OutputStrategy();
		os.per(OutputStrategy.Per.PER_CLASS); // TODO this needs to be configured per lsp

		sys = new ElSystem();
		sys.verbose = false; // TODO flag? ie CompilationOptions
		sys.setCompilation(c);
		sys.setOutputStrategy(os);

		cca1.getPipelineAcceptor().accept(this);

		errSink = c.getErrSink();
		io = c.getIO();
	}

	enum RunStages {
		GENERATE_OUTPUTS,
		WRITE_FILES,
		WRITE_BUFFERS
	}

	static class RunResult {
		List<Pair<RunStages, Exception>> _l = new ArrayList<>();

		void addResult(RunStages rs, Exception exc) {
			_l.add(Pair.<RunStages, Exception>of(rs, exc)); // wierd as f...
		}
	}

	RunResult rr = new RunResult();

	@Override
	public void run() throws Exception {
		event_hasGr.done(new DoneCallback<GenerateResult>() {
			@Override
			public void onDone(final GenerateResult gr) {
				int flag = 0;

				sys.generateOutputs(gr);
				rr.addResult(RunStages.GENERATE_OUTPUTS, null);

				try {
					write_files();
					rr.addResult(RunStages.WRITE_FILES, null);
				} catch (IOException ex) {
					Logger.getLogger(WritePipeline.class.getName()).log(Level.SEVERE, null, ex);
					rr.addResult(RunStages.WRITE_FILES, ex);
					flag = 1;
				}

				if (flag == 0) {
					Exception exc = null;
					try {
						// TODO control latch?
						write_buffers();
						exc = null;
					} catch (FileNotFoundException ex) {
						Logger.getLogger(WritePipeline.class.getName()).log(Level.SEVERE, null, ex);
						exc = ex;
					}
					rr.addResult(RunStages.WRITE_BUFFERS, exc);
				}

				System.err.println(rr._l);
			}
		});
	}

	@Override
	public void attachCB(CompilationShit cs) {
		int y = 2;
	}

	private final Subject<List<GenerateResultItem>> rslgri = ReplaySubject.create(2);
//	<List<GenerateResultItem>>() {
//		
//	};

	public void write_files() throws IOException {
		Multimap<String, Buffer> mb = ArrayListMultimap.create();

		Deferred<List<GenerateResultItem>, Void, Void> pgril = new DeferredObject<>();
		event_hasGr.then((gr) -> {
			pgril.resolve(gr.results());
		});

		pgril.done(new DoneCallback<List<GenerateResultItem>>() {
			@Override
			public void onDone(List<GenerateResultItem> result) {
				rslgri.subscribe(new Observer<List<GenerateResultItem>>() {
					@Override
					public void onSubscribe(Disposable d) {
						throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
					}

					@Override
					public void onNext(List<GenerateResultItem> t) {
						for (final GenerateResultItem ab : t) {
							mb.put(ab.output, ab.buffer);
						}
					}

					@Override
					public void onError(Throwable e) {
						throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
					}

					@Override
					public void onComplete() {
						file_prefix.mkdirs();
						String prefix = file_prefix.toString();

						try {
							// TODO flag?
							write_inputs(file_prefix);
						} catch (IOException ex) {
							Logger.getLogger(WritePipeline.class.getName()).log(Level.SEVERE, null, ex);
							return;
//							throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
						}

						for (final Map.Entry<String, Collection<Buffer>> entry : mb.asMap().entrySet()) {
							final String key = entry.getKey();
							final Collection<Buffer> value = entry.getValue();
							
/*
							Maybe<FileCharSink> mfcs = Maybe.create(new MaybeOnSubscribe<FileCharSink>() {
								@Override
								public void subscribe(MaybeEmitter<FileCharSink> emitter) throws Throwable {
									emitter.onSuccess(t);
//									return new MaybeEmitter<FileCharSink>() {
//										@Override
//										public void onSuccess(FileCharSink t) {
//											throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//										}
//
//										@Override
//										public void onError(Throwable t) {
//											throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//										}
//
//										@Override
//										public void onComplete() {
//											throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//										}
//
//										@Override
//										public void setDisposable(Disposable d) {
//											throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//										}
//
//										@Override
//										public void setCancellable(Cancellable c) {
//											throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//										}
//
//										@Override
//										public boolean isDisposed() {
//											throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//										}
//
//										@Override
//										public boolean tryOnError(Throwable t) {
//											throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//										}
//									};
								}
							});
*/
							doIt(key, value, prefix);
						}
					}

					private void doIt(String key, Collection<Buffer> value, String prefix) {
						BufferedReader reader = null;
						try {
							Path path = FileSystems.getDefault().getPath(prefix, key);
							reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
							path.getParent().toFile().mkdirs();
							// TODO functionality
							System.out.println("201 Writing path: " + path);
							CharSink x;
							try {
								x = io.openWrite(path);
							} catch (IOException ex) {
								Logger.getLogger(WritePipeline.class.getName()).log(Level.SEVERE, null, ex);
								return;
							}
							for (Buffer buffer : value) {
								x.accept(buffer.getText());
							}
							((FileCharSink) x).close();
						} catch (IOException ex) {
							Logger.getLogger(WritePipeline.class.getName()).log(Level.SEVERE, null, ex);
						} finally {
							try {
								reader.close();
							} catch (IOException ex) {
								Logger.getLogger(WritePipeline.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					}

				});
//				throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
			}
		});

		event_hasGr.done(new DoneCallback<GenerateResult>() {
			@Override
			public void onDone(GenerateResult result) {
				for (final GenerateResultItem ab : result.results()) {
					mb.put(ab.output, ab.buffer);
				}
				//throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
			}
		});
	}

	private void write_inputs(File file_prefix) throws IOException {
		final String fn1 = new File(file_prefix, "inputs.txt").toString();

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
		for (final File file : io.recordedreads) {
			final String fn = file.toString();

			append_hash(buf, fn, errSink);
		}
		String s = buf.getText();
		Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fn1, true)));
		w.write(s);
		w.close();
	}

	private void append_hash(TextBuffer aBuf, String aFilename, ErrSink errSink) throws IOException {
		@Nullable final String hh = Helpers.getHashForFilename(aFilename, errSink);
		if (hh != null) {
			aBuf.append(hh);
			aBuf.append(" ");
			aBuf.append_ln(aFilename);
		}
	}

	public void write_buffers() throws FileNotFoundException {
		file_prefix.mkdirs();

		PrintStream db_stream = new PrintStream(new File(file_prefix, "buffers.txt"));
		//PipelineLogic.
		event_hasGr.done((gr) -> debug_buffers(gr, db_stream));
	}

	private static void debug_buffers(GenerateResult gr, PrintStream stream) {
		for (GenerateResultItem ab : gr.results()) {
			stream.println("---------------------------------------------------------------");
			stream.println(ab.counter);
			stream.println(ab.ty);
			stream.println(ab.output);
			stream.println(ab.node.identityString());
			stream.println(ab.buffer.getText());
			stream.println("---------------------------------------------------------------");
		}
	}
}

//
//
//
