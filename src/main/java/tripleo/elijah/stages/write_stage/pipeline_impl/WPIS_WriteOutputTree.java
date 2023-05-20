package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.nextgen.outputstatement.EG_Naming;
import tripleo.elijah.nextgen.outputstatement.EG_SequenceStatement;
import tripleo.elijah.nextgen.outputstatement.EG_SingleStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputtree.EOT_OutputFile;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.nextgen.outputtree.EOT_OutputType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.stages.logging.LogEntry;
import tripleo.util.io.CharSink;
import tripleo.util.io.FileCharSink;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

public class WPIS_WriteOutputTree implements WP_Indiviual_Step {
	@Override
	public void act(final @NotNull WritePipelineSharedState st, final WP_State_Control sc) {
		final EOT_OutputTree       ot = st.c.getOutputTree();
		final List<EOT_OutputFile> l  = ot.getList();

		//
		//
		//
		//
		//
		//
		//
		// HACK should be done earlier in process
		//
		//
		//
		//
		//
		//
		//
		final List<ElLog> logs = st.pa.getCompilationEnclosure().getPipelineLogic().elLogs;
		final String s1 = logs.get(0).getFileName();

		for (final ElLog log : logs) {
			final List<EG_Statement> stmts = new ArrayList<>();

			if (log.getEntries().size() == 0) continue;

			for (final LogEntry entry : log.getEntries()) {
				final String logentry = String.format("[%s] [%tD %tT] %s %s", s1, entry.time, entry.time, entry.level, entry.message);
				stmts.add(new EG_SingleStatement(logentry));
			}

			final EG_SequenceStatement seq      = new EG_SequenceStatement(new EG_Naming("wot.log.seq"), stmts);
			final String               fileName = new String(log.getFileName()).replace("/", "~~");
			final EOT_OutputFile       off      = new EOT_OutputFile(st.c, List_of(), fileName, EOT_OutputType.LOGS, seq);
			l.add(off);
		}

		for (final EOT_OutputFile outputFile : l) {
			final String       path0 = outputFile.getFilename();
			String             path;
			final EG_Statement seq   = outputFile.getStatementSequence();

			switch (outputFile.getType()) {
			case SOURCES -> path = MessageFormat.format("{0}/code/{1}", st.base_dir, path0);
			case LOGS 	 -> path = MessageFormat.format("{0}/logs/{1}", st.base_dir, path0);
			case INPUTS  -> path = MessageFormat.format("{0}/inputs.txt", st.base_dir);
			default 	 -> path = path0;
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
}
