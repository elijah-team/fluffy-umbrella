package tripleo.elijah.comp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.comp.internal.ProcessRecord;

public class StageToRuntime {
	@Contract("_, _, _, _ -> new")
	@NotNull
	public static RuntimeProcesses get(final @NotNull Stages stage,
									   final @NotNull ICompilationAccess ca,
									   final @NotNull ProcessRecord aPr, final IPipelineAccess aPa) {
		final RuntimeProcesses r = new RuntimeProcesses(ca, aPr);

		r.add(stage.getProcess(ca, aPr));

		return r;
	}
}
