package tripleo.elijah.stages.write_stage.functionality.f301;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.write_stage.pipeline_impl.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class WriteBufferText {
	private GenerateResult result;
	private File           file;

	public void run() {

		final PrintStream[] db_stream = {null};

		final WPIS_GenerateOutputs wgo = new WPIS_GenerateOutputs(result) {
			@Contract(pure = true)
			@Override
			public boolean _printing() {
				return false;
			}

			@Override
			public XPrintStream getPrintStream() {
				try {
					db_stream[0] = new PrintStream(file);
				} catch (FileNotFoundException aE) {
					//throw new RuntimeException(aE);
					return null;
				}
				XXPrintStream xps = new XXPrintStream(db_stream[0]);
				return xps;
			}

		};

		try {
			final XPrintStream xps = wgo.getPrintStream();

			if (xps != null) {
				DebugBuffersLogic.debug_buffers_logic(result, xps);
			}
		//} catch (FileNotFoundException aE) {
		//	throw new RuntimeException(aE);
		} finally {
			if (db_stream[0] != null)
				db_stream[0].close();
		}
	}

	public void setFile(final File aFile) {
		file = aFile;
	}

	public void setResult(final GenerateResult aResult) {
		result = aResult;
	}
}
