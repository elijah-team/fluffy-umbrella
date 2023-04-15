package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.ICompilationAccess;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.comp.Stages;

public class StageToRuntime {
	@Contract("_, _, _ -> new")
	@NotNull
	public static RuntimeProcesses get(final @NotNull Stages stage,
	                                   final @NotNull ICompilationAccess ca,
	                                   final @NotNull ProcessRecord aPr) {
		final RuntimeProcesses r = new RuntimeProcesses(ca, aPr);

		r.add(stage.getProcess(ca, aPr));

		return r;
	}
}
