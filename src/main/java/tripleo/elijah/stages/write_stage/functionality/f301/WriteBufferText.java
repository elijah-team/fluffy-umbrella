package tripleo.elijah.stages.write_stage.functionality.f301;

import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.write_stage.pipeline_impl.DebugBuffersLogic;
import tripleo.elijah.stages.write_stage.pipeline_impl.XXPrintStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class WriteBufferText {
	private GenerateResult result;
	private File           file;

	public void run() {
		PrintStream db_stream = null;

		try {
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

	public void setFile(final File aFile) {
		file = aFile;
	}

	public void setResult(final GenerateResult aResult) {
		result = aResult;
	}
}
