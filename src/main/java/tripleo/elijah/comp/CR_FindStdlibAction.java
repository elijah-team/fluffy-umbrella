package tripleo.elijah.comp;

import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.i.CD_FindStdLib;
import tripleo.elijah.comp.internal.CB_Output;
import tripleo.elijah.comp.internal.CR_State;
import tripleo.elijah.comp.internal.CompilerDriven;
import tripleo.elijah.nextgen.query.Mode;

import static tripleo.elijah.nextgen.query.Mode.FAILURE;

class CR_FindStdlibAction implements CR_Action {

	private final CompilationRunner compilationRunner;

	public CR_FindStdlibAction(final CompilationRunner aCompilationRunner) {
		compilationRunner = aCompilationRunner;
	}

	@Override
	public void attach(final CompilationRunner cr) {

	}

	@Override
	public Operation<Boolean> execute(final CR_State st, final CB_Output aO) {
		Operation<CompilerDriven> ocrfsld = compilationRunner
				.compilation
				.getCompilationEnclosure()
				.getCompilationBus()
				.cd
				.get(Compilation.CompilationAlways.Tokens.COMPILATION_RUNNER_FIND_STDLIB);

		if (ocrfsld.mode() == FAILURE) {
			throw new Error();
		}

		Operation<CompilerInstructions>[] y = new Operation[1];

		final CD_FindStdLib findStdLib = (CD_FindStdLib) ocrfsld.success();
		findStdLib.findStdLib(st,
							  Compilation.CompilationAlways.defaultPrelude(),
							  (x1) -> y[0] = x1);

		final Operation<CompilerInstructions> x = y[0];
		if (x.mode() == Mode.FAILURE) {
			compilationRunner.compilation.getErrSink().exception(x.failure());

			// NOTE huh!
			return Operation.failure(x.failure());
		}

		aO.logProgress(130, "GEN_LANG: " + x.success().genLang());

		return Operation.success(true);
	}

	@Override
	public String name() {
		return "find stdlib";
	}
}
