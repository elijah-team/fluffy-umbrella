package tripleo.elijah.world.i;

import tripleo.elijah.comp.GeneratePipeline;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.stages.garish.GarishClass;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_generic.GenerateResult;

public interface LivingClass {
	ClassStatement getElement();

	int getCode();

	EvaClass evaNode();

	void garish(GenerateC aGenerateC, GenerateResult aGr, GeneratePipeline.GenerateResultSink aResultSink);

	//void setGarish(GarishClass aGarishClass);
}
