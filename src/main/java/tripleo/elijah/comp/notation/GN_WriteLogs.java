package tripleo.elijah.comp.notation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.functionality.f202.F202;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.internal.DefaultCompilationAccess;
import tripleo.elijah.stages.logging.ElLog;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GN_WriteLogs implements GN_Notable {
	private final ICompilationAccess ca;
	//private final boolean            silent;
	private final List<ElLog>        logs;

	@Contract(pure = true)
	public GN_WriteLogs(final @NotNull ICompilationAccess aCa,
						final @NotNull List<ElLog> aLogs) {
		ca     = aCa;
		//silent = aCa.testSilence() == ElLog.Verbosity.SILENT;
		logs   = aLogs;
	}

	@Override
	public void run() {
		final Multimap<String, ElLog> logMap = ArrayListMultimap.create();

		for (final ElLog deduceLog : logs) {
			logMap.put(deduceLog.getFileName(), deduceLog);
		}

			for (final Map.Entry<String, Collection<ElLog>> stringCollectionEntry : logMap.asMap().entrySet()) {
				final F202 f202 = new F202(ca.getCompilation().getErrSink(), ca.getCompilation());
				f202.processLogs(stringCollectionEntry.getValue());
			}
		}
	}
}
