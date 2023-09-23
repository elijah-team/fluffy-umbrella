package tripleo.elijah.comp;

import tripleo.elijah.comp.i.CB_Monitor;
import tripleo.elijah.comp.i.CB_OutputString;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

class ActionWrapper implements ICompilationBus.CB_Action {
	private final CompilationRunner.CR_Action a;
	private final CompilationRunner.CR_State  crState;

	ActionWrapper(final CompilationRunner.CR_Action aAction, final CompilationRunner.CR_State aCrState) {
		a       = aAction;
		crState = aCrState;
	}

	@Override
	public void execute(final CB_Monitor aMonitor) {
		crState.cur = this;
		a.execute(crState);
		crState.cur = null;
	}

	@Override
	public String name() {
		return a.name();
	}

	@Override
	public List<CB_OutputString> outputStrings() {
		return List_of();
	}
}
