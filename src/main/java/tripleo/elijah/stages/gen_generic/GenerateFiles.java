package tripleo.elijah.stages.gen_generic;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.nextgen.model.SM_Node;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaConstructor;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;

import java.util.Collection;
import java.util.List;

public interface GenerateFiles extends CodeGenerator {

	static Collection<EvaNode> classes_to_list_of_generated_nodes(Collection<EvaClass> aValues) {
		throw new NotImplementedException();

	}

	static Collection<EvaNode> constructors_to_list_of_generated_nodes(Collection<EvaConstructor> aValues) {
		throw new NotImplementedException();

	}

	static Collection<EvaNode> functions_to_list_of_generated_nodes(Collection<EvaFunction> aValues) {
		throw new NotImplementedException();

	}

	GenerateResult generateCode(final @NotNull Collection<EvaNode> aNodeCollection, final @NotNull WorkManager aWorkManager);

	void forNode(final SM_Node aNode);

	GenerateResult resultsFromNodes(List<EvaNode> aNodes, WorkManager aWm);

	default void generate_function(EvaFunction aGf, GenerateResult aGr, WorkList aWl, GenerateResultSink aResultSink) {
		throw new NotImplementedException();

	}

	default void generate_constructor(EvaConstructor aGf, GenerateResult aGr, WorkList aWl, GenerateResultSink aResultSink, WorkManager aWorkManager, GenerateResultEnv aFileGen) {
		throw new NotImplementedException();

	}

	void finishUp(Old_GenerateResult aGr, WorkManager aWm, WorkList aWl);

	GenerateResult resultsFromNodes(@NotNull List<EvaNode> aNodes,
	                                @NotNull WorkManager wm,
	                                @NotNull GenerateResultSink grs,
	                                @NotNull GenerateResultEnv fg);

	GenerateResult generateCode(Collection<EvaNode> aGn2, GenerateResultEnv aFileGen);
}
