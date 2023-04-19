package tripleo.elijah.comp;

import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.nextgen.query.Mode;

import static tripleo.elijah.nextgen.query.Mode.FAILURE;
import static tripleo.elijah.nextgen.query.Mode.SUCCESS;

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

	public static <T> tripleo.elijah.comp.Operation2<T> failure(final Diagnostic aException) {
		final Operation2<T> op = new tripleo.elijah.comp.Operation2<>(null, aException, FAILURE);
		return op;
	}

	public static <T> tripleo.elijah.comp.Operation2<T> success(final T aSuccess) {
		final Operation2<T> op = new tripleo.elijah.comp.Operation2<>(aSuccess, null, SUCCESS);
		return op;
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
