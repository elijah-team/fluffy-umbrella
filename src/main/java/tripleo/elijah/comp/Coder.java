package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Coder {
	private final ICodeRegistrar codeRegistrar;

	public Coder(final ICodeRegistrar aCodeRegistrar) {
		codeRegistrar = aCodeRegistrar;
	}

	public void codeNodes(final OS_Module mod, final List<EvaNode> resolved_nodes, final EvaNode aEvaNode) {
		if (aEvaNode instanceof final EvaFunction generatedFunction) {
			codeNodeFunction(generatedFunction, mod);
		} else if (aEvaNode instanceof final EvaClass generatedClass) {
			//			assert generatedClass.getCode() == 0;
			if (generatedClass.getCode() == 0)
				codeNodeClass(generatedClass, mod);

			setClassmapNodeCodes(generatedClass.classMap, mod);

			extractNodes_toResolvedNodes(generatedClass.functionMap, resolved_nodes);
		} else if (aEvaNode instanceof final EvaNamespace generatedNamespace) {

			if (generatedNamespace.getCode() != 0)
				codeNodeNamespace(generatedNamespace, mod);

			setClassmapNodeCodes(generatedNamespace.classMap, mod);

			extractNodes_toResolvedNodes(generatedNamespace.functionMap, resolved_nodes);
		}
	}

	public void codeNodeFunction(@NotNull final BaseEvaFunction generatedFunction, final OS_Module mod) {
//		if (generatedFunction.getCode() == 0)
//			generatedFunction.setCode(mod.parent.nextFunctionCode());
		codeRegistrar.registerFunction(generatedFunction);
	}

	private void codeNodeClass(@NotNull final EvaClass generatedClass, final OS_Module mod) {
//		if (generatedClass.getCode() == 0)
//			generatedClass.setCode(mod.parent.nextClassCode());
		codeRegistrar.registerClass(generatedClass);
	}

	private void setClassmapNodeCodes(@NotNull final Map<ClassStatement, EvaClass> aClassMap, final OS_Module mod) {
		aClassMap.values().forEach(generatedClass -> codeNodeClass(generatedClass, mod));
	}

	private static void extractNodes_toResolvedNodes(@NotNull final Map<FunctionDef, EvaFunction> aFunctionMap, @NotNull final List<EvaNode> resolved_nodes) {
		aFunctionMap.values().stream().map(generatedFunction -> (generatedFunction.idte_list)
		              .stream()
		              .filter(IdentTableEntry::isResolved)
		              .map(IdentTableEntry::resolvedType)
		              .collect(Collectors.toList()))
		            .forEach(resolved_nodes::addAll);
	}

	public void codeNodeNamespace(@NotNull final EvaNamespace generatedNamespace, final OS_Module mod) {
//		if (generatedNamespace.getCode() == 0)
//			generatedNamespace.setCode(mod.parent.nextClassCode());
		codeRegistrar.registerNamespace(generatedNamespace);
	}

	public void codeNode(final EvaNode aEvaNode, final OS_Module mod) {
		final Coder coder = this;

		if (aEvaNode instanceof final EvaFunction generatedFunction) {
			coder.codeNodeFunction(generatedFunction, mod);
		} else if (aEvaNode instanceof final EvaClass generatedClass) {
			coder.codeNodeClass(generatedClass, mod);
		} else if (aEvaNode instanceof final EvaNamespace generatedNamespace) {
			coder.codeNodeNamespace(generatedNamespace, mod);
		}
	}
}
