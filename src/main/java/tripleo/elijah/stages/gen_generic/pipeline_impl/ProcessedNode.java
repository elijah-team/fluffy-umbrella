package tripleo.elijah.stages.gen_generic.pipeline_impl;

import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.work.WorkManager;

public interface ProcessedNode {
	boolean matchModule(OS_Module aMod);

	boolean isContainerNode();

	void processContainer(GenerateFiles ggc,
						  GenerateResult gr,
						  GenerateResultSink aResultSink);

	void processConstructors(GenerateFiles ggc,
							 GenerateResult gr,
							 GenerateResultSink aResultSink, final WorkManager wm);

	void processFunctions(GenerateFiles ggc,
						  GenerateResult gr,
						  GenerateResultSink aResultSink, final WorkManager wm);

	void processClassMap(GenerateFiles ggc,
						 GenerateResult gr,
						 GenerateResultSink aResultSink, final WorkManager wm);
}
