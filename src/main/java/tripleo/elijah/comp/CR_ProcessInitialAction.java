package tripleo.elijah.comp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructionsImpl;
import tripleo.elijah.comp.internal.CB_Output;
import tripleo.elijah.comp.internal.CR_State;

public class CR_ProcessInitialAction implements CR_Action {
	private       CompilationRunner        compilationRunner;
	private final CompilerInstructionsImpl ci;
	private final boolean                  do_out;

	@Contract(pure = true)
	public CR_ProcessInitialAction(final @NotNull CompilerInstructionsImpl aCi,
								   final boolean aDo_out) {
		ci                = aCi;
		do_out            = aDo_out;
	}

	@Override
	public void attach(final @NotNull CompilationRunner cr) {
		compilationRunner = cr;
	}

	@Override
	public Operation<Boolean> execute(final @NotNull CR_State st, final CB_Output aO) {
		compilationRunner = st.runner();

		try {
			compilationRunner.compilation.use(ci, do_out);
			return Operation.success(true);
		} catch (final Exception aE) {
			return Operation.failure(aE);
		}
	}

	@Override
	public String name() {
//			"process initial action"
		return "process initial";
	}
}
