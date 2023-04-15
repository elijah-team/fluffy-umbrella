package tripleo.elijah.stages.gen_generic.pipeline_impl;

import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.world.i.LivingClass;

public interface GenerateResultSink {
	void add(EvaNode node);

	void additional(GenerateResult aGenerateResult);

	LivingClass getLivingClassForEva(EvaClass aEvaClass);
}
