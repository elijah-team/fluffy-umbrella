package tripleo.elijah.stages.gen_generic.pipeline_impl;

import com.google.common.collect.ImmutableList;
import tripleo.elijah.comp.GeneratePipeline;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.elijah.world.i.LivingClass;

import java.util.ArrayList;
import java.util.List;

public class DefaultGenerateResultSink implements GenerateResultSink {

	private final GeneratePipeline         generatePipeline;
	private final List<GenerateResultItem> gris = new ArrayList<>();
	private final IPipelineAccess pa;

	public DefaultGenerateResultSink(final GeneratePipeline aGeneratePipeline, IPipelineAccess pa0) {
		generatePipeline = aGeneratePipeline;
		pa=pa0;
	}


	@Override
	public void add(EvaNode node) {
		int y = 2;
	}

	@Override
	public void additional(final GenerateResult aGenerateResult) {
		for (GenerateResultItem result : aGenerateResult.results()) {
			gris.add(result);
			add(result.node);
		}
	}

	@Override
	public LivingClass getClass(final EvaClass aEvaClass) {
		return pa.getCompilation()._repo.getClass(aEvaClass);
	}

	public List<GenerateResultItem> resultList() {
		return ImmutableList.copyOf(gris);
	}

}
