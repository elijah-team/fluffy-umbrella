package tripleo.elijah.comp;

import tripleo.elijah.comp.i.CB_Monitor;
import tripleo.elijah.comp.i.CB_OutputString;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

class ActionWrapper2 implements ICompilationBus.CB_Action {
	private final CompilationRunner.CR_Action aa;
	private final String                      name;
	private final CompilationRunner.CR_State  crState;

	ActionWrapper2(final CompilationRunner.CR_Action aAa, final String aName, final CompilationRunner.CR_State aCrState) {
		aa      = aAa;
		name    = aName;
		crState = aCrState;
	}

	@Override
	public void execute(final CB_Monitor aMonitor) {
		aa.execute(crState);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public List<CB_OutputString> outputStrings() {
		return List_of();
	}
}
