package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.CB_OutputString;

import java.util.List;

public interface CB_Output {
	@NotNull List<CB_OutputString> get();

	void logProgress(int number, String text);

	void print(String s);
}
