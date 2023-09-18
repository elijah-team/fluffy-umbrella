package tripleo.elijah.comp.i;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CB_Action {
	void execute(CB_Monitor monitor);

	String name();

	@Nullable List<CB_OutputString> outputStrings();

}
