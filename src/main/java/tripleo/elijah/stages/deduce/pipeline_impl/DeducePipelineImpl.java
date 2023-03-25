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
			if (false) {
/*
				new DeduceTypes(module).deduce();
				for (final OS_Element2 item : module.items()) {
					if (item instanceof ClassStatement || item instanceof NamespaceStatement) {
						System.err.println("8001 "+item);
					}
				}
				new TranslateModule(module).translate();
*/
/*
				new ExpandFunctions(module).expand();

				final JavaCodeGen visit = new JavaCodeGen();
				module.visitGen(visit);
*/
			} else {
				//c.pipelineLogic.addModule(module);
				addRunnable(new PL_AddModule(module));
			}
		}

		addRunnable(new PL_EverythingBeforeGenerate(this));
		addRunnable(new PL_SaveGeneratedClasses(this));
	}

	public void run() {
		// TODO move "futures" to ctor...
		//c.pipelineLogic.everythingBeforeGenerate(lgc);
		//lgc = c.pipelineLogic.dp.generatedClasses.copy();

		// TODO wait for these two to finish...
		// TODO make sure you call #setPipelineLogic...

		assert c.pipelineLogic != null;

		setPipelineLogic(c.pipelineLogic);
	}

	public void setPipelineLogic(final PipelineLogic aPipelineLogic) {
		for (PipelineLogicRunnable plr : plrs) {
			plr.run(aPipelineLogic);
		}
	}

	private void addRunnable(final PipelineLogicRunnable plr) {
		plrs.add(plr);
	}

}
