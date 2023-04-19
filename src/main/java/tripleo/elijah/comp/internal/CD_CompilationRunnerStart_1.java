package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationRunner;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.i.CD_CompilationRunnerStart;
import tripleo.elijah.comp.i.CompilationClosure;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.i.IPipelineAccess;

import static tripleo.elijah.nextgen.query.Mode.FAILURE;

public class CD_CompilationRunnerStart_1 implements CD_CompilationRunnerStart {
	@Override
	public void start(final CompilationRunner aCompilationRunner,
					  final CompilerInstructions aCi,
					  final boolean aDoOut,
					  final IPipelineAccess pa) {
		final CompilationClosure ccl = aCompilationRunner.compilation.getCompilationClosure();
		try {

			_____start(aCi, aDoOut, ccl, aCompilationRunner, pa);
		} catch (Exception aE) {
			ccl.errSink().exception(aE);
		}
	}

	public void _____start(final CompilerInstructions ci,
						   final boolean do_out,
						   final CompilationClosure ccl,
						   final @NotNull CompilationRunner cr,
						   final IPipelineAccess pa) throws Exception {
		// 0. find stdlib
		//   -- question placement
		//   -- ...
		{

			Operation<CompilerInstructions>[] y = new Operation[1];

			final Operation<CompilerInstructions> x = cr.findStdLib2(Compilation.CompilationAlways.defaultPrelude(),
																	 ccl.getCompilation()
																	 //,(x) -> {y[0]=x;}
																	);
			if (x.mode() == FAILURE) {
				ccl.errSink().exception(x.failure());
				return;
			}

			cr.logProgress(130, "GEN_LANG: " + x.success().genLang());
		}

		// 1. process the initial
		ccl.getCompilation().use(ci, do_out);

		// 2. do rest
		final CR_State crState = cr.crState;

		final ICompilationAccess ca = crState.ca();
		final ProcessRecord      pr = crState.pr;
		final RuntimeProcesses   rt = StageToRuntime.get(ccl.getCompilation().stage, ca, pr, pr.pa);

		rt.run_better();
	}
}
