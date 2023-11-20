package tripleo.elijah.stages.gen_generic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DoubleLatch<T> {
	private final @NotNull Consumer<T> action;
	private                T           tt;
	private                boolean     simple;

	//private IincInsnNode action;

	@Contract(pure = true)
	public DoubleLatch(final Consumer<T> aAction) {
		action = aAction;
	}

	public void notify(final T att) {
		tt = att;
		if (simple && tt != null) {
			action.accept(tt);
		}
	}

	public void notify(final boolean ass) {
		simple = ass;
		if (simple && tt != null) {
			action.accept(tt);
		}
	}
}
