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
		tripleo.elijah.util.Stupidity.println_err_2("***** RuntimeProcess [prepare] named " + process);
		process.prepare();

		// rt.run();
		tripleo.elijah.util.Stupidity.println_err_2("***** RuntimeProcess [run    ] named " + process);
		process.run(ca.getCompilation());

		// rt.postProcess(pr);
		tripleo.elijah.util.Stupidity.println_err_2("***** RuntimeProcess [postProcess] named " + process);
		process.postProcess();

		tripleo.elijah.util.Stupidity.println_err_2("***** RuntimeProcess^ [postProcess/writeLogs]");
		pr.writeLogs(ca);
	}
}

//
//
//
