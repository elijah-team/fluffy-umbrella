package tripleo.elijah.comp;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;

import java.util.List;
import java.util.function.Function;

public class AccessBus {
	private final Compilation _c;
	private final DeferredObject<PipelineLogic,Void,Void> pipeLineLogicPromise = new DeferredObject<>();
	private final DeferredObject<List<GeneratedNode>, Void, Void> lgcPromise = new DeferredObject<>();
	private final DeferredObject<EIT_ModuleList, Void, Void> moduleListPromise = new DeferredObject<>();
	private final DeferredObject<GenerateResult, Void, Void> generateResultPromise = new DeferredObject<>();

	public AccessBus(Compilation aC) {
		_c = aC;
	}

	public @NotNull Compilation getCompilation() {
		return _c;
	}

	public void subscribePipelineLogic(DoneCallback<PipelineLogic> aPipelineLogicDoneCallback) {
		pipeLineLogicPromise.then(aPipelineLogicDoneCallback);
	}

	private void resolvePipelineLogic(PipelineLogic pl) {
		pipeLineLogicPromise.resolve(pl);
	}

	@Deprecated
	public void resolveModuleList(List<OS_Module> aModuleList) {
		resolveModuleList(new EIT_ModuleList(aModuleList)); // TODO
	}

	public void resolveModuleList(final EIT_ModuleList aModuleList) {
		moduleListPromise.resolve(aModuleList);
	}

	public void resolveGenerateResult(GenerateResult aGenerateResult) {
		generateResultPromise.resolve(aGenerateResult);
	}

	public void resolveLgc(List<GeneratedNode> lgc) {
		lgcPromise.resolve(lgc);
	}

	public void add(final @NotNull Function<AccessBus, PipelineMember> aCr) {
		PipelineMember x = aCr.apply(this);
		_c.pipelines.add(x);
	}

	public void addPipelineLogic(final @NotNull Function<AccessBus, PipelineLogic> aPlr) {
		PipelineLogic x = aPlr.apply(this);
		resolvePipelineLogic(x);
	}

	public void subscribe_lgc(@NotNull AB_LgcListener aLgcListener) {
		lgcPromise.then(aLgcListener::lgc_slot);
	}

	public void subscribe_moduleList(@NotNull AB_ModuleListListener aModuleListListener) {
		moduleListPromise.then(aModuleListListener::mods_slot);
	}

	public void subscribe_GenerateResult(@NotNull AB_GenerateResultListener aGenerateResultListener) {
		generateResultPromise.then(aGenerateResultListener::gr_slot);
	}

	public interface AB_ModuleListListener {
		//		void mods_slot(List<OS_Module> mods);
		void mods_slot(final EIT_ModuleList aModuleList);
	}

	public interface AB_LgcListener {
		void lgc_slot(List<GeneratedNode> lgc);
	}

	public interface AB_GenerateResultListener {
		void gr_slot(GenerateResult gr);
	}
}
