package tripleo.elijah.test_help;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ICompilationAccess;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.contexts.ModuleContext;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.OutputFileFactory;
import tripleo.elijah.stages.gen_generic.OutputFileFactoryParams;
import tripleo.elijah.stages.logging.ElLog;

public class Boilerplate {
	public Compilation        comp;
	public ICompilationAccess aca;
	public ProcessRecord      pr;
	public PipelineLogic      pipelineLogic;
	public GenerateFiles      generateFiles;
	OS_Module module;

	public void get() {
		comp = new CompilationImpl(new StdErrSink(), new IO());
		aca  = ((CompilationImpl) comp)._access();
		pr   = new ProcessRecord(aca);

//		final RuntimeProcesses rt = StageToRuntime.get(ca.getStage(), ca, pr);

		pipelineLogic = pr.ab.__getPL(); // FIXME make ab private
		//getGenerateFiles(mod);

		if (module != null) {
			module.setParent(comp);
		}
	}

	public void getGenerateFiles(final @NotNull OS_Module mod) {
		generateFiles = OutputFileFactory.create(Compilation.CompilationAlways.defaultPrelude(),
		  new OutputFileFactoryParams(mod,
			comp.getErrSink(),
			aca.testSilence(),
			pipelineLogic));
	}

	public OS_Module defaultMod() {
		if (module == null) {
			module = new OS_Module();
			module.setContext(new ModuleContext(module));
			if (comp != null)
				module.setParent(comp);
		}

		return module;
	}

	public DeducePhase getDeducePhase() {
		return pipelineLogic.dp;
	}

	public BoilerplateModuleBuilder withModBuilder(final OS_Module aMod) {
		return new BoilerplateModuleBuilder(aMod);
	}

	public DeduceTypes2 simpleDeduceModule3(final OS_Module aMod) {
		final ElLog.Verbosity verbosity = Compilation.gitlabCIVerbosity();
		@NotNull final String s         = Compilation.CompilationAlways.defaultPrelude();
		return simpleDeduceModule2(aMod, s, verbosity);
	}

	public DeduceTypes2 simpleDeduceModule2(final OS_Module mod, final @NotNull String aS, final ElLog.Verbosity aVerbosity) {
		final Compilation c = mod.getCompilation();

		mod.prelude = c.findPrelude(aS).success();

		return simpleDeduceModule(aVerbosity);
	}

	public DeduceTypes2 simpleDeduceModule(final ElLog.Verbosity verbosity) {
//		final PipelineLogic pl = new PipelineLogic(new AccessBus(module.getCompilation()));
//		final DeduceTypes2  d  = pl.dp.deduceModule(module, verbosity);

		final DeduceTypes2 d = getDeducePhase().deduceModule(module, verbosity);

//		d.processWachers();
		return d;
	}
}
