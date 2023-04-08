package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.jdeferred2.DoneCallback;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

public class WPIS_WriteBuffers implements WP_Indiviual_Step {
	private final WritePipeline writePipeline;

	public WPIS_WriteBuffers(final WritePipeline aWritePipeline) {
		writePipeline = aWritePipeline;
	}

	@Override
	public void act(final WritePipelineSharedState st, final WP_State_Control sc) {
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
				PrintStream db_stream = null;

				try {
					final File file = new File(writePipeline.st.file_prefix, "buffers.txt");
					db_stream = new PrintStream(file);
					XXPrintStream xps = new XXPrintStream(db_stream);

					DebugBuffersLogic.debug_buffers_logic(result, xps);
				} catch (FileNotFoundException aE) {
					throw new RuntimeException(aE);
				} finally {
					if (db_stream != null)
						db_stream.close();
				}
			}


		});
	}
}
