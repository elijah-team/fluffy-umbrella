package tripleo.elijah.comp;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.GeneratedContainerNC;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Stupidity;
import tripleo.elijah.work.WorkManager;

import java.util.Collection;
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

	void doModule(final @NotNull List<GeneratedNode> lgc,
				  final @NotNull WorkManager wm,
				  final @NotNull OS_Module mod,
				  final @NotNull PipelineLogic aPipelineLogic,
				  final @NotNull ErrSink errSink) {
		final ElLog.Verbosity verbosity = aPipelineLogic.getVerbosity();

		final GenerateC generateC = new GenerateC(mod, errSink, verbosity, aPipelineLogic);

		final GenerateResult gr = new GenerateResult();

		{
			Compilation             ccc = mod.parent;
			@NotNull EOT_OutputTree cot = ccc.getOutputTree();

			for (GeneratedNode generatedNode : lgc) {
				if (generatedNode.module() != mod) continue; // README curious

				if (generatedNode instanceof GeneratedContainerNC) {
					final GeneratedContainerNC nc = (GeneratedContainerNC) generatedNode;

					// 1.
					nc.generateCode(generateC, gr);

					// 2.
					final @NotNull Collection<GeneratedNode> gn1 = generateC.functions_to_list_of_generated_nodes(nc.functionMap.values());
					GenerateResult                           gr2 = generateC.generateCode(gn1, wm);
					gr.additional(gr2);

					// 3.
					final @NotNull Collection<GeneratedNode> gn2 = generateC.classes_to_list_of_generated_nodes(nc.classMap.values());
					GenerateResult                           gr3 = generateC.generateCode(gn2, wm);
					gr.additional(gr3);
				} else {
					Stupidity.println_out("2009 " + generatedNode.getClass().getName());
				}
			}
		}

		wm.drain();

		aPipelineLogic.gr.additional(gr);
	}

	public interface AB_ModuleListListener {
		void mods_slot(final EIT_ModuleList aModuleList);
	}

	public interface AB_LgcListener {
		void lgc_slot(List<GeneratedNode> lgc);
	}

	public interface AB_GenerateResultListener {
		void gr_slot(GenerateResult gr);
	}
}
