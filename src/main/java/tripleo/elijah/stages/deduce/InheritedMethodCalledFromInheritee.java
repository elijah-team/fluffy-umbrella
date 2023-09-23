package tripleo.elijah.stages.deduce;

import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;

public record InheritedMethodCalledFromInheritee(FunctionDef activeFunction,
												 ClassStatement definedIn,
												 BaseEvaFunction calledIn)
		implements CI_Hint {
	// the function referenced by pte.expression is an inherited method
	// defined in genType.resolved and called in generatedFunction
	// for whatever reason we don't have a CodePoint (gf, instruction...)
}
