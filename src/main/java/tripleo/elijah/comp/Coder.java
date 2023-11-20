package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Coder {

	public void codeNodes(final OS_Module mod, final List<GeneratedNode> resolved_nodes, final GeneratedNode generatedNode) {
		if (generatedNode instanceof GeneratedFunction) {
			final GeneratedFunction generatedFunction = (GeneratedFunction) generatedNode;
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

	private void setClassmapNodeCodes(@NotNull final Map<ClassStatement, GeneratedClass> aClassMap, final OS_Module mod) {
		aClassMap.values().forEach(generatedClass -> codeNodeClass(generatedClass, mod));
	}

	private static void extractNodes_toResolvedNodes(@NotNull final Map<FunctionDef, GeneratedFunction> aFunctionMap, @NotNull final List<GeneratedNode> resolved_nodes) {
		aFunctionMap.values().stream().map(generatedFunction -> (generatedFunction.idte_list)
				.stream()
				.filter(IdentTableEntry::isResolved)
				.map(IdentTableEntry::resolvedType)
				.collect(Collectors.toList())).forEach(resolved_nodes::addAll);
	}

	public void codeNode(final GeneratedNode generatedNode, final OS_Module mod) {
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

	public void codeNodeFunction(@NotNull final BaseGeneratedFunction generatedFunction, final OS_Module mod) {
		if (generatedFunction.getCode() == 0)
			generatedFunction.setCode(mod.parent.nextFunctionCode());
	}

	public void codeNodeClass(@NotNull final GeneratedClass generatedClass, final OS_Module mod) {
		if (generatedClass.getCode() == 0)
			generatedClass.setCode(mod.parent.nextClassCode());
	}

	public void codeNodeNamespace(@NotNull final GeneratedNamespace generatedNamespace, final OS_Module mod) {
		if (generatedNamespace.getCode() == 0)
			generatedNamespace.setCode(mod.parent.nextClassCode());
	}
}
