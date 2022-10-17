package tripleo.elijah.entrypoints;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EntryPointList {

	final @NotNull List<EntryPoint> eps;

	@Contract(pure = true)
	public EntryPointList() {
		eps = new ArrayList<>();
	}

	private void generateFromEntryPoints(DeducePhase deducePhase,
										 GenerateFunctions aGenerateFunctions,
										 WorkManager wm) {
		if (eps.size() == 0) return; // short circuit


		final WorkList wl = new WorkList();

		for (final EntryPoint entryPoint : eps) {
			final EntryPointProcessor epp = EntryPointProcessor.dispatch(entryPoint, deducePhase, wl, aGenerateFunctions);
			epp.process();
		}

		wm.addJobs(wl);
		wm.drain();
	}

	public void generate(@NotNull GenerateFunctions aGenerateFunctions, DeducePhase aDeducePhase, @NotNull Supplier<WorkManager> wm) {
		generateFromEntryPoints(aDeducePhase, aGenerateFunctions, wm.get());
	}

	public void add(EntryPoint aEntryPoint) {
		eps.add(aEntryPoint);
	}

}
