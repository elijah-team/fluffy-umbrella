package tripleo.elijah.comp.internal;

import tripleo.elijah.comp.AccessBus;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.i.IPipelineAccess;

public interface ProcessRecord {
	PipelineLogic pipelineLogic();

	IPipelineAccess pa();

	ICompilationAccess                         ca();

	AccessBus ab();

	void writeLogs();
}