package tripleo.elijah.entrypoints;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_fn.WlGenerateClass;
import tripleo.elijah.stages.gen_fn.WlGenerateFunction;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.world.i.LivingRepo;

public interface EntryPointProcessor {
	static @NotNull EntryPointProcessor dispatch(final EntryPoint ep, final DeducePhase aDeducePhase, final WorkList aWl, final GenerateFunctions aGenerateFunctions) {
		if (ep instanceof MainClassEntryPoint) {
			return new EPP_MCEP((MainClassEntryPoint) ep, aDeducePhase, aWl, aGenerateFunctions);
		} else if (ep instanceof ArbitraryFunctionEntryPoint) {
			return new EPP_AFEP((ArbitraryFunctionEntryPoint) ep, aDeducePhase, aWl, aGenerateFunctions);
		}

		throw new IllegalStateException();
	}

	void process();

	class EPP_MCEP implements EntryPointProcessor {
		private final MainClassEntryPoint mcep;
		private final DeducePhase         deducePhase;
		private final WorkList            wl;
		private final GenerateFunctions   generateFunctions;

		public EPP_MCEP(final MainClassEntryPoint aEp, final DeducePhase aDeducePhase, final WorkList aWl, final GenerateFunctions aGenerateFunctions) {
			mcep              = aEp;
			deducePhase       = aDeducePhase;
			wl                = aWl;
			generateFunctions = aGenerateFunctions;
		}

		@Override
		public void process() {
			final @NotNull ClassStatement cs = mcep.getKlass();
			final @NotNull FunctionDef    f  = mcep.getMainFunction();
			final ClassInvocation         ci = deducePhase.registerClassInvocation(cs, null, ()->null);

			assert ci != null; // TODO use eventual with registrar this

			final ICodeRegistrar codeRegistrar = new ICodeRegistrar() {
				final Compilation compilation = deducePhase._compilation();

				@Override
				public void registerNamespace(final EvaNamespace aNamespace) {
					compilation._repo.addNamespace(aNamespace, LivingRepo.Add.NONE);
				}

				@Override
				public void registerClass(final EvaClass aClass) {
					compilation._repo.addClass(aClass, LivingRepo.Add.MAIN_CLASS);
				}

				@Override
				public void registerFunction(final BaseEvaFunction aFunction) {
					compilation._repo.addFunction(aFunction, LivingRepo.Add.MAIN_FUNCTION);
				}
			};


//			final ICodeRegistrar           cr = deducePhase.codeRegistrar;
			final ICodeRegistrar cr = codeRegistrar;

			final @NotNull WlGenerateClass job = new WlGenerateClass(generateFunctions, ci, deducePhase.generatedClasses, cr);
			wl.addJob(job);

			final @NotNull FunctionInvocation fi = new FunctionInvocation(f, null, ci, deducePhase.generatePhase);
//				fi.setPhase(phase);
			final @NotNull WlGenerateFunction job1 = new WlGenerateFunction(generateFunctions, fi, cr);
			wl.addJob(job1);
		}
	}

	class EPP_AFEP implements EntryPointProcessor {

		private final ArbitraryFunctionEntryPoint afep;
		private final DeducePhase                 deducePhase;
		private final WorkList                    wl;
		private final GenerateFunctions           generateFunctions;

		public EPP_AFEP(final ArbitraryFunctionEntryPoint aEp, final DeducePhase aDeducePhase, final WorkList aWl, final GenerateFunctions aGenerateFunctions) {
			afep              = aEp;
			deducePhase       = aDeducePhase;
			wl                = aWl;
			generateFunctions = aGenerateFunctions;
		}

		@Override
		public void process() {
			final @NotNull FunctionDef     f  = afep.getFunction();
			@NotNull final ClassInvocation ci = deducePhase.registerClassInvocation((ClassStatement) afep.getParent());

			final WlGenerateClass job = new WlGenerateClass(generateFunctions, ci, deducePhase.generatedClasses, deducePhase.getCodeRegistrar());
			wl.addJob(job);

			final @NotNull FunctionInvocation fi = new FunctionInvocation(f, null, ci, deducePhase.generatePhase);
//				fi.setPhase(phase);
			final WlGenerateFunction job1 = new WlGenerateFunction(generateFunctions, fi, deducePhase.getCodeRegistrar());
			wl.addJob(job1);
		}
	}
}
