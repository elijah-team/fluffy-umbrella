package tripleo.elijah.testing.comp;

import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.stages.gen_fn.EvaFunction;

import java.util.Collection;

public interface IFunctionMapHook {
	boolean matches(BaseFunctionDef aFunctionDef);

	void apply(Collection<EvaFunction> aGeneratedFunctions);
}
