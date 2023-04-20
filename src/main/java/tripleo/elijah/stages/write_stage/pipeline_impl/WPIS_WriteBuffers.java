package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.elijah.stages.write_stage.functionality.f301.WriteBufferText;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class WPIS_WriteBuffers implements WP_Indiviual_Step {
	private final WritePipeline writePipeline;

	@Contract(pure = true)
	public WPIS_WriteBuffers(final WritePipeline aWritePipeline) {
		writePipeline = aWritePipeline;
	}

	@Override
	public void act(final @NotNull WritePipelineSharedState st, final WP_State_Control sc) {
		// 5. write buffers
		// TODO flag?
		try {
			st.file_prefix.mkdirs();

			debug_buffers();
		} catch (FileNotFoundException aE) {
			sc.exception(aE);
		}
	}

	private void debug_buffers() throws FileNotFoundException {
		// TODO can/should this fail??

		final List<GenerateResultItem> generateResultItems1 = writePipeline.st.getGr().results();

		writePipeline.prom.then(new DoneCallback<GenerateResult>() {
			@Override
			public void onDone(final GenerateResult result) {
				final File file = new File(writePipeline.st.file_prefix, "buffers.txt");

				WriteBufferText wbt = new WriteBufferText();
				wbt.setFile(file);
				wbt.setResult(result);
				wbt.run();
			}
		});
	}
}
