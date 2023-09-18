package tripleo.elijah.stages.gen_c;

import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.notation.GM_GenerateModule;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_generic.GenerateResultEnv;
import tripleo.elijah.stages.logging.ElLog;

public class WhyNotGarish_Function extends WhyNotGarish_BaseFunction implements WhyNotGarish_Item {
	private final BaseEvaFunction                               gf;

	private final GenerateC                                     generateC;
	private final DeferredObject<GenerateResultEnv, Void, Void> fileGenPromise = new DeferredObject<>();
	public WhyNotGarish_Function(final BaseEvaFunction aGf, final GenerateC aGenerateC) {
		gf        = aGf;
		generateC = aGenerateC;

		var compilationEnclosure = aGenerateC._ce();
		assert compilationEnclosure != null;
		fileGenPromise.then(fileGen -> onFileGen(fileGen, compilationEnclosure));
	}

	@Contract(pure = true)
	private @Nullable BaseEvaFunction deduced(final @NotNull BaseEvaFunction aEvaFunction, final CompilationEnclosure compilationEnclosure) {
		final GM_GenerateModule    generateModule       = generateC.getFileGen().gmgm();
//		final CompilationEnclosure compilationEnclosure = generateModule.gmr().env().pa().getCompilationEnclosure();
		final DeducePhase          deducePhase          = compilationEnclosure.getPipelineLogic().dp;

		final DeduceTypes2 dt2 = deducePhase._inj().new_DeduceTypes2(aEvaFunction.module(), deducePhase, ElLog.Verbosity.VERBOSE);
		dt2.deduceOneFunction((EvaFunction) aEvaFunction, deducePhase);

		return aEvaFunction;
	}

	@Override
	public BaseEvaFunction getGf() {
		return gf;
	}

	@Override
	public boolean hasFileGen() {
		return fileGenPromise.isResolved();
	}

	public void onFileGen(final @NotNull GenerateResultEnv aFileGen, final CompilationEnclosure compilationEnclosure) {
		if (gf.getFD() == null) assert false; //return; // FIXME why? when?
		Generate_Code_For_Method gcfm = new Generate_Code_For_Method(generateC, generateC.LOG);
		gcfm.generateCodeForMethod(deduced(gf, compilationEnclosure), aFileGen);
	}

	@Override
	public void provideFileGen(final GenerateResultEnv fg) {
		fileGenPromise.resolve(fg);
	}

	public void resolveFileGenPromise(final GenerateResultEnv aFileGen) {
		if (!fileGenPromise.isResolved())
			fileGenPromise.resolve(aFileGen);
		else
			System.out.println("twice for " + generateC);
	}
}
