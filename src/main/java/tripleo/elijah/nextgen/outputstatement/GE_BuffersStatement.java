package tripleo.elijah.nextgen.outputstatement;

import tripleo.elijah.util.Helpers;
import tripleo.util.buffer.Buffer;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class GE_BuffersStatement implements EG_Statement {
	private final Map.Entry<String, Collection<Buffer>> entry;

	public GE_BuffersStatement(final Map.Entry<String, Collection<Buffer>> aEntry) {
		entry = aEntry;
	}

	@Override
	public String getText() {
		return Helpers.String_join("\n\n", entry.getValue()
		                                        .stream()
		                                        .map(buffer -> buffer.getText())
		                                        .collect(Collectors.toList()));
	}

	@Override
	public EX_Explanation getExplanation() {
		return new GE_BuffersExplanation(this);
	}

	private static class GE_BuffersExplanation implements EX_Explanation {
		final         String              message = "buffers to statement";
		private final GE_BuffersStatement st;

		public GE_BuffersExplanation(final GE_BuffersStatement aGEBuffersStatement) {
			st = aGEBuffersStatement;
		}

		public String getText() {
			return message;
		}
	}
}
