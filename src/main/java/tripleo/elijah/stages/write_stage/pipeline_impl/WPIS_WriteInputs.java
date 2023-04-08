package tripleo.elijah.stages.write_stage.pipeline_impl;

import tripleo.elijah.comp.WritePipeline;
import tripleo.util.buffer.DefaultBuffer;

import java.io.*;

public class WPIS_WriteInputs implements WP_Indiviual_Step {
	private final WritePipeline writePipeline;

	public WPIS_WriteInputs(final WritePipeline aWritePipeline) {
		writePipeline = aWritePipeline;
	}

	@Override
	public void act(final WritePipelineSharedState st, final WP_State_Control sc) {
		// 3. write inputs
		// TODO ... 1/ output(s) per input and 2/ exceptions ... and 3/ plan
		//  "plan", effects; input(s), output(s)
		// TODO flag?
		try {
			final String        fn1 = new File(st.file_prefix, "inputs.txt").toString();
			final DefaultBuffer buf = new DefaultBuffer("");

			for (File file : st.c.getIO().recordedreads) {
				final String fn = file.toString();

				writePipeline.append_hash(buf, fn, st.c.getErrSink());
			}
			String s = buf.getText();
			Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fn1, true)));
			w.write(s);
			w.close();
		} catch (IOException aE) {
			throw new RuntimeException(aE);
		}
	}
}
