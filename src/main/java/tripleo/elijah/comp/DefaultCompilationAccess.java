package tripleo.elijah.comp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.functionality.f202.F202;
import tripleo.elijah.stages.gen_fn.DeferredObject2;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.testing.comp.IFunctionMapHook;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DefaultCompilationAccess implements ICompilationAccess {
	protected final Compilation                                compilation;
	private final   DeferredObject2<PipelineLogic, Void, Void> pipelineLogicDeferred = new DeferredObject2<>();

	public DefaultCompilationAccess(final Compilation aCompilation) {
		compilation = aCompilation;

		compilation.getCompilationEnclosure().provideCompilationAccess(this);
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

		writeLogs(silent, compilation.elLogs);
	}

	@Override
	public List<IFunctionMapHook> functionMapHooks() {
		return compilation.getDeducePhase().functionMapHooks;
	}

	@Override
	public Stages getStage() {
		return getCompilation().cfg.stage;
	}

	private void writeLogs(final boolean aSilent, final List<ElLog> aLogs) {
		final Multimap<String, ElLog> logMap = ArrayListMultimap.create();
		if (true) {
			for (final ElLog deduceLog : aLogs) {
				logMap.put(deduceLog.getFileName(), deduceLog);
			}
			for (final Map.Entry<String, Collection<ElLog>> stringCollectionEntry : logMap.asMap().entrySet()) {
				final F202 f202 = new F202(compilation.getErrSink(), compilation);
				f202.processLogs(stringCollectionEntry.getValue());
			}
		}
	}
}
