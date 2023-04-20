package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;

import java.text.MessageFormat;
import java.util.List;

public class DebugBuffersLogic {
	public static void debug_buffers_logic(final @NotNull GenerateResult result, final XPrintStream db_stream) {
		final List<GenerateResultItem> generateResultItems = result.results();
		debug_buffers_logic(generateResultItems, db_stream);
	}

	public static void debug_buffers_logic(final @NotNull List<GenerateResultItem> generateResultItems,
										   final XPrintStream db_stream) {
		for (final GenerateResultItem ab : generateResultItems) {
			final String s = MessageFormat.format("{0} - {1} - {2}", ab.counter, ab.ty, ab.output);

			db_stream.println("---------------------------------------------------------------");
			db_stream.println(s);
			db_stream.println(ab.node.identityString());
			db_stream.println(ab.buffer.getText());
			db_stream.println("---------------------------------------------------------------");
		}
	}

}
