package tripleo.elijah.comp;

import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.internal.CompilationBus;

public interface CD_CompilationRunnerStart extends  CompilationBus.CompilerDriven {
	void start(final CompilationRunner aCompilationRunner, CompilerInstructions aCi, boolean aDoOut);
}
