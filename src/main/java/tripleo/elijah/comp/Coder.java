package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Coder {
	private final ICodeRegistrar codeRegistrar;

	public Coder(final ICodeRegistrar aCodeRegistrar) {
		codeRegistrar = aCodeRegistrar;
	}

	public void codeNodes(final List<EvaNode> resolved_nodes, final EvaNode aEvaNode) {
		if (aEvaNode instanceof final EvaFunction generatedFunction) {
			codeNodeFunction(generatedFunction);
		} else if (aEvaNode instanceof final EvaClass generatedClass) {
			assert generatedClass.getCode() == 0;
			if (generatedClass.getCode() == 0)
				codeNodeClass(generatedClass);

			setClassmapNodeCodes(generatedClass.classMap);

			extractNodes_toResolvedNodes(generatedClass.functionMap.values(), resolved_nodes);
		} else if (aEvaNode instanceof final EvaNamespace generatedNamespace) {

			if (generatedNamespace.getCode() != 0)
				codeNodeNamespace(generatedNamespace);

			setClassmapNodeCodes(generatedNamespace.classMap);

			extractNodes_toResolvedNodes(generatedNamespace.functionMap.values(), resolved_nodes);
		}
	}

	public void codeNodeFunction(@NotNull final BaseEvaFunction generatedFunction) {
		codeRegistrar.registerFunction(generatedFunction);
	}

	private void codeNodeClass(@NotNull final EvaClass generatedClass) {
		codeRegistrar.registerClass(generatedClass);
	}

	private void setClassmapNodeCodes(@NotNull final Map<ClassStatement, EvaClass> aClassMap) {
		aClassMap.values().forEach(this::codeNodeClass);
	}

	public void codeNodeNamespace(@NotNull final EvaNamespace generatedNamespace) {
		codeRegistrar.registerNamespace(generatedNamespace);
	}

	public void codeNode(final EvaNode aEvaNode) {
		if (aEvaNode instanceof final EvaFunction generatedFunction) {
			this.codeNodeFunction(generatedFunction);
		} else if (aEvaNode instanceof final EvaClass generatedClass) {
			this.codeNodeClass(generatedClass);
		} else if (aEvaNode instanceof final EvaNamespace generatedNamespace) {
			this.codeNodeNamespace(generatedNamespace);
		}
	}

	private void extractNodes_toResolvedNodes(final Collection<EvaFunction> aValues, final List<EvaNode> resolved_nodes) {
		aValues.stream().map(generatedFunction -> (generatedFunction.idte_list)
		         .stream()
		         .filter(IdentTableEntry::isResolved)
		         .map(IdentTableEntry::resolvedType)
		         .collect(Collectors.toList()))
		       .forEach(resolved_nodes::addAll);
	}
}
