package tripleo.elijah.comp;

import org.jetbrains.annotations.*;
import tripleo.elijah.ci.*;
import tripleo.elijah.comp.caches.*;
import tripleo.elijah.comp.diagnostic.*;
import tripleo.elijah.comp.i.*;
import tripleo.elijah.comp.internal.*;
import tripleo.elijah.comp.specs.*;
import tripleo.elijah.diagnostic.*;
import tripleo.elijah.nextgen.query.*;
import tripleo.elijah.stages.deduce.post_bytecode.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import static tripleo.elijah.util.Helpers.*;

public class CompilationRunner {
	private final Compilation                       compilation;
	private final Compilation.CIS                   cis;
	private final CCI                               cci;
	private final ICompilationBus                   cb;
	private final EzCache                           ezCache = new DefaultEzCache();

	@Contract(pure = true)
	public CompilationRunner(final Compilation aCompilation, final Compilation.CIS a_cis, final ICompilationBus aCb, final IProgressSink ps1) {
		compilation = aCompilation;
		cis         = a_cis;
		cci         = new CCI(compilation, a_cis, ps1);
		cb          = aCb;
//		ps = ps1;
	}

	@Contract(pure = true)
	public CompilationRunner(final Compilation aCompilation, final Compilation.CIS a_cis, final ICompilationBus aCb) {
		compilation = aCompilation;
		cis         = a_cis;
		final DefaultProgressSink ps1 = new DefaultProgressSink();
		cci = new CCI(compilation, a_cis, ps1);
		cb  = aCb;
	}

	void start(final CompilerInstructions ci, final boolean do_out) throws Exception {
		final CR_State st1 = new CR_State();

		cb.add(new ICompilationBus.CB_Process() {
			@Override
			public List<ICompilationBus.CB_Action> steps() {
				// 1. find stdlib
				//   -- question placement
				//   -- ...
				final ICompilationBus.CB_Action a = new ICompilationBus.CB_Action() {
					private final CR_FindStdlibAction aa = new CR_FindStdlibAction();

					@Override
					public String name() {
						return "find stdlib";
					}

					@Override
					public void execute() {
						aa.execute(st1);
					}

					@Override
					public ICompilationBus.OutputString[] outputStrings() {
						return new ICompilationBus.OutputString[0];
					}
				};
				// 2. process the initial
				final ICompilationBus.CB_Action b = new ICompilationBus.CB_Action() {
					private final CR_ProcessInitialAction aa = new CR_ProcessInitialAction(ci, do_out);

					@Override
					public String name() {
						return "process initial action";
					}

					@Override
					public void execute() {
						aa.execute(st1);
					}

					@Override
					public ICompilationBus.OutputString[] outputStrings() {
						return new ICompilationBus.OutputString[0];
					}
				};
				// 3. do rest
				final ICompilationBus.CB_Action c = new ICompilationBus.CB_Action() {
					private final CR_RunBetterAction aa = new CR_RunBetterAction();

					@Override
					public String name() {
						return "run better";
					}

					@Override
					public void execute() {
						aa.execute(st1);
					}

					@Override
					public ICompilationBus.OutputString[] outputStrings() {
						return new ICompilationBus.OutputString[0];
					}
				};

				return List_of(a, b, c);
			}
		});
	}

	private void logProgress(final int number, final String text) {
		if (number == 130) return;

		System.err.println(MessageFormat.format("{0} {1}", number, text));
	}

	public void doFindCIs(final String[] args2, final ICompilationBus cb) {
		// TODO map + "extract"

		final CR_State st1 = new CR_State();

		cb.add(new ICompilationBus.CB_Process() {
			@Override
			public List<ICompilationBus.CB_Action> steps() {
				final ICompilationBus.CB_Action a = new ICompilationBus.CB_Action() {
					private final CR_Action a = new CR_FindCIs(args2);

					@Override
					public String name() {
						return a.name();
					}

					@Override
					public void execute() {
						st1.cur = this;
						a.execute(st1);
						st1.cur = null;
					}

					@Override
					public ICompilationBus.OutputString[] outputStrings() {
						return new ICompilationBus.OutputString[0];
					}
				};

				final ICompilationBus.CB_Action b = new ICompilationBus.CB_Action() {
					private final CR_Action a = new CR_AlmostComplete();

					@Override
					public String name() {
						return a.name();
					}

					@Override
					public void execute() {
						a.execute(st1);
					}

					@Override
					public ICompilationBus.OutputString[] outputStrings() {
						return new ICompilationBus.OutputString[0];
					}
				};

				return List_of(a, b);
			}
		});

	}

	private @NotNull Operation<CompilerInstructions> findStdLib(final String prelude_name, final @NotNull Compilation c) {
		final ErrSink errSink = c.getErrSink();
		final IO      io      = c.getIO();

		// TODO CP_Paths.stdlib(...)
		final File local_stdlib = new File("lib_elijjah/lib-" + prelude_name + "/stdlib.ez");
		if (local_stdlib.exists()) {
			final EzSpec spec;
			try (final InputStream s = io.readFile(local_stdlib)) {
				spec = new EzSpec(local_stdlib.getName(), s, local_stdlib);
				final Operation<CompilerInstructions> oci = realParseEzFile(spec, ezCache());
				return oci;
			} catch (final Exception e) {
				return Operation.failure(e);
			}
		}

		return Operation.failure(new Exception() {
			public String message() {
				return "No stdlib found";
			}
		});
	}

	/**
	 * - I don't remember what absolutePath is for
	 * - Cache doesn't add to QueryDB
	 * <p>
	 * STEPS
	 * ------
	 * <p>
	 * 1. Get absolutePath
	 * 2. Check cache, return early
	 * 3. Parse (Query is incorrect I think)
	 * 4. Cache new result
	 *
	 * @param spec
	 * @param cache
	 * @return
	 */
	public Operation<CompilerInstructions> realParseEzFile(final EzSpec spec, final EzCache cache) {
		final @NotNull File file = spec.file();

		final String absolutePath;
		try {
			absolutePath = file.getCanonicalFile().toString();
		} catch (final IOException aE) {
			return Operation.failure(aE);
		}

		final Optional<CompilerInstructions> early = cache.get(absolutePath);

		if (early.isPresent()) {
			return Operation.success(early.get());
		}

		final Operation<CompilerInstructions> cio = CX_ParseEzFile.parseAndCache(spec, ezCache(), absolutePath);
		return cio;
	}

	private @NotNull List<CompilerInstructions> searchEzFiles(final @NotNull File directory, final ErrSink errSink, final IO io, final Compilation c) {
		final QuerySearchEzFiles                     q    = new QuerySearchEzFiles(c, errSink, io, this);
		final Operation2<List<CompilerInstructions>> olci = q.process(directory);

		if (olci.mode() == Mode.SUCCESS) {
			return olci.success();
		}

		errSink.reportDiagnostic(olci.failure());
		return List_of();
	}

	public EzCache ezCache() {
		return ezCache;
	}

	interface CR_Process {
		List<ICompilationBus.CB_Action> steps();
	}

	public interface CR_Action {
		void attach(CompilationRunner cr);

		void execute(CR_State st);

		String name();
	}

	class CR_State {
		public ICompilationBus.CB_Action cur;
		ICompilationAccess ca;
		ProcessRecord      pr;
		RuntimeProcesses   rt;

		public ICompilationAccess ca() {
			if (ca == null) {
				ca = new DefaultCompilationAccess(compilation);
				pr = new ProcessRecord(ca);
			}

			return ca;
		}
	}

	class CR_AlmostComplete implements CR_Action {

		@Override
		public void attach(final CompilationRunner cr) {

		}

		@Override
		public void execute(final CR_State st) {
			cis.almostComplete();
		}

		@Override
		public String name() {
			return "cis almostComplete";
		}
	}

//	class CR_FindStdlib implements CR_Action {
//
//		private String prelude_name;
//
//		CR_FindStdlib(final String aPreludeName) {
//			prelude_name = aPreludeName;
//		}
//
//		@Override
//		public void attach(final CompilationRunner cr) {
//
//		}
//
//		@Override
//		public void execute(final CR_State st) {
//			@NotNull final Operation<CompilerInstructions> op = findStdLib(prelude_name, compilation);
//			assert op.mode() == Mode.SUCCESS; // TODO .NOTHING??
//		}

	class CR_FindCIs implements CR_Action {

		private final String[] args2;

		CR_FindCIs(final String[] aArgs2) {
			args2 = aArgs2;
		}

		@Override
		public void attach(final CompilationRunner cr) {

		}

		@Override
		public void execute(final CR_State st) {
			final Compilation c = st.ca().getCompilation();

			final IProgressSink ps = new IProgressSink() {
				@Override
				public void note(final int aCode, final ProgressSinkComponent aCci, final int aType, final Object[] aParams) {

				}
			};

			find_cis(args2, c, c.getErrSink(), c.getIO(), cb, ps);
		}

		@Override
		public String name() {
			return "find cis";
		}

		protected void find_cis(final @NotNull String @NotNull [] args2,
		                        final @NotNull Compilation c,
		                        final @NotNull ErrSink errSink,
		                        final @NotNull IO io,
		                        final ICompilationBus cb,
		                        final IProgressSink ps) {
			CompilerInstructions ez_file;
			for (final String file_name : args2) {
				final File    f        = new File(file_name);
				final boolean matches2 = Pattern.matches(".+\\.ez$", file_name);
				if (matches2) {
					final ILazyCompilerInstructions ilci = ILazyCompilerInstructions.of(f, c);
					cci.accept(new Maybe<>(ilci, null));

					cb.inst(ilci);
				} else {
					//errSink.reportError("9996 Not an .ez file "+file_name);
					if (f.isDirectory()) {
						final List<CompilerInstructions> ezs = searchEzFiles(f, errSink, io, c);

						switch (ezs.size()) {
						case 0:
							final Diagnostic d_toomany = new TooManyEz_ActuallyNone();
							final Maybe<ILazyCompilerInstructions> m = new Maybe<>(null, d_toomany);
							cci.accept(m);
							break;
						case 1:
							ez_file = ezs.get(0);
							final ILazyCompilerInstructions ilci = ILazyCompilerInstructions.of(ez_file);
							cci.accept(new Maybe<>(ilci, null));
							cb.inst(ilci);
							break;
						default:
							//final Diagnostic d_toomany = new TooManyEz_UseFirst();
							//add_ci(ezs.get(0));

							// more than 1 (negative is not possible)
							final Diagnostic d_toomany2 = new TooManyEz_BeSpecific();
							final Maybe<ILazyCompilerInstructions> m2 = new Maybe<>(null, d_toomany2);
							cci.accept(m2);
							break;
						}
					} else
						errSink.reportError("9995 Not a directory " + f.getAbsolutePath());
				}
			}
		}
	}
//	}

	private class CR_FindStdlibAction implements CR_Action {

		@Override
		public void attach(final CompilationRunner cr) {

		}

		@Override
		public void execute(final CR_State st) {
			final Operation<CompilerInstructions> oci = findStdLib(Compilation.CompilationAlways.defaultPrelude(), compilation);
			switch (oci.mode()) {
			case SUCCESS -> compilation.pushItem(oci.success()); // caught twice!!
			case FAILURE -> {
				compilation.getErrSink().exception(oci.failure());
				return;
			}
			default -> throw new IllegalStateException("Unexpected value: " + oci.mode());
			}
			logProgress(130, "GEN_LANG: " + oci.success().genLang());
		}

		@Override
		public String name() {
			return "find stdlib";
		}
	}

	private class CR_ProcessInitialAction implements CR_Action {
		private final CompilerInstructions ci;
		private final boolean              do_out;

		public CR_ProcessInitialAction(final CompilerInstructions aCi, final boolean aDo_out) {
			ci     = aCi;
			do_out = aDo_out;
		}

		@Override
		public void attach(final CompilationRunner cr) {

		}

		@Override
		public void execute(final CR_State st) {
			try {
				compilation.use(ci, do_out);
			} catch (final Exception aE) {
				throw new RuntimeException(aE); // FIXME
			}
		}

		@Override
		public String name() {
			return "process initial";
		}
	}

	private class CR_RunBetterAction implements CR_Action {
		@Override
		public void attach(final CompilationRunner cr) {

		}

		@Override
		public void execute(final CR_State st) {
			st.rt = StageToRuntime.get(st.ca().getStage(), st.ca(), st.pr);

			try {
				st.rt.run_better();
			} catch (final Exception aE) {
				throw new RuntimeException(aE); // FIXME
			}
		}

		@Override
		public String name() {
			return "run better";
		}
	}
}
