package tripleo.elijah.stages.deduce.pipeline_impl;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.GeneratedNode;

import java.util.ArrayList;
import java.util.List;

public class DeducePipelineImpl {
	private final Compilation                 c;
	private final List<PipelineLogicRunnable> plrs = new ArrayList<>();
	public        List<GeneratedNode>         lgc  = new ArrayList<GeneratedNode>();

	public DeducePipelineImpl(final Compilation aCompilation) {
		c = aCompilation;

		for (final OS_Module module : c.modules) {
			addRunnable(new PL_AddModule(module));
		}

		addRunnable(new PL_EverythingBeforeGenerate(this));
		addRunnable(new PL_SaveGeneratedClasses(this));
	}

	public void run() {
		assert c.pipelineLogic != null;

		setPipelineLogic(c.pipelineLogic);
	}

	public void setPipelineLogic(final PipelineLogic aPipelineLogic) {
		for (final PipelineLogicRunnable plr : plrs) {
			plr.run(aPipelineLogic);
		}
	}

	private void addRunnable(final PipelineLogicRunnable plr) {
		plrs.add(plr);
	}

	public void saveGeneratedClasses(final List<GeneratedNode> aGeneratedNodeList) {
		lgc = aGeneratedNodeList;
	}
}
