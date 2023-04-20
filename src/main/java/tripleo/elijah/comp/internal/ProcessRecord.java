package tripleo.elijah.comp.internal;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;

import tripleo.elijah.comp.AccessBus;
import tripleo.elijah.comp.DeducePipeline;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.vendor.mal.stepA_mal;

public interface ProcessRecord {

	void writeLogs(ICompilationAccess aCa);

	Promise<GenerateResult, Void, Void> generateResultPromise();

	void setGenerateResult(GenerateResult gr);

	void consumeGenerateResult(Consumer<Supplier<GenerateResult>> csgr);

	PipelineLogic pipelineLogic();

	IPipelineAccess pa();

	DeducePipeline dpl();

	ICompilationAccess                         ca();

	AccessBus ab();

	DeferredObject<GenerateResult, Void, Void> _pgr();

	stepA_mal.MalEnv2                          env();
}