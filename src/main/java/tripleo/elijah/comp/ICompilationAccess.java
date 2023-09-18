package tripleo.elijah.comp;

import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.testing.comp.IFunctionMapHook;

import java.util.List;

public interface ICompilationAccess {
//	void addPipeline(final PipelineMember pl);

	ElLog.Verbosity testSilence();

	Compilation getCompilation();

	void writeLogs();

	List<IFunctionMapHook> functionMapHooks();

//	Pipeline pipelines();

	Stages getStage();
}
