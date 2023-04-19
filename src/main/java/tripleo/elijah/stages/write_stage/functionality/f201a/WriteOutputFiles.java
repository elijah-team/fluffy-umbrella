package tripleo.elijah.stages.write_stage.functionality.f201a;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.IO;
import tripleo.elijah.stages.gen_c.OutputFileC;
import tripleo.elijah.stages.write_stage.pipeline_impl.WritePipelineSharedState;
import tripleo.elijah.util.NotImplementedException;
import tripleo.util.io.DisposableCharSink;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;

public class WriteOutputFiles {
	public void writeOutputFiles(final @NotNull WritePipelineSharedState sharedState, final @NotNull Map<String, OutputFileC> outputFileMap) {
		final IO io = sharedState.c.getIO();

		final ErrSink errSink = sharedState.c.getErrSink();
		final String  prefix  = sharedState.file_prefix.toString();

		NotImplementedException.raise();

		for (final Map.Entry<String, OutputFileC> entry : outputFileMap.entrySet()) {
			final OutputFileC value = entry.getValue();
			final String      key   = entry.getKey();

			writeOutputFile(io, errSink, prefix, value, key);
		}
	}

	private /*static*/ void writeOutputFile(final IO io,
											final ErrSink errSink,
											final String prefix,
											final OutputFileC value,
											final String key) {
		assert key != null;

		final Path path = FileSystems.getDefault().getPath(prefix, key);
		boolean    made = path.getParent().toFile().mkdirs();

		// TODO functionality
		System.out.println("201a Writing path: " + path);
		try (DisposableCharSink x = io.openWrite(path)) {
			x.accept(value.getOutput());
		} catch (Exception aE) {
			errSink.exception(aE);
		}
	}
}
