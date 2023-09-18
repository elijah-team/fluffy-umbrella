package tripleo.elijah.comp;

class ActionWrapper implements ICompilationBus.CB_Action {
	private final CompilationRunner.CR_Action a;
	private final CompilationRunner.CR_State  crState;

	ActionWrapper(final CompilationRunner.CR_Action aAction, final CompilationRunner.CR_State aCrState) {
		a       = aAction;
		crState = aCrState;
	}

	@Override
	public void execute() {
		crState.cur = this;
		a.execute(crState);
		crState.cur = null;
	}

	@Override
	public String name() {
		return a.name();
	}

	@Override
	public ICompilationBus.OutputString[] outputStrings() {
		return new ICompilationBus.OutputString[0];
	}
}
