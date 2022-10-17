package tripleo.elijah.entrypoints;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_fn.WlGenerateClass;
import tripleo.elijah.stages.gen_fn.WlGenerateFunction;
import tripleo.elijah.work.WorkList;

public interface EntryPointProcessor {
	void process();

	static EntryPointProcessor dispatch(final EntryPoint ep, DeducePhase aDeducePhase, WorkList aWl, GenerateFunctions aGenerateFunctions) {
		if (ep instanceof MainClassEntryPoint) {
			return new EPP_MCEP((MainClassEntryPoint) ep, aDeducePhase, aWl, aGenerateFunctions);
		} else if (ep instanceof ArbitraryFunctionEntryPoint) {
			return new EPP_AFEP((ArbitraryFunctionEntryPoint) ep, aDeducePhase, aWl, aGenerateFunctions);
		}

		throw new IllegalStateException();
	}

	class EPP_MCEP implements EntryPointProcessor {
		private final MainClassEntryPoint mcep;
		private final DeducePhase deducePhase;
		private final WorkList wl;
		private final GenerateFunctions generateFunctions;

		public EPP_MCEP(MainClassEntryPoint aEp, DeducePhase aDeducePhase, WorkList aWl, GenerateFunctions aGenerateFunctions) {
			mcep = aEp;
			deducePhase = aDeducePhase;
			wl = aWl;
			generateFunctions = aGenerateFunctions;
		}

		@Override
		public void process() {
			@NotNull final ClassStatement cs = mcep.getKlass();
			final FunctionDef f = mcep.getMainFunction();
			ClassInvocation ci = deducePhase.registerClassInvocation(cs, null);
			wl.addJob(new WlGenerateClass(generateFunctions, ci, deducePhase.generatedClasses));
			final FunctionInvocation fi = new FunctionInvocation(f, null, ci, deducePhase.generatePhase);
//				fi.setPhase(phase);
			wl.addJob(new WlGenerateFunction(generateFunctions, fi));
		}
	}

	class EPP_AFEP implements EntryPointProcessor {

		private final ArbitraryFunctionEntryPoint afep;
		private final DeducePhase deducePhase;
		private final WorkList wl;
		private final GenerateFunctions generateFunctions;

		public EPP_AFEP(ArbitraryFunctionEntryPoint aEp, DeducePhase aDeducePhase, WorkList aWl, GenerateFunctions aGenerateFunctions) {
			afep = aEp;
			deducePhase = aDeducePhase;
			wl = aWl;
			generateFunctions = aGenerateFunctions;
		}

		@Override
		public void process() {
			final FunctionDef f = afep.getFunction();
			ClassInvocation ci = new ClassInvocation((ClassStatement) afep.getParent(), null);
			ci = deducePhase.registerClassInvocation(ci);
			wl.addJob(new WlGenerateClass(generateFunctions, ci, deducePhase.generatedClasses));
			final FunctionInvocation fi = new FunctionInvocation(f, null, ci, deducePhase.generatePhase);
//				fi.setPhase(phase);
			wl.addJob(new WlGenerateFunction(generateFunctions, fi));
		}
	}
}
