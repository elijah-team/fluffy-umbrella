package tripleo.elijah.comp.i;

import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.CompilationRunner;
import tripleo.elijah.comp.internal.CompilationBus;

public interface CD_CompilationRunnerStart extends CompilationBus.CompilerDriven {
	void start(final CompilationRunner aCompilationRunner, CompilerInstructions aCi, boolean aDoOut, final IPipelineAccess pa);
}
