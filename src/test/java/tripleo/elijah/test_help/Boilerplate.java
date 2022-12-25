package tripleo.elijah.test_help;

import tripleo.elijah.comp.*;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_generic.GenerateFiles;

public class Boilerplate {
	public Compilation comp;
	public ICompilationAccess aca;
	public ProcessRecord pr;
	public PipelineLogic pipelineLogic;
	public GenerateFiles generateFiles;
	OS_Module module;

	/*public void getGenerateFiles(final @NotNull OS_Module mod) {
		generateFiles = OutputFileFactory.create(Compilation.CompilationAlways.defaultPrelude(),
												 new OutputFileFactoryParams(mod,
																			 comp.getErrSink(),
																			 aca.testSilence(),
																			 pipelineLogic));
	}*/
	
	public void get() {
		comp = new CompilationImpl(new StdErrSink(), new IO());
		aca = new DefaultCompilationAccess(comp);
		pr = new ProcessRecord(aca);
		pipelineLogic = pr.pipelineLogic;
		//getGenerateFiles(mod);
		
		if (module != null) {
			module.setParent(comp);
		}
	}
	
	public OS_Module defaultMod() {
		if (module == null) {
			module = new OS_Module();
			if (comp != null)
				module.setParent(comp);
		}
		
		return module;
	}
}
