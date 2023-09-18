package tripleo.elijah.stateful;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public abstract class _RegistrationTarget {
	private final List<State> registeredStates = new ArrayList<>();

	public State registerState(final @NotNull State aState) {
		if (!(registeredStates.contains(aState))) {
			registeredStates.add(aState);

			final int id = registeredStates.indexOf(aState);

			aState.setIdentity(new StateRegistrationToken(id));
			return aState;
		}

		return aState;
	}
}
