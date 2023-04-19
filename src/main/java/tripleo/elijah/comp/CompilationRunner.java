package tripleo.elijah.comp;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.diagnostic.TooManyEz_ActuallyNone;
import tripleo.elijah.comp.diagnostic.TooManyEz_BeSpecific;
import tripleo.elijah.comp.i.*;
import tripleo.elijah.comp.internal.*;
import tripleo.elijah.comp.queries.QueryEzFileToModule;
import tripleo.elijah.comp.queries.QueryEzFileToModuleParams;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.stages.deduce.post_bytecode.DefaultStateful;
import tripleo.elijah.stages.deduce.post_bytecode.Maybe;
import tripleo.elijah.stages.deduce.post_bytecode.State;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static tripleo.elijah.nextgen.query.Mode.FAILURE;
import static tripleo.elijah.nextgen.query.Mode.SUCCESS;
import static tripleo.elijah.util.Helpers.List_of;

public class CompilationRunner {
	private static final List<State> registeredStates = new ArrayList<>();
	public final  Compilation     compilation;
	//	final         Map<String, CompilerInstructions> fn2ci = new HashMap<String, CompilerInstructions>();
//	public final  Compilation                       compilation;
//	private final Compilation.CIS                   cis;
//	public final  CCI                               cci;
	public final ICompilationBus cb;
	private final Compilation.CIS cis;
	private final CCI             cci;
	CR_FindCIs cr_find_cis;
	public final CR_State crState = new CR_State(CompilationRunner.this);

	@Contract(pure = true)
	public CompilationRunner(final Compilation aCompilation, final Compilation.CIS a_cis, CompilationBus aCompilationBus) {
		compilation = aCompilation;
		cis         = a_cis;
		cb = aCompilationBus;

		cci         = new DefaultCCI(compilation, a_cis, new IProgressSink() {
			@Override
			public void note(final int aCode, final ProgressSinkComponent aProgressSinkComponent, final int aType, final Object[] aParams) {
				tripleo.elijah.util.Stupidity.println_err_2(aProgressSinkComponent.printErr(aCode, aType, aParams));
			}
		});
	}

	public static State registerState(final State aState) {
		if (!(registeredStates.contains(aState))) {
			registeredStates.add(aState);

			final int id = registeredStates.indexOf(aState);

			aState.setIdentity(id);
			return aState;
		}

		return aState;
	}

	void start(final CompilerInstructions ci, final boolean do_out, final @NotNull OptionsProcessor ignoredOp, final IPipelineAccess pa) throws Exception {
		Operation<CompilationBus.CompilerDriven> ocrsd = compilation.cb.cd.get(Compilation.CompilationAlways.Tokens.COMPILATION_RUNNER_START);

		if (ocrsd.mode() == FAILURE) {
			throw new Error();
		}

		((CD_CompilationRunnerStart) ocrsd.success()).start(this, ci, do_out, pa);
	}

	public Operation<CompilerInstructions> findStdLib(final String prelude_name, final @NotNull Compilation c) {
		Operation<CompilationBus.CompilerDriven> ocrfsld = compilation.cb.cd.get(Compilation.CompilationAlways.Tokens.COMPILATION_RUNNER_FIND_STDLIB);

		if (ocrfsld.mode() == FAILURE) {
			throw new Error();
		}

		Operation<CompilerInstructions>[] y = new Operation[1];

		((CD_FindStdLib) ocrfsld.success()).findStdLib(this, prelude_name, c
				, (x) -> {
					y[0] = x;
				}
													  );

		return y[0];
	}

	/*
	 * Design question:
	 *   - why push and return?
	 *     we only want to check error
	 *     utilize exceptions --> only one usage
	 *     or inline (esp use of Compilation)
	 */
	@NotNull
	public Operation<CompilerInstructions> findStdLib2(final String prelude_name, final @NotNull Compilation c) {
		final ErrSink errSink = c.getErrSink();
		final IO      io      = c.getIO();

		// TODO stdlib path here
		final File local_stdlib = new File("lib_elijjah/lib-" + prelude_name + "/stdlib.ez");
		if (local_stdlib.exists()) {
			try {
				final Operation<CompilerInstructions> oci = realParseEzFile(local_stdlib.getName(), io.readFile(local_stdlib), local_stdlib, c);
				if (oci.mode() == SUCCESS) {
					c.pushItem(oci.success());
					return oci;
				}
			} catch (final Exception e) {
				return Operation.failure(e);
			}
		}
		//return false;
		return Operation.failure(new Exception() {
			public String message() {
				return "No stdlib found";
			}
		});
	}

	public Operation<CompilerInstructions> realParseEzFile(final String f,
														   final InputStream s,
														   final @NotNull File file,
														   final Compilation c) {
		final String absolutePath;
		try {
			absolutePath = file.getCanonicalFile().toString(); // TODO 04/10 hash this and "attach"
			//queryDB.attach(compilerInput, new EzFileIdentity_Sha256($hash)); // ??
		} catch (IOException aE) {
			//throw new RuntimeException(aE);
			return Operation.failure(aE);
		}

		// TODO 04/10
		// Cache<CompilerInput, CompilerInstructions> fn2ci /*EzFileIdentity??*/(MAP/*??*/, resolver is try stmt)
		if (c.fn2ci.containsKey(absolutePath)) { // don't parse twice
			// TODO 04/10
			// ...queryDB.attach(compilerInput, new EzFileIdentity_Sha256($hash)); // ?? fnci
			return Operation.success(c.fn2ci.get(absolutePath));
		}

		try {
			try {
				final Operation<CompilerInstructions> cio = parseEzFile_(f, s);

				if (cio.mode() != SUCCESS) {
					final Exception e = cio.failure();
					assert e != null;

					tripleo.elijah.util.Stupidity.println_err_2(("parser exception: " + e));
					e.printStackTrace(System.err);
					//s.close();
					return cio;
				}

				final CompilerInstructions R = cio.success();
				R.setFilename(file.toString());
				c.fn2ci.put(absolutePath, R);
				return cio;
			} catch (final ANTLRException e) {
				tripleo.elijah.util.Stupidity.println_err_2(("parser exception: " + e));
				e.printStackTrace(System.err);
				return Operation.failure(e);
			}
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException aE) {
					// TODO return inside finally: is this ok??
					return new Operation<>(null, aE, FAILURE);
				}
			}
		}
	}

	private Operation<CompilerInstructions> parseEzFile_(final String f, final InputStream s) throws RecognitionException, TokenStreamException {
		final QueryEzFileToModuleParams qp = new QueryEzFileToModuleParams(f, s);
		return new QueryEzFileToModule(qp).calculate();
	}

	public void doFindCIs(final String[] args2) {
		final ErrSink errSink1 = compilation.getErrSink();
		final IO      io       = compilation.getIO();

		// TODO map + "extract"
		find_cis(args2, compilation, errSink1, io);

		cis.almostComplete();
	}

	protected void find_cis(final @NotNull String @NotNull [] args2,
							final @NotNull Compilation c,
							final @NotNull ErrSink errSink,
							final @NotNull IO io) {

		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		cr_find_cis = new CR_FindCIs(List_of());
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		/********************/
		cr_find_cis.execute(crState, new CB_Output());


		//final IProgressSink ps = cis.ps;
		final IProgressSink ps = new IProgressSink() {
			@Override
			public void note(final int aCode, final ProgressSinkComponent aCci, final int aType, final Object[] aParams) {
				tripleo.elijah.util.Stupidity.println_err_2(aCci.printErr(aCode, aType, aParams));
			}
		};


		CompilerInstructions ez_file;
		for (int i = 0; i < args2.length; i++) {
			final String  file_name = args2[i];
			final File    f         = new File(file_name);
			final boolean matches2  = Pattern.matches(".+\\.ez$", file_name);
			if (matches2) {
				ILazyCompilerInstructions ilci = ILazyCompilerInstructions.of(f, c);
				cci.accept(new Maybe<>(ilci, null), ps);
			} else {
				//errSink.reportError("9996 Not an .ez file "+file_name);
				if (f.isDirectory()) {
					final List<CompilerInstructions> ezs = searchEzFiles(f, errSink, io, c);

					switch (ezs.size()) {
					case 0:
						final Diagnostic d_toomany = new TooManyEz_ActuallyNone();
						final Maybe<ILazyCompilerInstructions> m = new Maybe<>(null, d_toomany);
						cci.accept(m, ps);
						break;
					case 1:
						ez_file = ezs.get(0);
						cci.accept(new Maybe<>(ILazyCompilerInstructions.of(ez_file), null), ps);
						break;
					default:
						//final Diagnostic d_toomany = new TooManyEz_UseFirst();
						//add_ci(ezs.get(0));

						// more than 1 (negative is not possible)
						final Diagnostic d_toomany2 = new TooManyEz_BeSpecific();
						final Maybe<ILazyCompilerInstructions> m2 = new Maybe<>(null, d_toomany2);
						cci.accept(m2, ps);
						break;
					}
				} else
					errSink.reportError("9995 Not a directory " + f.getAbsolutePath());
			}
		}
	}

	private @NotNull List<CompilerInstructions> searchEzFiles(final @NotNull File directory, final ErrSink errSink, final IO io, final Compilation c) {
		final List<CompilerInstructions> R = new ArrayList<CompilerInstructions>();
		final FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(final File file, final String s) {
				final boolean matches2 = Pattern.matches(".+\\.ez$", s);
				return matches2;
			}
		};
		final String[] list = directory.list(filter);
		if (list != null) {
			for (final String file_name : list) {
				try {
					final File                 file   = new File(directory, file_name);
					final CompilerInstructions ezFile = parseEzFile(file, file.toString(), errSink, io, c);
					if (ezFile != null)
						R.add(ezFile);
					else
						errSink.reportError("9995 ezFile is null " + file);
				} catch (final Exception e) {
					errSink.exception(e);
				}
			}
		}
		return R;
	}

	@Nullable CompilerInstructions parseEzFile(final @NotNull File f, final String file_name, final ErrSink errSink, final IO io, final Compilation c) throws Exception {
		final Operation<CompilerInstructions> om = parseEzFile1(f, file_name, errSink, io, c);

		final CompilerInstructions m;

		if (om.mode() == SUCCESS) {
			m = om.success();

/*
		final String prelude;
		final String xprelude = m.genLang();
		tripleo.elijah.util.Stupidity.println_err_2("230 " + prelude);
		if (xprelude == null)
			prelude = CompilationAlways.defaultPrelude(); // TODO should be java for eljc
		else
			prelude = null;
*/
		} else {
			m = null;
		}

		return m;
	}

	public @NotNull Operation<CompilerInstructions> parseEzFile1(final @NotNull File f,
																 final String file_name,
																 final ErrSink errSink,
																 final IO io,
																 final Compilation c) {
		System.out.printf("   %s%n", f.getAbsolutePath());
		if (!f.exists()) {
			errSink.reportError(
					"File doesn't exist " + f.getAbsolutePath());
			return null;
		} else {
			final Operation<CompilerInstructions> om = realParseEzFile(file_name/*, io.readFile(f), f*/, io, c);
			return om;
		}
	}

	private Operation<CompilerInstructions> realParseEzFile(final String f, final @NotNull IO io, final Compilation c) {
		final File file = new File(f);

		try {
			return realParseEzFile(f, io.readFile(file), file, c);
		} catch (FileNotFoundException aE) {
			return Operation.failure(aE);
		}
	}

	public void logProgress(final int number, final String text) {
		if (number == 130) return;

		tripleo.elijah.util.Stupidity.println_err_2(number + " " + text);
	}

	public void doFindCIs(final List<CompilerInput> aInputs, final String[] args2, final CompilationBus cb) {
		final ErrSink errSink1 = compilation.getErrSink();
		final IO      io       = compilation.getIO();

		// TODO map + "extract"
		find_cis(aInputs, compilation, errSink1, io);

		cis.almostComplete();
	}

	private void find_cis(List<CompilerInput> inputs, Compilation c, ErrSink errSink, IO io) {


		//final IProgressSink ps = cis.ps;
		final IProgressSink ps = new IProgressSink() {
			@Override
			public void note(final int aCode, final ProgressSinkComponent aCci, final int aType, final Object[] aParams) {
				tripleo.elijah.util.Stupidity.println_err_2(aCci.printErr(aCode, aType, aParams));
			}
		};


		CompilerInstructions ez_file;
		for (int i = 0; i < inputs.size(); i++) {

			final CompilerInput input     = inputs.get(i);
			final String        file_name = input.getInp();
			final File          f         = new File(file_name);
			final boolean       matches2  = Pattern.matches(".+\\.ez$", file_name);
			if (matches2) {
				ILazyCompilerInstructions ilci = ILazyCompilerInstructions.of(f, c);
				cci.accept(new Maybe<>(ilci, null), ps);
			} else {
				//errSink.reportError("9996 Not an .ez file "+file_name);
				if (f.isDirectory()) {
					input.setDirectory(f);

					final List<CompilerInstructions> ezs = searchEzFiles(f, errSink, io, c);

					switch (ezs.size()) {
					case 0:
						final Diagnostic d_toomany = new TooManyEz_ActuallyNone();
						final Maybe<ILazyCompilerInstructions> m = new Maybe<>(null, d_toomany);
						cci.accept(m, ps);
						break;
					case 1:
						ez_file = ezs.get(0);
						cci.accept(new Maybe<>(ILazyCompilerInstructions.of(ez_file), null), ps);
						break;
					default:
						//final Diagnostic d_toomany = new TooManyEz_UseFirst();
						//add_ci(ezs.get(0));

						// more than 1 (negative is not possible)
						final Diagnostic d_toomany2 = new TooManyEz_BeSpecific();
						final Maybe<ILazyCompilerInstructions> m2 = new Maybe<>(null, d_toomany2);
						cci.accept(m2, ps);
						break;
					}
				} else
					errSink.reportError("9995 Not a directory " + f.getAbsolutePath());
			}
		}
	}

	void __00__start(final CompilerInstructions ci, final boolean do_out) throws Exception {

		cb.add(new ICompilationBus.CB_Process() {
			@Override
			public List<ICompilationBus.CB_Action> steps() {
				// 1. find stdlib
				//   -- question placement
				//   -- ...
				final ICompilationBus.CB_Action a = new ICompilationBus.CB_Action() {
					final CB_Output o = new CB_Output();
					private final CR_FindStdlibAction aa = new CR_FindStdlibAction();

					@Override
					public String name() {
						return aa.name();
					}

					@Override
					public void execute() {
						aa.execute(crState, o);
					}

					@Override
					public List<ICompilationBus.OutputString> outputStrings() {
						return o.get();
					}
				};
				// 2. process the initial
				final ICompilationBus.CB_Action b = new ICompilationBus.CB_Action() {
					final CB_Output o = new CB_Output();
					private final CR_ProcessInitialAction aa = new CR_ProcessInitialAction(ci, do_out);

					@Override
					public String name() {
						return aa.name();
					}

					@Override
					public void execute() {
						aa.execute(crState, o);
					}

					@Override
					public List<ICompilationBus.OutputString> outputStrings() {
						return o.get();
					}
				};
				// 3. do rest
				final ICompilationBus.CB_Action c = new ICompilationBus.CB_Action() {
					final CB_Output o = new CB_Output();
					private final CR_RunBetterAction aa = new CR_RunBetterAction();

					@Override
					public String name() {
						return aa.name();
					}

					@Override
					public void execute() {
						aa.execute(crState, o);
					}

					@Override
					public List<ICompilationBus.OutputString> outputStrings() {
						return o.get();
					}
				};

				return List_of(a, b, c);
			}
		});
	}

	public void doFindCIs(final List<CompilerInput> inputs, final String[] args2, final ICompilationBus cb) {
		// TODO map + "extract"
		cb.add(new _FindCI_Steps(inputs, args2));
	}

//	@Contract(pure = true)
//	public CompilationRunner(final Compilation aCompilation, final Compilation.CIS a_cis, final ICompilationBus aCb, final IProgressSink ps1) {
//		compilation = aCompilation;
//		cis         = a_cis;
//		cci         = new DefaultCCI(compilation, a_cis, ps1) {
//			@Override
//			public void accept(final @NotNull Maybe<ILazyCompilerInstructions> mcci, final IProgressSink aPs) {
//				super.accept(mcci, aPs);
//
//				if (mcci.isException()) {
//				} else {
//					cb.inst(mcci.o);
//					//ps1.note();
//				}
//			}
//		};
//		cb          = aCb;
////		ps = ps1;
//	}
//
//	@Contract(pure = true)
//	public CompilationRunner(final Compilation aCompilation, final Compilation.CIS a_cis, final ICompilationBus aCb) {
//		compilation = aCompilation;
//		cis         = a_cis;
//		final DefaultProgressSink ps1 = new DefaultProgressSink();
//		cci = new DefaultCCI(compilation, a_cis, ps1);
//		cb  = aCb;
//	}

	public enum ST {
		;
		public static State EXIT_RESOLVE;
		public static State EXIT_CONVERT_USER_TYPES;
		public static State INITIAL;

		public static void register() {
			//EXIT_RESOLVE            = registerState(new ST.ExitResolveState());
			INITIAL = registerState(new ST.InitialState());
			//EXIT_CONVERT_USER_TYPES = registerState(new ST.ExitConvertUserTypes());
		}

		static class ExitConvertUserTypes implements State {
			private int identity;

			@Override
			public void apply(final DefaultStateful element) {
				//final VariableTableEntry vte = ((DeduceElement3_VariableTableEntry) element).principal;

				//final DeduceTypes2         dt2     = ((DeduceElement3_VariableTableEntry) element).deduceTypes2();
			}

			@Override
			public void setIdentity(final int aId) {
				identity = aId;
			}

			@Override
			public boolean checkState(final DefaultStateful aElement3) {
				return true;
			}
		}

		static class InitialState implements State {
			private int identity;

			@Override
			public void apply(final DefaultStateful element) {

			}

			@Override
			public void setIdentity(final int aId) {
				identity = aId;
			}

			@Override
			public boolean checkState(final DefaultStateful aElement3) {
				return true;
			}
		}

		static class ExitResolveState implements State {
			private int identity;

			@Override
			public void apply(final DefaultStateful element) {
				//final VariableTableEntry vte = ((DeduceElement3_VariableTableEntry) element).principal;
			}

			@Override
			public void setIdentity(final int aId) {
				identity = aId;
			}

			@Override
			public boolean checkState(final DefaultStateful aElement3) {
				//return ((DeduceElement3_VariableTableEntry) aElement3).st == DeduceElement3_VariableTableEntry.ST.INITIAL;
				return false; // FIXME
			}
		}
	}

	public interface CR_Action {
		void attach(@NotNull CompilationRunner cr);

		void execute(@NotNull CR_State st, CB_Output aO);

		String name();
	}

	class _FindCI_Steps implements ICompilationBus.CB_Process {

		final         CR_State            st1 = crState;
		private final String[]            args2;
		private final List<CompilerInput> inputs;

		public _FindCI_Steps(final List<CompilerInput> aInputs, final String[] aArgs2) {
			inputs = aInputs;
			args2  = aArgs2;
		}

		@Override
		@NotNull
		public List<ICompilationBus.CB_Action> steps() {
			final ICompilationBus.CB_Action a = new _ActionBase(() -> {
				return new CR_FindCIs(inputs);
			}) {
				@Override
				public void execute() {
					st1.cur = this;
					action.execute(st1, o);
					st1.cur = null;
				}
			};

			final ICompilationBus.CB_Action b = new _ActionBase(CR_AlmostComplete::new) {
			};

			return List_of(a, b);
		}

		abstract class _ActionBase implements ICompilationBus.CB_Action {
			//			final Supplier<CR_Action> actionSupplier;
			protected final CR_Action action;
			protected final CB_Output o = new CB_Output();

			_ActionBase(final @NotNull Supplier<CR_Action> aActionSupplier) {
				action = aActionSupplier.get();
				action.attach(CompilationRunner.this);
			}

			@Override
			public String name() {
				return action.name();
			}

			@Override
			public void execute() {
				action.execute(st1, o);
			}

			@Override
			public List<ICompilationBus.OutputString> outputStrings() {
				return o.get();
			}

		}
	}

	//	/*
//	 * Design question:
//	 *   - why push and return?
//	 *     we only want to check error
//	 *     utilize exceptions --> only one usage
//	 *     or inline (esp use of Compilation)
//	 */
//	private @NotNull Operation<CompilerInstructions> findStdLib(final String prelude_name, final @NotNull Compilation c) {
//		final ErrSink errSink = c.getErrSink();
//		final IO      io      = c.getIO();
//
//		// TODO stdlib path here
//		final File local_stdlib = new File("lib_elijjah/lib-" + prelude_name + "/stdlib.ez");
//		if (local_stdlib.exists()) {
//			try {
//				final Operation<CompilerInstructions> oci = realParseEzFile(local_stdlib.getName(), io.readFile(local_stdlib), local_stdlib, c);
//				if (oci.mode() == Mode.SUCCESS) {
//					c.pushItem(oci.success());
//					return oci;
//				}
//			} catch (final Exception e) {
//				return Operation.failure(e);
//			}
//		}
//
//		return Operation.failure(new Exception() {
//			public String message() {
//				return "No stdlib found";
//			}
//		});
//	}
//
//	public Operation<CompilerInstructions> realParseEzFile(final String f,
//														   final InputStream s,
//														   final @NotNull File file,
//														   final Compilation c) {
//		final String absolutePath;
//		try {
//			absolutePath = file.getCanonicalFile().toString();
//		} catch (final IOException aE) {
//			//throw new RuntimeException(aE);
//			return Operation.failure(aE);
//		}
//
//		if (fn2ci.containsKey(absolutePath)) { // don't parse twice
//			return Operation.success(fn2ci.get(absolutePath));
//		}
//
//		try {
//			try {
//				final Operation<CompilerInstructions> cio = parseEzFile_(f, s);
//
//				if (cio.mode() == Mode.FAILURE) {
//					final Exception e = cio.failure();
//					assert e != null;
//
//					tripleo.elijah.util.Stupidity.println_err2(("parser exception: " + e));
//					e.printStackTrace(System.err);
//					//s.close();
//					return cio;
//				}
//
//				final CompilerInstructions R = cio.success();
//				R.setFilename(file.toString());
//				fn2ci.put(absolutePath, R);
//				return cio;
//			} catch (final ANTLRException e) {
//				tripleo.elijah.util.Stupidity.println_err2(("parser exception: " + e));
//				e.printStackTrace(System.err);
//				return Operation.failure(e);
//			}
//		} finally {
//			if (s != null) {
//				try {
//					s.close();
//				} catch (final IOException aE) {
//					// TODO return inside finally: is this ok??
//					return new Operation<>(null, aE, Mode.FAILURE);
//				}
//			}
//		}
//	}
//
//	private Operation<CompilerInstructions> parseEzFile_(final String f, final InputStream s) throws RecognitionException, TokenStreamException {
//		final QueryEzFileToModuleParams qp = new QueryEzFileToModuleParams(f, s);
//		return new QueryEzFileToModule(qp).calculate();
//	}
//
//	interface CR_Process {
//		List<ICompilationBus.CB_Action> steps();
//	}
//
//	public interface CR_Action {
//		void attach(CompilationRunner cr);
//
//		void execute(CR_State st, final CB_Output aO);
//
//		String name();
//	}
//
	class CR_AlmostComplete implements CR_Action {

		@Override
		public void attach(final CompilationRunner cr) {

		}

		@Override
		public void execute(final CR_State st, final CB_Output aO) {
			cis.almostComplete();
		}

		@Override
		public String name() {
			return "cis almostComplete";
		}
	}

	////	class CR_FindStdlib implements CR_Action {
////
////		private String prelude_name;
////
////		CR_FindStdlib(final String aPreludeName) {
////			prelude_name = aPreludeName;
////		}
////
////		@Override
////		public void attach(final CompilationRunner cr) {
////
////		}
////
////		@Override
////		public void execute(final CR_State st) {
////			@NotNull final Operation<CompilerInstructions> op = findStdLib(prelude_name, compilation);
////			assert op.mode() == Mode.SUCCESS; // TODO .NOTHING??
////		}
////	}
//
	private class CR_FindStdlibAction implements CR_Action {

		@Override
		public void attach(final CompilationRunner cr) {

		}

		@Override
		public void execute(final CR_State st, final CB_Output aO) {
			final Operation<CompilerInstructions> x = findStdLib(Compilation.CompilationAlways.defaultPrelude(), compilation);
			if (x.mode() == Mode.FAILURE) {
				compilation.getErrSink().exception(x.failure());
				return;
			}
			aO.logProgress(130, "GEN_LANG: " + x.success().genLang());
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
		public void execute(final CR_State st, final CB_Output aO) {
			try {
				compilation.use(ci, do_out);
			} catch (final Exception aE) {
				throw new RuntimeException(aE); // FIXME
			}
		}

		@Override
		public String name() {
//			"process initial action"
			return "process initial";
		}
	}

	private class CR_RunBetterAction implements CR_Action {
		@Override
		public void attach(final CompilationRunner cr) {

		}

		@Override
		public void execute(final CR_State st, final CB_Output aO) {
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			st.rt = StageToRuntime.get(st.ca().getStage(), st.ca(), st.pr, null);
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/
			/********************/

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
