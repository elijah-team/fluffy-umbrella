package tripleo.elijah.stages.write_stage.pipeline_impl;

import org.apache.commons.lang3.tuple.Pair;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.comp.WritePipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class WP_Flow {
	public enum FlowStatus {
		NOT_TRIED, TRIED, FAILED
	}

	private final WritePipeline                                                    writePipeline;
	private final HashMap<WP_Indiviual_Step, Pair<FlowStatus, Operation<Boolean>>> ops = new HashMap<WP_Indiviual_Step, Pair<FlowStatus, Operation<Boolean>>>();

	private final List<WP_Indiviual_Step> steps = new ArrayList<>();

	public WP_Flow(final WritePipeline aWritePipeline, final Collection<? extends WP_Indiviual_Step> s) {
		writePipeline = aWritePipeline;
		steps.addAll(s);
	}

	public HashMap<WP_Indiviual_Step, Pair<FlowStatus, Operation<Boolean>>> act() {
		final WP_State_Control_1 sc = new WP_State_Control_1();

		for (final WP_Indiviual_Step step : steps) {
			ops.put(step, Pair.of(FlowStatus.NOT_TRIED, null));
		}

		for (final WP_Indiviual_Step step : steps) {
			sc.clear();

			step.act(writePipeline.st, sc);

			if (sc.hasException()) {
				ops.put(step, Pair.of(FlowStatus.FAILED, Operation.failure(sc.getException())));
				break;
			} else {
				ops.put(step, Pair.of(FlowStatus.TRIED, Operation.success(true)));
			}
		}

		return ops;
	}
}
