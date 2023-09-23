package tripleo.elijah.stages.deduce.nextgen;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.GeneratePhase;
import tripleo.elijah.util.Eventual;

public interface DeduceCreationContext {

	@NotNull DeducePhase getDeducePhase();

	DeduceTypes2 getDeduceTypes2();

	@NotNull GeneratePhase getGeneratePhase();

	Eventual<BaseEvaFunction> makeGenerated_fi__Eventual(FunctionInvocation aFunctionInvocation);
}
