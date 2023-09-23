package tripleo.elijah.comp;

import tripleo.elijah.nextgen.query.Mode;

import static tripleo.elijah.nextgen.query.Mode.FAILURE;
import static tripleo.elijah.nextgen.query.Mode.SUCCESS;

/**
 * An emulation of Rust's Result type
 *
 * @param <T> the success type
 */
public class Operation<T> {
	private final T         succ;
	private final Exception exc;
	private final Mode      mode;

	public Operation(final T aSuccess, final Exception aException, final Mode aMode) {
		succ = aSuccess;
		exc  = aException;
		mode = aMode;

		assert succ != exc;
	}

	public static <T> Operation<T> failure(final Exception aException) {
		final Operation<T> op = new Operation<>(null, aException, FAILURE);
		return op;
	}

	public static <T> Operation<T> success(final T aSuccess) {
		final Operation<T> op = new Operation<>(aSuccess, null, SUCCESS);
		return op;
	}

	public Mode mode() {
		return mode;
	}

	public T success() {
		return succ;
	}

	public Exception failure() {
		return exc;
	}
}
