package tripleo.elijah.comp.i;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.DeducePipeline;

public interface IPipelineAccess {
	Compilation getCompilation();

	DeducePipeline getDeducePipeline();
}
