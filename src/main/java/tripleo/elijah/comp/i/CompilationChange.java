package tripleo.elijah.comp.i;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;

public interface CompilationChange {
	void apply(final Compilation c);
}

