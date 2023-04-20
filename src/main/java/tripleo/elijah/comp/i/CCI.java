package tripleo.elijah.comp.i;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.util.Maybe;

public interface CCI {
	void accept(@NotNull Maybe<ILazyCompilerInstructions> mcci, IProgressSink aPs);
}
