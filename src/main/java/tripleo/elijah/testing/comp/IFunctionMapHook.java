package tripleo.elijah.testing.comp;

import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;

import java.util.Collection;

public interface IFunctionMapHook {
	boolean matches(FunctionDef aFunctionDef);

	void apply(Collection<GeneratedFunction> aGeneratedFunctions);
}
