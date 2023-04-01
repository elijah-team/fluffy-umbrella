package tripleo.elijah.stages.gen_generic;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaConstructor;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.work.WorkManager;

import java.util.Collection;

public interface GenerateFiles extends CodeGenerator {
	@NotNull
	static Collection<GeneratedNode> functions_to_list_of_generated_nodes(Collection<GeneratedFunction> generatedFunctions) {
		return Collections2.transform(generatedFunctions, new Function<GeneratedFunction, GeneratedNode>() {
			@org.checkerframework.checker.nullness.qual.Nullable
			@Override
			public GeneratedNode apply(@org.checkerframework.checker.nullness.qual.Nullable GeneratedFunction input) {
				return input;
			}
		});
	}

	@NotNull
	static Collection<GeneratedNode> constructors_to_list_of_generated_nodes(Collection<EvaConstructor> aEvaConstructors) {
		return Collections2.transform(aEvaConstructors, new Function<EvaConstructor, GeneratedNode>() {
			@org.checkerframework.checker.nullness.qual.Nullable
			@Override
			public GeneratedNode apply(@org.checkerframework.checker.nullness.qual.Nullable EvaConstructor input) {
				return input;
			}
		});
	}

	@NotNull
	static Collection<GeneratedNode> classes_to_list_of_generated_nodes(Collection<EvaClass> aEvaClasses) {
		return Collections2.transform(aEvaClasses, new Function<EvaClass, GeneratedNode>() {
			@org.checkerframework.checker.nullness.qual.Nullable
			@Override
			public GeneratedNode apply(@org.checkerframework.checker.nullness.qual.Nullable EvaClass input) {
				return input;
			}
		});
	}

	GenerateResult generateCode(Collection<GeneratedNode> aNodeCollection, WorkManager aWorkManager);

/*
	@Override
	void generate_namespace(GeneratedNamespace aGeneratedNamespace, GenerateResult aGenerateResult);

	@Override
	void generate_class(GeneratedClass aGeneratedClass, GenerateResult aGenerateResult);
*/
}
