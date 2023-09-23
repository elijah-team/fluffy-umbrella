package tripleo.elijah.stages.garish;

import org.jetbrains.annotations.NotNull;

import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;

public class GarishClass_Generator {
	private final EvaClass carrier;
	private       boolean  generatedAlready;

	public GarishClass_Generator(final EvaClass aEvaClass) {
		carrier = aEvaClass;
	}

	public boolean generatedAlready() {
		return generatedAlready;
	}

	public void provide(final @NotNull GenerateResultSink aResultSink,
	                    final @NotNull GarishClass aGarishClass,
	                    final GenerateResult aGr,
	                    final GenerateC aGenerateC) {
		if (generatedAlready) {
			return;
		}

		switch (carrier.getKlass().getType()) {
		// Don't generate class definition for these three
		case INTERFACE:
		case SIGNATURE:
		case ABSTRACT:
			return;
		}

		//aResultSink.addClass_0(this, tos.getBuffer(), tosHdr.getBuffer());
		aResultSink.addClass_1(aGarishClass, aGr, aGenerateC);
		generatedAlready = true;
	}
}
