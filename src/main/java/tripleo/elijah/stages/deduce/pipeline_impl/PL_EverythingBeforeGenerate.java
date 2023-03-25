package tripleo.elijah.stages.deduce.pipeline_impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.PipelineLogic;

class PL_EverythingBeforeGenerate implements PipelineLogicRunnable {
	private final DeducePipelineImpl deducePipeline;

	public PL_EverythingBeforeGenerate(final DeducePipelineImpl aDeducePipeline) {
		deducePipeline = aDeducePipeline;
	}

	@Override
	public void run(final @NotNull PipelineLogic pipelineLogic) {
		pipelineLogic.everythingBeforeGenerate(deducePipeline.lgc); // FIXME inline
	}
}
