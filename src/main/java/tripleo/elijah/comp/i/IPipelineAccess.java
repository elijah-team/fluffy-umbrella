package tripleo.elijah.comp.i;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.DeducePipeline;
import tripleo.elijah.comp.PipelineLogic;

public interface IPipelineAccess {
	Compilation getCompilation();

	DeducePipeline getDeducePipeline();

	PipelineLogic pipelineLogic();
}
