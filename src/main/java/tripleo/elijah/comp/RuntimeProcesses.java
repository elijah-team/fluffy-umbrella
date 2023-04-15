package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.RuntimeProcess;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.util.Stupidity;

import java.util.ArrayList;
import java.util.List;

public class RuntimeProcesses {
	private final List<RuntimeProcess> processes = new ArrayList<>();
	private final ICompilationAccess ca;
	private final ProcessRecord      pr;

	public RuntimeProcesses(final @NotNull ICompilationAccess aca, final @NotNull ProcessRecord aPr) {
		ca = aca;
		pr = aPr;
	}

	public void run() {
		final Compilation comp = ca.getCompilation();

		for (RuntimeProcess runtimeProcess : processes) {
			Stupidity.println_out_2("***** RuntimeProcess [run    ] named " + runtimeProcess);
			runtimeProcess.run(comp);
		}
	}

	public void prepare() throws Exception {
		for (RuntimeProcess runtimeProcess : processes) {
			Stupidity.println_out_2("***** RuntimeProcess [prepare] named " + runtimeProcess);
			runtimeProcess.prepare();
		}
	}

	public void add(final RuntimeProcess aProcess) {
		processes.add(aProcess);
	}

	public void postProcess(ProcessRecord pr) {
		for (RuntimeProcess runtimeProcess : processes) {
			Stupidity.println_out_2("***** RuntimeProcess [postProcess] named " + runtimeProcess);
			runtimeProcess.postProcess();
		}

		Stupidity.println_out_2("***** RuntimeProcess^ [postProcess/writeLogs]");
		pr.writeLogs(ca);
	}

	public int size() {
		return processes.size();
	}

	public void run_better() {
		// do nothing. job over
		switch (ca.getCompilation().stage) {
		case E -> {
			return;
		}
		default -> {
			final RuntimeProcesses rt = this;

			try {
				rt.prepare();
			} catch (Exception aE) {
				throw new RuntimeException(aE);
			}
			rt.run();
			rt.postProcess(pr);
		}
		}
	}
}
