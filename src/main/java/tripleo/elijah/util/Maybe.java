package tripleo.elijah.util;

import org.jetbrains.annotations.Nullable;
import tripleo.elijah.diagnostic.Diagnostic;

public class Maybe<T> {
	public final @Nullable Diagnostic exc;
	public final @Nullable T          o;

	public Maybe(final @Nullable T o, final Diagnostic exc) {
		if (o == null) {
			if (exc == null) {
				throw new IllegalStateException("Both o and exc are null!");
			}
		} else {
			if (exc != null) {
				throw new IllegalStateException("Both o and exc are null (2)!");
			}
		}

		this.o   = o;
		this.exc = exc;
	}

	public static <TT> Maybe<TT> empty() {
		return new Maybe<>(null, null);
	}

	public static <TT> Maybe<TT> of(final TT aTT) {
		return new Maybe<>(aTT, null);
	}

	public boolean isException() {
		return exc != null;
	}
}
