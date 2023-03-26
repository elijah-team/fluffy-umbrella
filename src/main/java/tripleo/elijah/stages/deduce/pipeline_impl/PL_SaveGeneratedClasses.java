package tripleo.elijah.stages.deduce.pipeline_impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.PipelineLogic;

class PL_SaveGeneratedClasses implements PipelineLogicRunnable {
	private final DeducePipelineImpl deducePipeline;

	public PL_SaveGeneratedClasses(final DeducePipelineImpl aDeducePipeline) {
		deducePipeline = aDeducePipeline;
	}

	@Override
	public void run(final @NotNull PipelineLogic pipelineLogic) {
		deducePipeline.saveGeneratedClasses(pipelineLogic.dp.generatedClasses.copy());
	}
}
