package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.IProgressSink;
import tripleo.elijah.comp.i.ProgressSinkComponent;

public class DefaultProgressSink implements IProgressSink {
	@Override
	public void note(final int code, final @NotNull ProgressSinkComponent component, final int type, final Object[] params) {
//		component.note(code, type, params);
		if (component.isPrintErr(code, type)) {
			final String s = component.printErr(code, type, params);
			tripleo.elijah.util.Stupidity.println_err_2(s);
		}
	}
}
