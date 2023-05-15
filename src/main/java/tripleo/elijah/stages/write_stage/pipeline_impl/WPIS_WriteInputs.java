package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.nextgen.outputstatement.EG_Naming;
import tripleo.elijah.nextgen.outputstatement.EG_SequenceStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.nextgen.outputtree.EOT_OutputType;
import tripleo.util.buffer.DefaultBuffer;

import java.io.*;

import static tripleo.elijah.util.Helpers.List_of;

public class WPIS_WriteInputs implements WP_Indiviual_Step {
	private final WritePipeline writePipeline;

	@Contract(pure = true)
	public WPIS_WriteInputs(final WritePipeline aWritePipeline) {
		writePipeline = aWritePipeline;
	}

	@Override
	public void act(final @NotNull WritePipelineSharedState st, final WP_State_Control sc) {
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

			final @NotNull EOT_OutputTree ot = st.c.getOutputTree();

			final EG_SequenceStatement seq = new EG_SequenceStatement(new EG_Naming("<<WPIS_WriteInputs>>"), List_of(
					new EG_Statement() {
						@Override
						public String getText() {
							return s;
						}

						@Override
						public EX_Explanation getExplanation() {
							return () -> "<<WPIS_WriteInputs>> >> statement";
						}
					}
																													));
			final EOT_OutputFile off = new EOT_OutputFile(st.c, List_of(), fn1, EOT_OutputType.LOGS, seq);

			ot.add(off);

		} catch (IOException aE) {
			throw new RuntimeException(aE);
		}
	}
}
