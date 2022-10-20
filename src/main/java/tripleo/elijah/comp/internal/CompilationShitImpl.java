package tripleo.elijah.comp.internal;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationShit;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.functionality.f202.F202;
import tripleo.elijah.stages.logging.ElLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CompilationShitImpl implements CompilationShit {
	private final Compilation c;

	public CompilationShitImpl(final Compilation aC) {
		this.c = aC;
	}

	@Override
	public Compilation getComp() {
		return c;
	}

	@Override
	public boolean getSilent() {
		return ((CompilationImpl) c).silent();
	}

	@Override
	public void addLog(ElLog aLog) {
		logs.add(aLog);
	}

	@Override
	public void writeLogs(boolean aSilent) {
		final ErrSink errSink = getComp().getErrSink();
		final Multimap<String, ElLog> logMap = ArrayListMultimap.create();
		final boolean cond = true || aSilent; // TODO

		if (!cond) return;

		// TODO huh?
		for (final ElLog deduceLog : logs) {
			logMap.put(deduceLog.getFileName(), deduceLog);
		}

		final F202 f202 = new F202(errSink, getComp());
		for (Map.Entry<String, Collection<ElLog>> stringCollectionEntry : logMap.asMap().entrySet()) {
			f202.processLogs(stringCollectionEntry.getValue());
		}
	}

	@Override
	public PipelineLogic getPipelineLogic() {
		throw new IllegalArgumentException();
//		return null;
	}

	@Override
	public List<ElLog> getLogs() {
		return logs;
	}

	private final List<ElLog> logs = new ArrayList<>();
}
