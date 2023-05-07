package tripleo.elijah.comp.i;

import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.CompilationRunner;
import tripleo.elijah.comp.internal.CompilerDriven;

public interface CD_CompilationRunnerStart extends CompilerDriven {
	void start(final CompilationRunner aCompilationRunner, CompilerInstructions ci, boolean aDoOut, final IPipelineAccess pa);
}
