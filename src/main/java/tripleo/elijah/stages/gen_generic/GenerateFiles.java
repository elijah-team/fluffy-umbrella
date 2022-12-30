package tripleo.elijah.stages.gen_generic;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedConstructor;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.work.WorkManager;

import java.util.Collection;

public interface GenerateFiles extends CodeGenerator {
	@NotNull
	static Collection<GeneratedNode> functions_to_list_of_generated_nodes(final Collection<GeneratedFunction> generatedFunctions) {
		return Collections2.transform(generatedFunctions, new Function<GeneratedFunction, GeneratedNode>() {
			@org.checkerframework.checker.nullness.qual.Nullable
			@Override
			public GeneratedNode apply(@org.checkerframework.checker.nullness.qual.Nullable final GeneratedFunction input) {
				return input;
			}
		});
	}

	@NotNull
	static Collection<GeneratedNode> constructors_to_list_of_generated_nodes(final Collection<GeneratedConstructor> aGeneratedConstructors) {
		return Collections2.transform(aGeneratedConstructors, new Function<GeneratedConstructor, GeneratedNode>() {
			@org.checkerframework.checker.nullness.qual.Nullable
			@Override
			public GeneratedNode apply(@org.checkerframework.checker.nullness.qual.Nullable final GeneratedConstructor input) {
				return input;
			}
		});
	}

	@NotNull
	static Collection<GeneratedNode> classes_to_list_of_generated_nodes(final Collection<GeneratedClass> aGeneratedClasses) {
		return Collections2.transform(aGeneratedClasses, new Function<GeneratedClass, GeneratedNode>() {
			@org.checkerframework.checker.nullness.qual.Nullable
			@Override
			public GeneratedNode apply(@org.checkerframework.checker.nullness.qual.Nullable final GeneratedClass input) {
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
