package tripleo.elijah.comp;

import tripleo.elijah.stages.deduce.FunctionMapHook;
import tripleo.elijah.stages.logging.ElLog;

import java.util.List;

public interface ICompilationAccess {
	void setPipelineLogic(final PipelineLogic pl);

//	void addPipeline(final PipelineMember pl);

	ElLog.Verbosity testSilence();

	Compilation getCompilation();

	void writeLogs();

	List<FunctionMapHook> functionMapHooks();

//	Pipeline pipelines();

	Stages getStage();
}
