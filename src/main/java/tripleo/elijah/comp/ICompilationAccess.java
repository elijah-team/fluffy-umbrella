package tripleo.elijah.comp;

import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.testing.comp.IFunctionMapHook;

import java.util.List;

public interface ICompilationAccess {
//	void addPipeline(final PipelineMember pl);

	List<IFunctionMapHook> functionMapHooks();

	Compilation getCompilation();

	Stages getStage();

	ElLog.Verbosity testSilence();

//	Pipeline pipelines();

	void writeLogs();
}
