package tripleo.elijah.test_help;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.internal.CR_State;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.comp.internal.DefaultCompilationAccess;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.OutputFileFactory;
import tripleo.elijah.stages.gen_generic.OutputFileFactoryParams;

public class Boilerplate {
	public Compilation        comp;
	public ICompilationAccess aca;
	public ProcessRecord      pr;
	public PipelineLogic      pipelineLogic;
	public GenerateFiles      generateFiles;
	private CompilationRunner  cr;
	OS_Module module;

	public void get() {
		comp          = new CompilationImpl(new StdErrSink(), new IO());
		final ICompilationAccess aca1 = ((CompilationImpl) comp)._access();
		aca       = aca1 != null ? aca1 : new DefaultCompilationAccess(comp);
		cr        = new CompilationRunner(aca);

		comp.getCompilationEnclosure().setCompilationRunner(cr);

		final CR_State crState = comp.getCompilationEnclosure().getCompilationRunner().crState;
		crState.ca();
		assert comp.getCompilationEnclosure().getCompilationRunner().crState != null; // always true

		pr            = cr.crState.pr;
		pipelineLogic = pr.pipelineLogic();

		if (module != null) {
			module.setParent(comp);
		}
	}

	public void getGenerateFiles(final @NotNull OS_Module mod) {
		generateFiles = OutputFileFactory.create(Compilation.CompilationAlways.defaultPrelude(),
												 new OutputFileFactoryParams(mod,
																			 comp.getErrSink(),
																			 aca.testSilence(),
																			 comp.getCompilationEnclosure()));
	}

	public OS_Module defaultMod() {
		if (module == null) {
			module = new OS_Module();
			if (comp != null)
				module.setParent(comp);
		}

		return module;
	}

	public DeducePhase getDeducePhase() {
		return pr.pipelineLogic().dp;
	}

	public PipelineLogic pipelineLogic() {
		return pipelineLogic;
	}
}
