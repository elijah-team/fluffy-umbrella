package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Coder {

	public void codeNodes(OS_Module mod, List<GeneratedNode> resolved_nodes, GeneratedNode generatedNode) {
		if (generatedNode instanceof GeneratedFunction) {
			GeneratedFunction generatedFunction = (GeneratedFunction) generatedNode;
			codeNodeFunction(generatedFunction, mod);
		} else if (generatedNode instanceof GeneratedClass) {
			final GeneratedClass generatedClass = (GeneratedClass) generatedNode;

//			assert generatedClass.getCode() == 0;
			if (generatedClass.getCode() == 0)
				codeNodeClass(generatedClass, mod);

			setClassmapNodeCodes(generatedClass.classMap, mod);

			extractNodes_toResolvedNodes(generatedClass.functionMap, resolved_nodes);
		} else if (generatedNode instanceof GeneratedNamespace) {
			final GeneratedNamespace generatedNamespace = (GeneratedNamespace) generatedNode;

			if (generatedNamespace.getCode() != 0)
				codeNodeNamespace(generatedNamespace, mod);

			setClassmapNodeCodes(generatedNamespace.classMap, mod);

			extractNodes_toResolvedNodes(generatedNamespace.functionMap, resolved_nodes);
		}
	}

	private void setClassmapNodeCodes(@NotNull Map<ClassStatement, GeneratedClass> aClassMap, OS_Module mod) {
		aClassMap.values().forEach(generatedClass -> codeNodeClass(generatedClass, mod));
	}

	private static void extractNodes_toResolvedNodes(@NotNull Map<FunctionDef, GeneratedFunction> aFunctionMap, @NotNull List<GeneratedNode> resolved_nodes) {
		aFunctionMap.values().stream().map(generatedFunction -> (generatedFunction.idte_list)
				.stream()
				.filter(IdentTableEntry::isResolved)
				.map(IdentTableEntry::resolvedType)
				.collect(Collectors.toList())).forEach(resolved_nodes::addAll);
	}

	public void codeNode(GeneratedNode generatedNode, OS_Module mod) {
		final Coder coder = this;

		if (generatedNode instanceof GeneratedFunction) {
			final GeneratedFunction generatedFunction = (GeneratedFunction) generatedNode;
			coder.codeNodeFunction(generatedFunction, mod);
		} else if (generatedNode instanceof GeneratedClass) {
			final GeneratedClass generatedClass = (GeneratedClass) generatedNode;
			coder.codeNodeClass(generatedClass, mod);
		} else if (generatedNode instanceof GeneratedNamespace) {
			final GeneratedNamespace generatedNamespace = (GeneratedNamespace) generatedNode;
			coder.codeNodeNamespace(generatedNamespace, mod);
		}
	}

	public void codeNodeFunction(@NotNull BaseGeneratedFunction generatedFunction, OS_Module mod) {
		if (generatedFunction.getCode() == 0)
			generatedFunction.setCode(mod.parent.nextFunctionCode());
	}

	public void codeNodeClass(@NotNull GeneratedClass generatedClass, OS_Module mod) {
		if (generatedClass.getCode() == 0)
			generatedClass.setCode(mod.parent.nextClassCode());
	}

	public void codeNodeNamespace(@NotNull GeneratedNamespace generatedNamespace, OS_Module mod) {
		if (generatedNamespace.getCode() == 0)
			generatedNamespace.setCode(mod.parent.nextClassCode());
	}
}
