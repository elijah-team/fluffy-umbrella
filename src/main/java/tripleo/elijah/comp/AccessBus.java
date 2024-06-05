package tripleo.elijah.comp;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;

import java.util.List;
import java.util.function.Function;

public class AccessBus {
	private final Compilation                                     _c;
	private final DeferredObject<PipelineLogic, Void, Void>       pipeLineLogicPromise  = new DeferredObject<>();
	private final DeferredObject<List<GeneratedNode>, Void, Void> lgcPromise            = new DeferredObject<>();
	private final DeferredObject<List<OS_Module>, Void, Void>     moduleListPromise     = new DeferredObject<>();
	private final DeferredObject<GenerateResult, Void, Void>      generateResultPromise = new DeferredObject<>();
	private       Compilation.PipelineAdder                       _pa;

	public AccessBus(Compilation aC) {
		_c  = aC;
		_pa = _c.getPipelineAdder(); // TODO despi?
	}

	public @NotNull Compilation getCompilation() {
		return _c;
	}

	private void resolvePipelineLogic(PipelineLogic pl) {
		pipeLineLogicPromise.resolve(pl);
	}

	public void resolveModuleList(List<OS_Module> aModuleList) {
		moduleListPromise.resolve(aModuleList);
	}

	public void resolveGenerateResult(GenerateResult aGenerateResult) {
		generateResultPromise.resolve(aGenerateResult);
	}

	public void resolveLgc(List<GeneratedNode> lgc) {
		lgcPromise.resolve(lgc);
	}

	public void add(final @NotNull Function<AccessBus, PipelineMember> aPipelineMemberFunction) {
		PipelineMember x = aPipelineMemberFunction.apply(this);
		_pa.addPipeline(x);
	}

	public void addPipelineLogic(final @NotNull Function<AccessBus, PipelineLogic> aPipelineLogicFunction) {
		PipelineLogic x = aPipelineLogicFunction.apply(this);
		resolvePipelineLogic(x);
	}

	public void subscribe_moduleList(@NotNull AB_ModuleListListener aModuleListListener) {
		moduleListPromise.then(aModuleListListener::mods_slot);
	}

	public void subscribe_lgc(@NotNull AB_LgcListener aLgcListener) {
		lgcPromise.then(aLgcListener::lgc_slot);
	}

	public void subscribe_GenerateResult(@NotNull AB_GenerateResultListener aGenerateResultListener) {
		generateResultPromise.then(aGenerateResultListener::gr_slot);
	}

	public void subscribe_PipelineLogic(AB_PipelineLogicListener aPipelineLogicDoneCallback) {
		pipeLineLogicPromise.then(aPipelineLogicDoneCallback::pl_slot);
	}

	public void checkFinishEventuals__Fake() {
		// FIXME do this better: add markers +/- compiler assertions
		if (!moduleListPromise.isResolved()) {
			throw new AssertionError();
		}
		if (!lgcPromise.isResolved()) {
			throw new AssertionError();
		}
		if (!generateResultPromise.isResolved()) {
			throw new AssertionError();
		}
		if (!pipeLineLogicPromise.isResolved()) {
			throw new AssertionError();
		}
	}

	public interface AB_ModuleListListener {
		void mods_slot(List<OS_Module> mods);
	}

	public interface AB_LgcListener {
		void lgc_slot(List<GeneratedNode> lgc);
	}

	public interface AB_GenerateResultListener {
		void gr_slot(GenerateResult gr);
	}

	public interface AB_PipelineLogicListener {
		void pl_slot(PipelineLogic gr);
	}
}
