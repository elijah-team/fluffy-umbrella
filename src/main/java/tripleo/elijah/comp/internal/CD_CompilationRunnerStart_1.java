package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.CD_CompilationRunnerStart;
import tripleo.elijah.comp.i.CompilationClosure;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.i.IPipelineAccess;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

public class CD_CompilationRunnerStart_1 implements CD_CompilationRunnerStart {
	@Override
	public void start(final @NotNull CompilationRunner cr,
					  final @NotNull CompilerInstructions aCompilerInstructions,
					  final boolean do_out,
					  final @NotNull IPipelineAccess pa) {
		try {
			_____start(aCompilerInstructions, do_out, cr, pa);
		} catch (Exception aE) {
			final CompilationClosure ccl = pa.getCompilationClosure();
			ccl.errSink().exception(aE);
		}
	}

	public void _____start_(final CompilerInstructions ci,
							final boolean do_out,
							final @NotNull CompilationRunner cr,
							final @NotNull IPipelineAccess pa) throws Exception {
		final CB_Output out = new CB_Output();

		final CR_FindCIs              f1 = new CR_FindCIs(pa.getCompilerInput());
		final CR_ProcessInitialAction f2 = new CR_ProcessInitialAction(cr, ci, do_out);
		final CR_AlmostComplete       f3 = new CR_AlmostComplete(cr);
		final CR_RunBetterAction      f4 = new CR_RunBetterAction();

		final @NotNull List<CR_Action> l = List_of(f1, f2, f3, f4);

		for (final CR_Action each : l) {
			each.execute(cr.crState, out);
		}
	}

	public void _____start(final @NotNull CompilerInstructions ci,
						   final boolean do_out,
						   final @NotNull CompilationRunner cr,
						   final @NotNull IPipelineAccess pa) throws Exception {
		final CompilationClosure ccl = pa.getCompilationClosure();

		// 0. find stdlib
		//   -- question placement
		//   -- ...
		final String                          preludeName = Compilation.CompilationAlways.defaultPrelude();
		final Operation<CompilerInstructions> x           = cr.findStdLib2(preludeName, ccl);

		switch (x.mode()) {
		case FAILURE -> ccl.errSink().exception(x.failure());
		default -> cr.logProgress(130, "GEN_LANG: " + x.success().genLang());
		}

		// 1. process the initial
		ccl.getCompilation().use(ci, do_out);

		// 2. do rest
		final CR_State           crState = cr.crState;

		final ICompilationAccess ca      = crState.ca();
		final ProcessRecord      pr      = crState.pr;


		assert pa == pr.pa();


		final RuntimeProcesses   rt = StageToRuntime.get(ccl.getCompilation().stage, ca, pr, pa);

		rt.run_better();
	}
}
