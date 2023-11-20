package tripleo.elijah.nextgen.query;

import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.diagnostic.ExceptionDiagnostic;
import tripleo.elijah.diagnostic.Diagnostic;

/**
 * An emulation of Rust's Result type
 *
 * @param <T> the success type
 */
public class Operation2<T> {
	private final T          succ;
	private final Diagnostic exc;
	private final Mode       mode;

	public Operation2(final T aSuccess, final Diagnostic aException, final Mode aMode) {
		succ = aSuccess;
		exc  = aException;
		mode = aMode;

		assert succ != exc;
	}

	public static <T> Operation2<T> failure(final Diagnostic aException) {
		final Operation2<T> op = new Operation2<>(null, aException, Mode.FAILURE);
		return op;
	}

	public static <T> Operation2<T> success(final T aSuccess) {
		final Operation2<T> op = new Operation2<>(aSuccess, null, Mode.SUCCESS);
		return op;
	}

	public static <TT> Operation2<TT> convert(final Operation<TT> aOperation) {
		switch (aOperation.mode()) {
		case SUCCESS -> {
			return Operation2.success(aOperation.success());
		}
		case FAILURE -> {
			return Operation2.failure(new ExceptionDiagnostic(aOperation.failure()));
		}
		default -> throw new IllegalStateException("Unexpected value: " + aOperation.mode());
		}
	}

	public Mode mode() {
		return mode;
	}

	public T success() {
		return succ;
	}

	public Diagnostic failure() {
		return exc;
	}
}
