package tripleo.elijah.stages.deduce.post_bytecode;

import tripleo.elijah.diagnostic.Diagnostic;

public class Maybe<T> {
	public final T          o;
	public final Diagnostic exc;

	public Maybe(T o, Diagnostic exc) {
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

	public boolean isException() {
		return exc != null;
	}
}
