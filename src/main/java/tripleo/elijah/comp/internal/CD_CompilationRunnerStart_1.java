package tripleo.elijah.comp.internal;

import com.google.common.base.Preconditions;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.*;

import static tripleo.elijah.nextgen.query.Mode.FAILURE;

public class CD_CompilationRunnerStart_1 implements CD_CompilationRunnerStart {
	@Override
	public void start(final CompilationRunner aCompilationRunner, final CompilerInstructions aCi, final boolean aDoOut) {
		try {
			_____start(aCi, aDoOut, aCompilationRunner.compilation.getCompilationClosure(), aCompilationRunner);
		} catch (Exception aE) {
			throw new RuntimeException(aE);
		}
	}

	public void _____start(final CompilerInstructions ci, final boolean do_out, final CompilationClosure ccl, final CompilationRunner aCompilationRunner) throws Exception {
		// 0. find stdlib
		//   -- question placement
		//   -- ...
		{
			final Operation<CompilerInstructions> x = aCompilationRunner.findStdLib(Compilation.CompilationAlways.defaultPrelude(), ccl.getCompilation());
			if (x.mode() == FAILURE) {
				ccl.errSink().exception(x.failure());
				return;
			}
			aCompilationRunner.logProgress(130, "GEN_LANG: " + x.success().genLang());
		}

		// 1. process the initial
		ccl.getCompilation().use(ci, do_out);

		// 2. do rest
		Preconditions.checkNotNull(ccl.getCompilation().__cr);
		final CR_State crState = new CR_State(ccl.getCompilation().__cr);

		final ICompilationAccess                   ca = crState.ca();
		final ProcessRecord                        pr = crState.pr;
		final tripleo.elijah.comp.RuntimeProcesses rt = tripleo.elijah.comp.StageToRuntime.get(ccl.getCompilation().stage, ca, pr);

		rt.run_better();
	}
}
