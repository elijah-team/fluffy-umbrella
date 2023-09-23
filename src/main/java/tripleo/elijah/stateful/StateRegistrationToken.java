package tripleo.elijah.stateful;

import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class StateRegistrationToken {
	private final int token;

	StateRegistrationToken(int token) {
		this.token = token;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		StateRegistrationToken that = (StateRegistrationToken) obj;
		return this.token == that.token;
	}

	@Override
	public int hashCode() {
		return Objects.hash(token);
	}

	public int token() {
		return token;
	}

	@Contract(pure = true)
	@Override
	public @NotNull String toString() {
		return "StateRegistrationToken[" +
				"token=" + token + ']';
	}

}
