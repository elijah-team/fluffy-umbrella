package tripleo.elijah.stages.write_stage.functionality.f301;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.write_stage.pipeline_impl.*;

public class WriteBufferText {
	private final WritePipelineSharedState st;
	private final WP_State_Control sc;
	private GenerateResult result;

	@Contract(pure = true)
	public WriteBufferText(final WritePipelineSharedState aSt, final WP_State_Control aSc) {
		st = aSt;
		sc = aSc;
	}

	public void run() {
		final WPIS_GenerateOutputs wgo = new WPIS_GenerateOutputs(new NonPrintingBehavior());

		wgo.act(st, sc);
	}

	public void setResult(final GenerateResult aResult) {
		result = aResult;
	}

	private static class NonPrintingBehavior implements WPIS_GenerateOutputs.WPIS_GenerateOutputs_Behavior_PrintDBLString {
		@Override
		public void print(final String sps) {
			// NOTE This was puprosely created to NOT print
			//System.err.println(sps);
		}
	}
}
