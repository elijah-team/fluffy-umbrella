package tripleo.elijah.comp;

import tripleo.elijah.stages.logging.ElLog;

import java.util.List;

public interface CompilationShit {
	Compilation getComp();

	boolean getSilent();

	void addLog(ElLog aLog);

	void writeLogs(boolean aSilent);

	PipelineLogic getPipelineLogic();

	List<ElLog> getLogs();
}
