package tripleo.elijah.testing.comp;

import java.util.Collection;

import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.stages.gen_fn.EvaFunction;

public interface IFunctionMapHook {
	void apply(Collection<EvaFunction> aGeneratedFunctions);

	boolean matches(BaseFunctionDef aFunctionDef);
}
