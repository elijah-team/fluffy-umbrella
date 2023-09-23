package tripleo.elijah.stages.gen_c;

import static tripleo.elijah.util.Helpers.String_join;

import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * (Unrealized) Intent: provide annotations in output code to show what generated text is generated from
 */
class SpecialText {
	@Contract("_ -> new")
	public static @NotNull SpecialText compose(final List<String> aStringList) {
		return new SpecialText(String_join(".", aStringList));
	}

	private final String text;

	public SpecialText(final String aText) {
		text = aText;
	}

	public String getText() {
		return text;
	}
}
