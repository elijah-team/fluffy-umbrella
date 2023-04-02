package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.ICompilationAccess;
import tripleo.elijah.comp.ProcessRecord;
import tripleo.elijah.comp.Stages;
import tripleo.elijah.comp.i.RuntimeProcess;

public class RuntimeProcesses {
	private final ICompilationAccess ca;
	private final ProcessRecord      pr;
	private       RuntimeProcess     process;

	public RuntimeProcesses(final @NotNull ICompilationAccess aca, final @NotNull ProcessRecord aPr) {
		ca = aca;
		pr = aPr;
	}

	public void add(final RuntimeProcess aProcess) {
		process = aProcess;
	}

	public int size() {
		return process == null ? 0 : 1;
	}

	public void run_better() throws Exception {
		// do nothing. job over
		if (ca.getStage() == Stages.E) return;

		// rt.prepare();
		System.err.println("***** RuntimeProcess [prepare] named " + process);
		process.prepare();

		// rt.run();
		System.err.println("***** RuntimeProcess [run    ] named " + process);
		process.run(ca.getCompilation());

		// rt.postProcess(pr);
		System.err.println("***** RuntimeProcess [postProcess] named " + process);
		process.postProcess();

		System.err.println("***** RuntimeProcess^ [postProcess/writeLogs]");
		pr.writeLogs(ca);
	}
}

//
//
//
