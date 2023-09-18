package tripleo.elijah.comp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.caches.DefaultEzCache;
import tripleo.elijah.comp.diagnostic.TooManyEz_ActuallyNone;
import tripleo.elijah.comp.diagnostic.TooManyEz_BeSpecific;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.i.IProgressSink;
import tripleo.elijah.comp.i.ProgressSinkComponent;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.comp.specs.EzCache;
import tripleo.elijah.comp.specs.EzSpec;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.util.Maybe;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.util.Operation;
import tripleo.elijah.util.Operation2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static tripleo.elijah.util.Helpers.List_of;

public class CompilationRunner {
	private class CB_CR_Start implements ICompilationBus.CB_Process {
		private final CR_State             st1;
		private final CompilerInstructions ci;
		private final boolean              do_out;

		final ICompilationBus.CB_Action a;
		final ICompilationBus.CB_Action b;
		final ICompilationBus.CB_Action c;

		public CB_CR_Start(final CR_State aSt1, final CompilerInstructions aCi, final boolean aDo_out) {
			st1    = aSt1;
			ci     = aCi;
			do_out = aDo_out;

			// 1. find stdlib
			//   -- question placement
			//   -- ...
			a = new ActionWrapper2(new CR_FindStdlibAction(), "find stdlib", st1);
			// 2. process the initial
			b = new ActionWrapper2(new CR_ProcessInitialAction(ci, do_out), "process initial action", st1);
			// 3. do rest
			c = new ActionWrapper2(new CR_RunBetterAction(), "run better", st1);
		}

		@Override
		public List<ICompilationBus.CB_Action> steps() {
			return List_of(a, b, c);
		}
	}
	private class CB_FindCIs implements ICompilationBus.CB_Process {
		private final String[] args2;
		private final CR_State crState;

		public CB_FindCIs(final String[] aArgs2, final CR_State aCrState) {
			args2   = aArgs2;
			crState = aCrState;
		}

		@Override
		public List<ICompilationBus.CB_Action> steps() {
			// TODO map + "extract"
			final ICompilationBus.CB_Action a = new ActionWrapper(new CR_FindCIs(args2), crState);
			final ICompilationBus.CB_Action b = new ActionWrapper(new CR_AlmostComplete(), crState);

			return List_of(a, b);
		}
	}
	public interface CR_Action {
		void attach(CompilationRunner cr);

		void execute(CR_State st);

		String name();
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
			final Compilation c = st.ca(compilation).getCompilation();

			final IProgressSink ps = new IProgressSink() {
				@Override
				public void note(final int aCode, final ProgressSinkComponent aCci, final int aType, final Object[] aParams) {

				}
			};

			find_cis(args2, c, c.getErrSink(), c.getIO(), cb, ps);
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

		@Override
		public String name() {
			return "find cis";
		}
	}
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

	interface CR_Process {
		List<ICompilationBus.CB_Action> steps();
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
			final CompilationEnclosure ce = compilation.getCompilationEnclosure();

			assert ce != null;

			st.rt = StageToRuntime.get(st.ca(compilation).getStage(), st.ca(compilation), st.pr, ce);

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

	public static class CR_State {
		public ICompilationBus.CB_Action cur;
		ICompilationAccess ca;
		ProcessRecord      pr;
		RuntimeProcesses   rt;

		public CR_State() {
			NotImplementedException.raise();
		}

		public ICompilationAccess ca(final Compilation aCompilation) {
			if (ca == null) {
				ca = new DefaultCompilationAccess(aCompilation);
				pr = new ProcessRecord(ca);
			}

			return ca;
		}
	}

	private final Compilation     compilation;

	private final Compilation.CIS cis;

	private final CCI             cci;

	private final ICompilationBus  cb;

	private final EzCache          ezCache = new DefaultEzCache();

	private final IProgressSink    ps;

	@Contract(pure = true)
	public CompilationRunner(final Compilation aCompilation, final Compilation.CIS a_cis, final ICompilationBus aCb, final IProgressSink ps1) {
		compilation = aCompilation;
		cis         = a_cis;
		cci         = new CCI(compilation, a_cis, ps1);
		cb          = aCb;
		ps          = ps1;
	}

	public void doFindCIs(final String[] args2, final ICompilationBus cbx) {
		final CR_State st1 = compilation.getCompilationEnclosure().getCrState(cb);

		cb.add(new CB_FindCIs(args2, st1));
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

	public EzCache ezCache() {
		return ezCache;
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

	private void logProgress(final int number, final String text) {
		if (number == 130) return;

		System.err.println(MessageFormat.format("CompilationRunner::logProgress: {0} {1}", number, text));
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

	void start(final CompilerInstructions ci, final boolean do_out) throws Exception {
		final CR_State st1 = compilation.getCompilationEnclosure().getCrState(cb);

		cb.add(new CB_CR_Start(st1, ci, do_out));
	}
}
