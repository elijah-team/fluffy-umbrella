package tripleo.elijah.stages.write_stage.pipeline_impl;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_generic.GenerateResult;

public class WPIS_GenerateOutputs implements WP_Indiviual_Step {
	private final WPIS_GenerateOutputs_Behavior_PrintDBLString printDBLString;

	@Contract(pure = true)
	public WPIS_GenerateOutputs() {
		// 1. GenerateOutputs with ElSystem
		printDBLString = new Default_WPIS_GenerateOutputs_Behavior_PrintDBLString();
	}

	@Contract(pure = true)
	public WPIS_GenerateOutputs(final @NotNull WPIS_GenerateOutputs.WPIS_GenerateOutputs_Behavior_PrintDBLString aPrintDBLString) {
		// 1. GenerateOutputs with ElSystem
		printDBLString = aPrintDBLString;
	}

	@Override
	public void act(final @NotNull WritePipelineSharedState st, final WP_State_Control sc) {
		Preconditions.checkState(st.getGr() != null);
		Preconditions.checkState(st.sys != null);

		GenerateResult result = st.getGr();

		final SPrintStream sps = new SPrintStream();

		DebugBuffersLogic.debug_buffers_logic(result, sps);

		printDBLString.print(sps.getString());

		st.sys.generateOutputs(result);
	}

	@FunctionalInterface
	public interface WPIS_GenerateOutputs_Behavior_PrintDBLString {
		void print(String sps);
	}

	static class Default_WPIS_GenerateOutputs_Behavior_PrintDBLString implements WPIS_GenerateOutputs_Behavior_PrintDBLString {
		@Override
		public void print(final String sps) {
			System.err.println(sps);
		}
	}

}
