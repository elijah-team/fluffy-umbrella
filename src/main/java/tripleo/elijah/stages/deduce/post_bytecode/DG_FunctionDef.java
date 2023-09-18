package tripleo.elijah.stages.deduce.post_bytecode;

import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.OS_Element;

public class DG_FunctionDef implements DG_Item {
	private final BaseFunctionDef functionDef;

	public DG_FunctionDef(final BaseFunctionDef aFunctionDef) {
		functionDef = aFunctionDef;
	}

	public OS_Element getFunctionDef() {
		return functionDef;
	}
}
