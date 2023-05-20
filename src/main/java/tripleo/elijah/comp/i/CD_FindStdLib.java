package tripleo.elijah.comp.i;

import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationRunner;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.internal.CR_State;
import tripleo.elijah.comp.internal.CompilerDriven;

import java.util.function.Consumer;

public interface CD_FindStdLib extends CompilerDriven {
	void findStdLib(CR_State crState, String aPreludeName, Consumer<Operation<CompilerInstructions>> coci);
}
