package tripleo.elijah.stages.write_stage.pipeline_impl;

import tripleo.elijah.comp.WritePipeline;

public interface WP_Indiviual_Step {
	void act(final WritePipeline.WritePipelineSharedState st, final WP_State_Control sc);
}
