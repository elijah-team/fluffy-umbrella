package tripleo.elijah.stages.deduce.pipeline_impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.EvaNode;

import java.util.ArrayList;
import java.util.List;

public class DeducePipelineImpl {
	private final List<PipelineLogicRunnable> plrs = new ArrayList<>();
	private final IPipelineAccess             pa;
	public        List<EvaNode>               lgc  = new ArrayList<EvaNode>();

	public DeducePipelineImpl(final @NotNull IPipelineAccess pa0) {
		pa = pa0;

		final Compilation c = pa.getCompilation();
		for (final OS_Module module : c.modules) {
			addRunnable(new PL_AddModule(module));
		}

		addRunnable(new PL_EverythingBeforeGenerate(this));
		addRunnable(new PL_SaveGeneratedClasses(this));
	}

	private void addRunnable(final PipelineLogicRunnable plr) {
		plrs.add(plr);
	}

	public void run() {
		final Compilation c = pa.getCompilation();

		assert c.pipelineLogic != null;

		setPipelineLogic(c.pipelineLogic);
	}

	public void setPipelineLogic(final PipelineLogic aPipelineLogic) {
		for (final PipelineLogicRunnable plr : plrs) {
			plr.run(aPipelineLogic);
		}
	}

	public void saveGeneratedClasses(final List<EvaNode> aEvaNodeList) {
		pa.setNodeList(aEvaNodeList);
		lgc = aEvaNodeList;
	}
}
