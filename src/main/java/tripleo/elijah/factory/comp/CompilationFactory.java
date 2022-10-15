package tripleo.elijah.factory.comp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.testing.comp.IFunctionMapHook;

import java.util.List;

public class CompilationFactory {

	@Contract("_, _ -> new")
	public static @NotNull CompilationImpl mkCompilation(final ErrSink eee, final IO io) {
		return new CompilationImpl(eee, io);
	}

	public static CompilationImpl mkCompilation2(List<IFunctionMapHook> aMapHooks) {
		final StdErrSink errSink = new StdErrSink();
		final IO io = new IO();

		final CompilationImpl c = mkCompilation(errSink, io);

		c.testMapHooks(aMapHooks);

		return c;
	}
}
