package tripleo.elijah.stages.deduce.pipeline_impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.lang.OS_Module;

class PL_AddModule implements PipelineLogicRunnable {
	private final OS_Module m;

	public PL_AddModule(final OS_Module aModule) {
		m = aModule;
	}

	@Override
	public void run(final @NotNull PipelineLogic pipelineLogic) {
		pipelineLogic.addModule(m);
	}
}
