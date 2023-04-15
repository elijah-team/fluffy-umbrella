package tripleo.elijah.comp.i;

import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationRunner;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.internal.CompilationBus;

import java.util.function.Consumer;

public interface CD_FindStdLib extends CompilationBus.CompilerDriven {
	void findStdLib(CompilationRunner aCompilationRunner, String aPreludeName, Compilation aC, Consumer<Operation<CompilerInstructions>> coci);
}
