package tripleo.elijah.comp.i;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.PipelineMember;
import tripleo.elijah.comp.Stages;
import tripleo.elijah.stages.deduce.IFunctionMapHook;
import tripleo.elijah.stages.logging.ElLog;

import java.util.List;

public interface ICompilationAccess {
	void setPipelineLogic(final PipelineLogic pl);

	void addPipeline(final PipelineMember pl);

	ElLog.Verbosity testSilence();

	Compilation getCompilation();

	void writeLogs();

	List<IFunctionMapHook> functionMapHooks();

	Stages getStage();
}
