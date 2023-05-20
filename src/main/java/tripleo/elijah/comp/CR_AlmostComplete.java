package tripleo.elijah.comp;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.comp.internal.CB_Output;
import tripleo.elijah.comp.internal.CR_State;

public class CR_AlmostComplete implements CR_Action {
	private final CompilationRunner compilationRunner;

	@Contract(pure = true)
	public CR_AlmostComplete(final CompilationRunner aCompilationRunner) {
		compilationRunner = aCompilationRunner;
	}

	@Override
	public void attach(final CompilationRunner cr) {

	}

	@Override
	public Operation<Boolean> execute(final CR_State st, final CB_Output aO) {
		compilationRunner.cis.almostComplete();
		return Operation.success(true);
	}

	@Override
	public String name() {
		return "cis almostComplete";
	}
}
