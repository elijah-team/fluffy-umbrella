package tripleo.elijah.stages.write_stage.pipeline_impl;

import tripleo.elijah.stages.gen_generic.GenerateResult;

public class WPIS_GenerateOutputs implements WP_Indiviual_Step {

	private final GenerateResult result;

	public WPIS_GenerateOutputs(final GenerateResult aResult) {
		// 1. GenerateOutputs with ElSystem
		result = aResult;
	}

	@Override
	public void act(final WritePipelineSharedState st, final WP_State_Control sc) {


		final SPrintStream sps = (SPrintStream) getPrintStream();
		DebugBuffersLogic.debug_buffers_logic(result, sps);

		if (_printing()) {
			System.err.println(sps.getString());
		}


		st.sys.generateOutputs(result);
	}

	public boolean _printing() {
		return true;
	}

	public XPrintStream getPrintStream() {
		final SPrintStream sps = new SPrintStream();
		return sps;
	}
}
