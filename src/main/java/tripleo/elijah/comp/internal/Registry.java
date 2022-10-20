package tripleo.elijah.comp.internal;

import tripleo.elijah.stages.logging.ElLog;

import java.util.List;

public interface Registry {
	// TODO: (java) excveptions ;)

	void loadPlan() throws Exception;

	void runPlan(final List<ElLog> elLogs) throws Exception;

	void GodsPlan();

	void runPlan() throws Exception;
}
