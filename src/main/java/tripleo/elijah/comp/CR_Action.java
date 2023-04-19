package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.internal.CB_Output;
import tripleo.elijah.comp.internal.CR_State;

public interface CR_Action {
	void attach(@NotNull CompilationRunner cr);

	void execute(@NotNull CR_State st, CB_Output aO);

	String name();
}
