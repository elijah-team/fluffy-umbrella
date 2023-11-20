package tripleo.elijah.comp;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation.CompilationAlways;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.stages.gen_fn.GeneratedContainerNC;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.OutputFileFactory;
import tripleo.elijah.stages.gen_generic.OutputFileFactoryParams;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Stupidity;
import tripleo.elijah.work.WorkManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccessBus {
	public final  GenerateResult                                  gr                    = new GenerateResult();
	private final Compilation                                     _c;
	private final DeferredObject<PipelineLogic, Void, Void>       pipeLineLogicPromise  = new DeferredObject<>();
	private final DeferredObject<List<GeneratedNode>, Void, Void> lgcPromise            = new DeferredObject<>();
	private final DeferredObject<EIT_ModuleList, Void, Void>      moduleListPromise     = new DeferredObject<>();
	final         DeferredObject<GenerateResult, Void, Void>      generateResultPromise = new DeferredObject<>();
	private final Map<String, ProcessRecord.PipelinePlugin>       pipelinePlugins       = new HashMap<>();
	private       PipelineLogic                                   ____pl;


	public AccessBus(final Compilation aC) {
		_c = aC;
	}

	public @NotNull Compilation getCompilation() {
		return _c;
	}

	public void subscribePipelineLogic(final DoneCallback<PipelineLogic> aPipelineLogicDoneCallback) {
		pipeLineLogicPromise.then(aPipelineLogicDoneCallback);
	}

	private void resolvePipelineLogic(final PipelineLogic pl) {
		pipeLineLogicPromise.resolve(pl);
	}

	@Deprecated
	public void resolveModuleList(final List<OS_Module> aModuleList) {
		resolveModuleList(new EIT_ModuleList(aModuleList)); // TODO
	}

	public void resolveModuleList(final EIT_ModuleList aModuleList) {
		moduleListPromise.resolve(aModuleList);
	}

	public void resolveGenerateResult(final GenerateResult aGenerateResult) {
		generateResultPromise.resolve(aGenerateResult);
	}

	public void resolveLgc(final List<GeneratedNode> lgc) {
		lgcPromise.resolve(lgc);
	}

	public void add(final @NotNull Function<AccessBus, PipelineMember> aCr) {
		final PipelineMember x = aCr.apply(this);
		_c.getPipelines().add(x);
	}

	public void addPipelineLogic(final @NotNull Function<AccessBus, PipelineLogic> aPlr) {
		final PipelineLogic x = aPlr.apply(this);

		____pl = x;

		resolvePipelineLogic(x);
	}

	public void subscribe_lgc(@NotNull final AB_LgcListener aLgcListener) {
		lgcPromise.then(aLgcListener::lgc_slot);
	}

	public void subscribe_moduleList(@NotNull final AB_ModuleListListener aModuleListListener) {
		moduleListPromise.then(aModuleListListener::mods_slot);
	}

	public void subscribe_GenerateResult(@NotNull final AB_GenerateResultListener aGenerateResultListener) {
		generateResultPromise.then(aGenerateResultListener::gr_slot);
	}

	void doModule(final @NotNull List<GeneratedNode> lgc,
	              final @NotNull WorkManager wm,
	              final @NotNull OS_Module mod,
	              final @NotNull PipelineLogic aPipelineLogic,
	              final @NotNull ErrSink aErrSink) {
		final ErrSink         errSink   = mod.parent.getErrSink();
		final ElLog.Verbosity verbosity = aPipelineLogic.getVerbosity();

		final OutputFileFactoryParams p         = new OutputFileFactoryParams(mod, aErrSink, verbosity, aPipelineLogic);
		final GenerateFiles           generateC = OutputFileFactory.create(CompilationAlways.defaultPrelude(), p);

		final Compilation             ccc = mod.parent;
		@NotNull final EOT_OutputTree cot = ccc.getOutputTree();

		for (final GeneratedNode generatedNode : lgc) {
			if (generatedNode.module() != mod) continue; // README curious

			if (generatedNode instanceof final GeneratedContainerNC nc) {

				// 1.
				nc.generateCode(generateC, gr);

				// 2.
				final @NotNull Collection<GeneratedNode> gn1 = (nc.functionMap.values()).stream().map(x -> (GeneratedNode) x).collect(Collectors.toList());
				final GenerateResult                     gr2 = generateC.generateCode(gn1, wm);
				gr.additional(gr2);

				// 3.
				final @NotNull Collection<GeneratedNode> gn2 = (nc.classMap.values()).stream().map(x -> (GeneratedNode) x).collect(Collectors.toList());
				final GenerateResult                     gr3 = generateC.generateCode(gn2, wm);
				gr.additional(gr3);
			} else {
				Stupidity.println_out("2009 " + generatedNode.getClass().getName());
			}
		}

		wm.drain();

//		gr.additional(grx);
	}

	public void writeLogs() {
		@NotNull final Compilation comp = getCompilation(); // this._c

		comp.writeLogs(comp.cfg.silent, comp.elLogs);
	}

	public PipelineLogic __getPL() {
		return ____pl; // TODO hack. remove soon
	}

	public void addPipelinePlugin(final ProcessRecord.PipelinePlugin aPlugin) {
		pipelinePlugins.put(aPlugin.name(), aPlugin);
	}

	public ProcessRecord.PipelinePlugin getPipelinePlugin(final String aPipelineName) {
		if (!(pipelinePlugins.containsKey(aPipelineName))) return null;

		return pipelinePlugins.get(aPipelineName);
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
