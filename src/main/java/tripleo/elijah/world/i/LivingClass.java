package tripleo.elijah.world.i;

import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;

public interface LivingClass {
	ClassStatement getElement();

	int getCode();

	EvaClass evaNode();

	void garish(GenerateC aGenerateC, GenerateResult aGr, GenerateResultSink aResultSink);

	//void setGarish(GarishClass aGarishClass);
}
