package tripleo.elijah.util;

import static tripleo.elijah.nextgen.query.Mode.FAILURE;
import static tripleo.elijah.nextgen.query.Mode.SUCCESS;

import org.jetbrains.annotations.NotNull;

import tripleo.elijah.nextgen.query.Mode;

/**
 * An emulation of Rust's Result type
 *
 * @param <T> the success type
 */
public class Operation<T> {
	public static <T> @NotNull Operation<T> failure(final Exception aException) {
		final Operation<T> op = new Operation<>(null, aException, FAILURE);
		return op;
	}
	public static <T> @NotNull Operation<T> success(final T aSuccess) {
		final Operation<T> op = new Operation<>(aSuccess, null, SUCCESS);
		return op;
	}

	private final Mode mode;

	private final T    succ;

	private final Exception exc;

	public Operation(final T aSuccess, final Exception aException, final Mode aMode) {
		succ = aSuccess;
		exc  = aException;
		mode = aMode;

		assert succ != exc;
	}

	public Exception failure() {
		return exc;
	}

	public Mode mode() {
		return mode;
	}

	public T success() {
		return succ;
	}
}
