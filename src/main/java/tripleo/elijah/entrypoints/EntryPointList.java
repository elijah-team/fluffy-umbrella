package tripleo.elijah.entrypoints;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Finally;
import tripleo.elijah.comp.FlowK;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EntryPointList {

	private final @NotNull List<EntryPoint> eps;
	private final OS_Module mod;

	@Contract(pure = true)
	public EntryPointList(final OS_Module aMod) {
		eps = new ArrayList<>();
		mod = aMod;
	}

	public void generate(@NotNull final GenerateFunctions aGenerateFunctions, final DeducePhase aDeducePhase, @NotNull final Supplier<WorkManager> wm) {
		generateFromEntryPoints(aDeducePhase, aGenerateFunctions, wm.get());
	}

	private void generateFromEntryPoints(final DeducePhase deducePhase,
	                                     final GenerateFunctions aGenerateFunctions,
	                                     final WorkManager wm) {
		if (eps.isEmpty()) {
			// flow
			flow(deducePhase).report(new FlowK.EntryPointList_generateFromEntryPoints__eps_isEmpty(mod));
			// flow
			return; // short circuit
		}

		final WorkList wl = new WorkList();

		// flow
		flow(deducePhase).report(new FlowK.EntryPointList_generateFromEntryPoints__epp_size(eps.size()));
		// flow

		for (final EntryPoint entryPoint : eps) {
			final EntryPointProcessor epp = EntryPointProcessor.dispatch(entryPoint, deducePhase, wl, aGenerateFunctions);

			// flow
			flow(deducePhase).report(new FlowK.EntryPointList_generateFromEntryPoints__epp_process__pre(epp));
			// flow

			epp.process();

			// flow
			flow(deducePhase).report(new FlowK.EntryPointList_generateFromEntryPoints__epp_process__post(epp));
			// flow
		}

		wm.addJobs(wl);
		wm.drain();
	}

	private Finally.Flow flow(final DeducePhase aDeducePhase) {
		var compilation = aDeducePhase._compilation();

		return compilation.flow();
	}

	public void add(final EntryPoint aEntryPoint) {
		eps.add(aEntryPoint);
	}

	public List<EntryPoint> _getMods() {
		return eps;
	}

	public int size() {
		return eps.size();
	}
}
