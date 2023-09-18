package tripleo.elijah.comp;

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
	public void execute() {
		aa.execute(crState);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public ICompilationBus.OutputString[] outputStrings() {
		return new ICompilationBus.OutputString[0];
	}
}
