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
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.comp.i.IProgressSink;
import tripleo.elijah.comp.i.ProgressSinkComponent;
import tripleo.elijah.comp.internal.CompilationBus;
import tripleo.elijah.comp.queries.QueryEzFileToModule;
import tripleo.elijah.comp.queries.QueryEzFileToModuleParams;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.stages.deduce.post_bytecode.Maybe;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static tripleo.elijah.nextgen.query.Mode.FAILURE;
import static tripleo.elijah.nextgen.query.Mode.SUCCESS;

public class CompilationRunner {
	public final  Compilation     compilation;
	private final Compilation.CIS cis;
	private final CCI             cci;

	@Contract(pure = true)
	public CompilationRunner(final Compilation aCompilation, final Compilation.CIS a_cis, CompilationBus cb) {
		compilation = aCompilation;
		cis         = a_cis;
		cci         = new DefaultCCI(compilation, a_cis, new IProgressSink() {
			@Override
			public void note(final int aCode, final ProgressSinkComponent aProgressSinkComponent, final int aType, final Object[] aParams) {
				tripleo.elijah.util.Stupidity.println_err_2(aProgressSinkComponent.printErr(aCode, aType, aParams));
			}
		});
	}

	void start(final CompilerInstructions ci, final boolean do_out, final @NotNull OptionsProcessor ignoredOp, final IPipelineAccess pa) throws Exception {
		Operation<CompilationBus.CompilerDriven> ocrsd = compilation.cb.cd.get(Compilation.CompilationAlways.Tokens.COMPILATION_RUNNER_START);

		if (ocrsd.mode() == FAILURE) {
			throw new Error();
		}

		((CD_CompilationRunnerStart) ocrsd.success()).start(this, ci, do_out, pa);
	}
	void findStdLib2(final String prelude_name, final @NotNull Compilation c) {
		Operation<CompilationBus.CompilerDriven> ocrfsld = compilation.cb.cd.get(Compilation.CompilationAlways.Tokens.COMPILATION_RUNNER_FIND_STDLIB);

		if (ocrfsld.mode() == FAILURE) {
			throw new Error();
		}

		((CD_FindStdLib) ocrfsld.success()).findStdLib(this, prelude_name, c);
	}

	/*
	 * Design question:
	 *   - why push and return?
	 *     we only want to check error
	 *     utilize exceptions --> only one usage
	 *     or inline (esp use of Compilation)
	 */
	@NotNull
	public Operation<CompilerInstructions> findStdLib(final String prelude_name, final @NotNull Compilation c) {
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

	private Operation<CompilerInstructions> parseEzFile_(final String f, final InputStream s) throws RecognitionException, TokenStreamException {
		final QueryEzFileToModuleParams qp = new QueryEzFileToModuleParams(f, s);
		return new QueryEzFileToModule(qp).calculate();
	}

	protected void find_cis(final @NotNull String @NotNull [] args2,
							final @NotNull Compilation c,
							final @NotNull ErrSink errSink,
							final @NotNull IO io) {




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
						final Diagnostic                       d_toomany = new TooManyEz_ActuallyNone();
						final Maybe<ILazyCompilerInstructions> m         = new Maybe<>(null, d_toomany);
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
						final Diagnostic                       d_toomany2 = new TooManyEz_BeSpecific();
						final Maybe<ILazyCompilerInstructions> m2         = new Maybe<>(null, d_toomany2);
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

	private Operation<CompilerInstructions> realParseEzFile(final String f, final @NotNull IO io, final Compilation c) {
		final File file = new File(f);

		try {
			return realParseEzFile(f, io.readFile(file), file, c);
		} catch (FileNotFoundException aE) {
			return Operation.failure(aE);
		}
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

	public void doFindCIs(final String[] args2) {
		final ErrSink errSink1 = compilation.getErrSink();
		final IO      io       = compilation.getIO();

		// TODO map + "extract"
		find_cis(args2, compilation, errSink1, io);

		cis.almostComplete();
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
			
			final CompilerInput input = inputs.get(i);
			final String  file_name = input.getInp();
			final File    f         = new File(file_name);
			final boolean matches2  = Pattern.matches(".+\\.ez$", file_name);
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
						final Diagnostic                       d_toomany = new TooManyEz_ActuallyNone();
						final Maybe<ILazyCompilerInstructions> m         = new Maybe<>(null, d_toomany);
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
						final Diagnostic                       d_toomany2 = new TooManyEz_BeSpecific();
						final Maybe<ILazyCompilerInstructions> m2         = new Maybe<>(null, d_toomany2);
						cci.accept(m2, ps);
						break;
					}
				} else
					errSink.reportError("9995 Not a directory " + f.getAbsolutePath());
			}
		}
	}
}
