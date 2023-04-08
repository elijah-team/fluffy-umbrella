package tripleo.elijah.comp.i;

import org.jdeferred2.Promise;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.DeducePipeline;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.ProcessRecord;

public interface IPipelineAccess {
	Compilation getCompilation();

	DeducePipeline getDeducePipeline();

	PipelineLogic pipelineLogic();

	ProcessRecord getProcessRecord();

	Promise<PipelineLogic, Void, Void> getPipelineLogicPromise();
}
