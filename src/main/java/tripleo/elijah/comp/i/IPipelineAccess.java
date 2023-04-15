package tripleo.elijah.comp.i;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;

import tripleo.elijah.comp.*;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.stages.gen_fn.EvaNode;

import java.util.List;

public interface IPipelineAccess {
	Compilation getCompilation();

	DeducePipeline getDeducePipeline();

	PipelineLogic pipelineLogic();

	ProcessRecord getProcessRecord();

	Promise<PipelineLogic, Void, Void> getPipelineLogicPromise();

	void setNodeList(List<EvaNode> aEvaNodeList);

	void registerNodeList(DoneCallback<List<EvaNode>> done);

	void _setAccessBus(AccessBus ab);

	AccessBus getAccessBus();

	WritePipeline getWitePipeline();

	void setWritePipeline(WritePipeline aWritePipeline);
}
