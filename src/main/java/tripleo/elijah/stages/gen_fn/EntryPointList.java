package tripleo.elijah.stages.gen_fn;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.entrypoints.EntryPoint;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;

import java.util.List;

class EntryPointList {

	final @NotNull List<EntryPoint> epl;

	EntryPointList(@NotNull List<EntryPoint> aEpl) {
		epl = aEpl;
	}

	public void generateFromEntryPoints(DeducePhase deducePhase, GenerateFunctions aGenerateFunctions, WorkManager wm) {
		final WorkList wl = new WorkList();

		for (final EntryPoint entryPoint : epl) {
			final EntryPointProcessor epp = EntryPointProcessor.dispatch(entryPoint, deducePhase, wl, aGenerateFunctions);
			epp.process();
		}

		wm.addJobs(wl);
		wm.drain();
	}

}
