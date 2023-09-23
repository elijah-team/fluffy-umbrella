package tripleo.elijah.comp.i;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import tripleo.elijah.comp.AccessBus;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilerInput;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.comp.internal.Provenance;
import tripleo.elijah.comp.notation.GN_Env;
import tripleo.elijah.comp.notation.GN_Notable;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.logging.ElLog;

import java.util.List;
import java.util.function.Consumer;

public interface IPipelineAccess {
	void _send_GeneratedClass(EvaNode aClass);

//	void addFunctionStatement(EvaPipeline.FunctionStatement aFunctionStatement);

	void _setAccessBus(AccessBus ab);

//	void addOutput(NG_OutputItem aO);

	void activeClass(EvaClass aEvaClass);

	void activeFunction(BaseEvaFunction aEvaFunction);

	void activeNamespace(EvaNamespace aEvaNamespace);

	void addLog(ElLog aLOG);

	AccessBus getAccessBus();

	List<EvaClass> getActiveClasses();

	//List<NG_OutputItem> getOutputs();

	List<BaseEvaFunction> getActiveFunctions();

	List<EvaNamespace> getActiveNamespaces();

	Compilation getCompilation();

	CompilationEnclosure getCompilationEnclosure();

	List<CompilerInput> getCompilerInput();

	GenerateResultSink getGenerateResultSink();

//	void setEvaPipeline(@NotNull EvaPipeline agp);

	DeferredObject/* Promise */<PipelineLogic, Void, Void> getPipelineLogicPromise();

	ProcessRecord getProcessRecord();

	WritePipeline getWitePipeline();

	void install_notate(Provenance aProvenance, Class<? extends GN_Notable> aRunClass, Class<? extends GN_Env> aEnvClass);

	void notate(Provenance aProvenance, GN_Env aPlRun2);

	void notate(Provenance provenance, GN_Notable aNotable);

	PipelineLogic pipelineLogic();

	void registerNodeList(DoneCallback<List<EvaNode>> done);

	void resolvePipelinePromise(PipelineLogic aPipelineLogic);

	void resolveWaitGenC(OS_Module mod, GenerateC gc);

	void setCompilerInput(List<CompilerInput> aInputs);

	void setGenerateResultSink(GenerateResultSink aGenerateResultSink);

	void setNodeList(List<EvaNode> aEvaNodeList);

	void setWritePipeline(WritePipeline aWritePipeline);

	void waitGenC(OS_Module mod, Consumer<GenerateC> aCb);
}
