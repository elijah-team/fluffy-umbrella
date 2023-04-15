package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.i.CCI;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.i.CompilationClosure;
import tripleo.elijah.comp.CompilationRunner;
import tripleo.elijah.comp.CompilerInput;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.i.ICompilationBus;
import tripleo.elijah.comp.i.ILazyCompilerInstructions;
//import tripleo.elijah.comp.QuerySearchEzFiles;
import tripleo.elijah.comp.diagnostic.TooManyEz_ActuallyNone;
import tripleo.elijah.comp.diagnostic.TooManyEz_BeSpecific;
import tripleo.elijah.comp.i.IProgressSink;
import tripleo.elijah.comp.i.ProgressSinkComponent;
import tripleo.elijah.comp.queries.QuerySearchEzFiles;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_VariableTableEntry;
import tripleo.elijah.stages.deduce.post_bytecode.DefaultStateful;
import tripleo.elijah.stages.deduce.post_bytecode.Maybe;
import tripleo.elijah.stages.deduce.post_bytecode.State;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static tripleo.elijah.util.Helpers.List_of;

public class CR_FindCIs extends DefaultStateful implements CompilationRunner.CR_Action {
	private final List<CompilerInput> inputs;
	private final State st;
	private       CompilationRunner   compilationRunner;
	private       CCI                 cci;
	private       ICompilationBus     cb;

	@Contract(pure = true)
	public CR_FindCIs(final List<CompilerInput> aArgs2) {
		inputs = aArgs2;
		st        = CompilationRunner.ST.INITIAL;
	}

	@Override
	public void attach(final @NotNull CompilationRunner cr) {
		compilationRunner = cr;

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

		//cb                = cr.cb; //compilation.pa().getCompilation().;
		//cci               = cr.cci;

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

	}

	@Override
	public void execute(final @NotNull CR_State st, final CB_Output aO) {
		final Compilation c = st.ca().getCompilation();

		final IProgressSink ps = new IProgressSink() {
			@Override
			public void note(final int aCode, final ProgressSinkComponent aComponent, final int aType, final Object[] aParams) {
				final int y = 2;
				aO.print("" + Arrays.toString(aParams));
			}
		};

		final List<CompilerInput> x = find_cis(inputs, c, c.getErrSink());
		for (final CompilerInput compilerInput : x) {
			cci.accept(compilerInput.acceptance_ci(), ps);
		}
	}

	@Override
	public String name() {
		return "find cis";
	}

	protected List<CompilerInput> find_cis(final @NotNull List<CompilerInput> inputs,
	                                       final @NotNull Compilation c,
	                                       final @NotNull ErrSink errSink) {
		final List<CompilerInput> x = new ArrayList<>();





		//final IProgressSink ps = cis.ps;
		final IProgressSink ps = new IProgressSink() {
			@Override
			public void note(final int aCode, final ProgressSinkComponent aCci, final int aType, final Object[] aParams) {
				tripleo.elijah.util.Stupidity.println_err_2(aCci.printErr(aCode, aType, aParams));
			}
		};







		CompilerInstructions ez_file;

		for (final CompilerInput input : inputs) {
			if (!input.isSourceRoot()) continue;

			final String  file_name = input.getInp();
			final File    f         = new File(file_name);
			final boolean matches2  = Pattern.matches(".+\\.ez$", file_name);
			if (matches2) {
				final ILazyCompilerInstructions ilci = ILazyCompilerInstructions.of(f, c);

				final Maybe<ILazyCompilerInstructions> m4 = new Maybe<>(ilci, null);
				input.accept_ci(m4);
				x.add(input);
			} else {
				//errSink.reportError("9996 Not an .ez file "+file_name);
				if (f.isDirectory()) {
					final List<CompilerInstructions> ezs = searchEzFiles(f, c.getCompilationClosure());

					switch (ezs.size()) {
					case 0:
						final Diagnostic d_toomany = new TooManyEz_ActuallyNone();
						final Maybe<ILazyCompilerInstructions> m = new Maybe<>(null, d_toomany);
						input.accept_ci(m);
						x.add(input);
						break;
					case 1:
						ez_file = ezs.get(0);
						final ILazyCompilerInstructions ilci = ILazyCompilerInstructions.of(ez_file);
						final Maybe<ILazyCompilerInstructions> m3 = new Maybe<>(ilci, null);
						input.accept_ci(m3);
						x.add(input);
						break;
					default:
						//final Diagnostic d_toomany = new TooManyEz_UseFirst();
						//add_ci(ezs.get(0));

						// more than 1 (negative is not possible)
						final Diagnostic d_toomany2 = new TooManyEz_BeSpecific();
						final Maybe<ILazyCompilerInstructions> m2 = new Maybe<>(null, d_toomany2);
						input.accept_ci(m2);
						x.add(input);
						break;
					}
				} else
					errSink.reportError("9995 Not a directory " + f.getAbsolutePath());
			}
		}

		return x;
	}

	private List<CompilerInstructions> searchEzFiles(final File directory, final CompilationClosure ccl) {
		final QuerySearchEzFiles                     q    = new QuerySearchEzFiles(ccl, compilationRunner);
		final Operation2<List<CompilerInstructions>> olci = q.process(directory);

		if (olci.mode() == Mode.SUCCESS) {
			return olci.success();
		}

		ccl.errSink().reportDiagnostic(olci.failure());
		return List_of();
	}
}
