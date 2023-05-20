package tripleo.elijah.comp.i;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.comp.notation.GN_Notable;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.logging.ElLog;

import java.util.List;

public interface IPipelineAccess {
	Compilation getCompilation();

	DeducePipeline getDeducePipeline();

	PipelineLogic pipelineLogic();

	ProcessRecord getProcessRecord();

	DeferredObject/* Promise */<PipelineLogic, Void, Void> getPipelineLogicPromise();

	void setNodeList(List<EvaNode> aEvaNodeList);

	void registerNodeList(DoneCallback<List<EvaNode>> done);

	void _setAccessBus(AccessBus ab);

	AccessBus getAccessBus();

	WritePipeline getWitePipeline();

	void setWritePipeline(WritePipeline aWritePipeline);

	void notate(int provenance, GN_Notable aNotable);

	List<CompilerInput> getCompilerInput();

	void setCompilerInput(List<CompilerInput> aInputs);

	CompilationClosure getCompilationClosure();

	CompilationEnclosure getCompilationEnclosure();

	void addLog(ElLog aLOG);
}
