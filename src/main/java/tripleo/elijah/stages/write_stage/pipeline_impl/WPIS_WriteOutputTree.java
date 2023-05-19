package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.comp.functionality.f203.F203;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.util.io.CharSink;
import tripleo.util.io.FileCharSink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class WPIS_WriteOutputTree implements WP_Indiviual_Step {
	@Contract(pure = true)
	public WPIS_WriteOutputTree(final WritePipeline aWritePipeline) {
	}

	@Override
	public void act(final @NotNull WritePipelineSharedState st, final WP_State_Control sc) {
		final EOT_OutputTree       ot = st.c.getOutputTree();
		final List<EOT_OutputFile> l  = ot.getList();

		for (final EOT_OutputFile outputFile : l) {
			final String       path0 = outputFile.getFilename();
			String             path;
			final EG_Statement seq   = outputFile.getStatementSequence();

			switch (outputFile.getType()) {
			case SOURCES -> {
				path = "COMP///code/" + path0;
			}
			case LOGS -> {
				path = "COMP///logs/" + path0;
			}
			default -> path = path0;
			}

			System.out.println("401 Writing path: " + path);
			CharSink x = null;
			try {
				x = st.c.getIO().openWrite(Path.of(path));
				x.accept(seq.getText());
			} catch (IOException aE) {
				sc.exception(aE);
			} finally {
				if (x != null)
					((FileCharSink) x).close();
			}
		}
	}

	private @NotNull File choose_dir_name(final @NotNull Compilation c) {
		final File fn00 = new F203(c.getErrSink(), c).chooseDirectory();
		final File fn01 = new File(fn00, "code");

		return fn01;
	}
}
