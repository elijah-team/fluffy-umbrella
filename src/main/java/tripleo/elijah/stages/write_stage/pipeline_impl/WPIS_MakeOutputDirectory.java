package tripleo.elijah.stages.write_stage.pipeline_impl;

import tripleo.elijah.comp.WritePipeline;

public class WPIS_MakeOutputDirectory implements WP_Indiviual_Step {

	@Override
	public void act(final WritePipeline.WritePipelineSharedState st, final WP_State_Control sc) {
		// 2. make output directory
		// TODO check first
		boolean made = st.file_prefix.mkdirs();
	}
}
