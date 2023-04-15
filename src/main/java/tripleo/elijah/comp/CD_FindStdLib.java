package tripleo.elijah.comp;

import tripleo.elijah.comp.internal.CompilationBus;

public interface CD_FindStdLib extends CompilationBus.CompilerDriven {
	void findStdLib(CompilationRunner aCompilationRunner, String aPreludeName, Compilation aC);
}
