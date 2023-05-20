package tripleo.elijah.comp.i;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.internal.CR_State;
import tripleo.elijah.comp.internal.CompilerDriven;

public interface CD_CompilationRunnerStart extends CompilerDriven {

	void start(@NotNull CompilerInstructions aCompilerInstructions,
			   @NotNull CR_State crState);
}
