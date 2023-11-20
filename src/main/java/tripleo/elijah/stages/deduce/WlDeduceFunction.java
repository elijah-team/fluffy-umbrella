package tripleo.elijah.stages.deduce;

import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedConstructor;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.WlGenerateCtor;
import tripleo.elijah.stages.gen_fn.WlGenerateDefaultCtor;
import tripleo.elijah.stages.gen_fn.WlGenerateFunction;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkManager;

import java.util.List;

class WlDeduceFunction implements WorkJob {
	private final WorkJob                     workJob;
	private final List<BaseGeneratedFunction> coll;
	private final DeduceTypes2                dt2;
	private       boolean                     _isDone;

	public WlDeduceFunction(final WorkJob aWorkJob, final List<BaseGeneratedFunction> aColl, final DeduceTypes2 aDeduceTypes2) {
		workJob = aWorkJob;
		coll    = aColl;
		dt2     = aDeduceTypes2;
	}

	@Override
	public void run(final WorkManager aWorkManager) {
		// TODO assumes result is in the same file as this (DeduceTypes2)

		if (workJob instanceof WlGenerateFunction) {
			final GeneratedFunction generatedFunction1 = ((WlGenerateFunction) workJob).getResult();
			if (!coll.contains(generatedFunction1)) {
				coll.add(generatedFunction1);
				if (!generatedFunction1.deducedAlready) {
					dt2.deduce_generated_function(generatedFunction1);
				}
				generatedFunction1.deducedAlready = true;
			}
		} else if (workJob instanceof WlGenerateDefaultCtor) {
			final GeneratedConstructor generatedConstructor = (GeneratedConstructor) ((WlGenerateDefaultCtor) workJob).getResult();
			if (!coll.contains(generatedConstructor)) {
				coll.add(generatedConstructor);
				if (!generatedConstructor.deducedAlready) {
					dt2.deduce_generated_constructor(generatedConstructor);
				}
				generatedConstructor.deducedAlready = true;
			}
		} else if (workJob instanceof WlGenerateCtor) {
			final GeneratedConstructor generatedConstructor = ((WlGenerateCtor) workJob).getResult();
			if (!coll.contains(generatedConstructor)) {
				coll.add(generatedConstructor);
				if (!generatedConstructor.deducedAlready) {
					dt2.deduce_generated_constructor(generatedConstructor);
				}
				generatedConstructor.deducedAlready = true;
			}
		} else
			throw new NotImplementedException();

		assert coll.size() == 1;

		_isDone = true;
	}

	@Override
	public boolean isDone() {
		return _isDone;
	}
}
