package tripleo.elijah.comp.i;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.deduce.post_bytecode.Maybe;

public interface CCI {
	void accept(@NotNull Maybe<ILazyCompilerInstructions> mcci, IProgressSink aPs);
}
