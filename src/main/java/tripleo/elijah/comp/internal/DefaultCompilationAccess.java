package tripleo.elijah.comp.internal;

import io.reactivex.rxjava3.functions.Consumer;
import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.PipelineMember;
import tripleo.elijah.comp.Stages;
import tripleo.elijah.comp.notation.GN_WriteLogs;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.stages.deduce.IFunctionMapHook;
import tripleo.elijah.stages.gen_fn.DeferredObject2;
import tripleo.elijah.stages.logging.ElLog;

import java.util.List;

public class DefaultCompilationAccess implements ICompilationAccess {
	protected final Compilation                                compilation;
	private         DeferredObject2<PipelineLogic, Void, Void> pipelineLogicDeferred = new DeferredObject2<>();

	public DefaultCompilationAccess(final Compilation aCompilation) {
		compilation = aCompilation;
	}

	void registerPipelineLogic(final Consumer<PipelineLogic> aPipelineLogicConsumer) {
		pipelineLogicDeferred.then(new DoneCallback<PipelineLogic>() {
			@Override
			public void onDone(final PipelineLogic result) {
				try {
					aPipelineLogicConsumer.accept(result);
				} catch (Throwable aE) {
					throw new RuntimeException(aE);
				}
			}
		});
	}

	@Override
	public void setPipelineLogic(final PipelineLogic pl) {
		assert compilation.getCompilationEnclosure().getPipelineLogic() == null;
		compilation.getCompilationEnclosure().setPipelineLogic(pl);

		compilation.pipelineLogic = pl;

//		pipelineLogicDeferred.resolve(pl);

//		compilation.pr.setGenerateResult(pl.gr);
	}

	@Override
	public void addPipeline(final PipelineMember pl) {
		compilation.addPipeline(pl);
	}

	@Override
	@NotNull
	public ElLog.Verbosity testSilence() {
		return compilation.cfg.silent ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;
	}

	@Override
	public Compilation getCompilation() {
		return compilation;
	}

	@Override
	public void writeLogs() {
		compilation.pa().notate(92, new GN_WriteLogs(this, compilation.getCompilationEnclosure().getPipelineLogic().elLogs));
	}

	@Override
	public List<IFunctionMapHook> functionMapHooks() {
		return compilation.getCompilationEnclosure().getPipelineLogic().dp.functionMapHooks;
	}


	@Override
	public Stages getStage() {
		return Stages.O;
	}
}
