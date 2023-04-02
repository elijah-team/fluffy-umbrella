package tripleo.elijah.comp.internal;

import tripleo.elijah.comp.*;

public class CR_State {
	private final CompilationRunner         compilationRunner;
	public        ICompilationBus.CB_Action cur;
	ICompilationAccess ca;
	public ProcessRecord    pr;
	public RuntimeProcesses rt;

	public CR_State(final CompilationRunner aCompilationRunner) {
		compilationRunner = aCompilationRunner;
	}

	public ICompilationAccess ca() {
		if (ca == null) {
			ca = new DefaultCompilationAccess(compilationRunner.compilation);
			pr = new ProcessRecord(ca);
		}

		return ca;
	}
}
