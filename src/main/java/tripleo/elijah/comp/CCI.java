package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.IProgressSink;
import tripleo.elijah.stages.deduce.post_bytecode.Maybe;

public interface CCI {
	void accept(@NotNull Maybe<ILazyCompilerInstructions> mcci, IProgressSink aPs);
}
