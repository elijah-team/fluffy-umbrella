package tripleo.elijah.comp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.reactivex.rxjava3.functions.Consumer;
import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.functionality.f202.F202;
import tripleo.elijah.stages.deduce.FunctionMapHook;
import tripleo.elijah.stages.gen_fn.DeferredObject2;
import tripleo.elijah.stages.logging.ElLog;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
		//final boolean isSilent = compilation.silent; // TODO No such thing. silent is a local var
		final boolean isSilent = false; // TODO fix this

		return isSilent ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;
	}

	@Override
	public Compilation getCompilation() {
		return compilation;
	}

	@Override
	public void writeLogs() {
		final boolean silent = testSilence() == ElLog.Verbosity.SILENT;

		__writeLogs(silent, compilation.pipelineLogic.elLogs);
	}

	@Override
	public List<FunctionMapHook> functionMapHooks() {
		return compilation.pipelineLogic.dp.functionMapHooks;
	}













	@Override
	public Stages getStage() {
		return Stages.O;
	}















	private void __writeLogs(boolean aSilent, List<ElLog> aLogs) {
		Multimap<String, ElLog> logMap = ArrayListMultimap.create();
		if (true || aSilent) {
			for (ElLog deduceLog : aLogs) {
				logMap.put(deduceLog.getFileName(), deduceLog);
			}
			for (Map.Entry<String, Collection<ElLog>> stringCollectionEntry : logMap.asMap().entrySet()) {
				final F202 f202 = new F202(compilation.getErrSink(), compilation);
				f202.processLogs(stringCollectionEntry.getValue());
			}
		}
	}
}
