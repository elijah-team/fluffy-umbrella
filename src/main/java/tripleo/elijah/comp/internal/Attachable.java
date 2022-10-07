package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.CompilationShit;

public interface Attachable {
	void attachCB(@NotNull CompilationShit cs);
}
