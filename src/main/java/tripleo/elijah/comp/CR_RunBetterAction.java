package tripleo.elijah.comp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.comp.internal.CB_Output;
import tripleo.elijah.comp.internal.CR_State;
import tripleo.elijah.comp.internal.RuntimeProcesses;

public class CR_RunBetterAction implements CR_Action {
	@Override
	public void attach(final CompilationRunner cr) {

	}

	@Override
	public Operation<Boolean> execute(final CR_State st, final CB_Output aO) {
		try {
			final ICompilationAccess ca = st.ca();

			st.rt = StageToRuntime.get(ca.getStage(), ca, ca.getCompilation().pa());
			st.rt.run_better();

			return Operation.success(true);
		} catch (final Exception aE) {
			return Operation.failure(aE);
		}
	}

	@Override
	public String name() {
		return "run better";
	}

	public class StageToRuntime {
		@Contract("_, _, _, _ -> new")
		@NotNull
		public static RuntimeProcesses get(final @NotNull Stages stage,
										   final @NotNull ICompilationAccess ca,
										   final @NotNull IPipelineAccess aPa) {
			final RuntimeProcesses r = new RuntimeProcesses(ca, aPa.getProcessRecord());

			r.add(stage.getProcess(ca, aPa.getProcessRecord()));

			return r;
		}
	}
}
