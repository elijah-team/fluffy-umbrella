package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;

import java.text.MessageFormat;
import java.util.List;

public final class DebugBuffersLogic {
	@Contract(pure = true)
	private DebugBuffersLogic() {
	}

	public static void debug_buffers_logic(final @NotNull GenerateResult result, final XPrintStream db_stream) {
		final List<GenerateResultItem> generateResultItems = result.results();
		for (final GenerateResultItem ab : generateResultItems) {
			__debug_buffers_logic_each(db_stream, ab);
		}
	}

	public static void __debug_buffers_logic_each(final @NotNull XPrintStream db_stream, final @NotNull GenerateResultItem ab) {
		if (false) {
			final String s = MessageFormat.format("{0} - {1} - {2}", ab.counter, ab.ty, ab.output);

			db_stream.println("---------------------------------------------------------------");
			db_stream.println(s);
			db_stream.println(ab.node.identityString());
			db_stream.println(ab.buffer.getText());
			db_stream.println("---------------------------------------------------------------");
		}
	}

}
