package tripleo.elijah.comp;

import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.internal.CB_Output;
import tripleo.elijah.comp.internal.CR_State;

class CR_ProcessInitialAction implements CR_Action {
	private final CompilationRunner    compilationRunner;
	private final CompilerInstructions ci;
	private final boolean              do_out;

	public CR_ProcessInitialAction(final CompilationRunner aCompilationRunner, final CompilerInstructions aCi, final boolean aDo_out) {
		compilationRunner = aCompilationRunner;
		ci                = aCi;
		do_out            = aDo_out;
	}

	@Override
	public void attach(final CompilationRunner cr) {

	}

	@Override
	public void execute(final CR_State st, final CB_Output aO) {
		try {
			compilationRunner.compilation.use(ci, do_out);
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
