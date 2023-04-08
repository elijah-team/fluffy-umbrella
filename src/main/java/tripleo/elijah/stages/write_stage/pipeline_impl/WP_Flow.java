package tripleo.elijah.stages.write_stage.pipeline_impl;

import tripleo.elijah.comp.WritePipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WP_Flow {
	private final WritePipeline           writePipeline;
	private final List<WP_Indiviual_Step> steps = new ArrayList<>();

	public WP_Flow(final WritePipeline aWritePipeline, final Collection<? extends WP_Indiviual_Step> s) {
		writePipeline = aWritePipeline;
		steps.addAll(s);
	}

	WP_Flow(final WritePipeline aWritePipeline) {
		writePipeline = aWritePipeline;
		//steps.addAll(s);
	}

	public void act() throws Exception {
		final WP_State_Control_1 sc = new WP_State_Control_1();

		for (WP_Indiviual_Step step : steps) {
			sc.clear();

			step.act(writePipeline.st, sc);

			if (sc.hasException()) {
				throw sc.getException();
			}
		}
	}
}
