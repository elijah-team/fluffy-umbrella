package tripleo.elijah.stages.deduce.pipeline_impl;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.util.CompletableProcess;
import tripleo.elijah.util.Eventual;
import tripleo.elijah.world.i.WorldModule;

import java.util.Collection;

class PL_AddModules implements PipelineLogicRunnable {
	private final Collection<WorldModule> ml;

	private final Eventual<PipelineLogic> plp = new Eventual<>();

	@Contract(pure = true)
	public PL_AddModules(final @NotNull IPipelineAccess aPipelineAccess) {
		var w = aPipelineAccess.getCompilation().world();

		w.addModuleProcess(new CompletableProcess<WorldModule>() {
			@Override
			public void add(final WorldModule item) {
				plp.then(pipelineLogic -> pipelineLogic.addModule(item));
			}

			@Override
			public void complete() {

			}

			@Override
			public void error(final Diagnostic d) {

			}

			@Override
			public void preComplete() {

			}

			@Override
			public void start() {

			}
		});

		ml = aPipelineAccess.getCompilation().world().modules();
	}

	@Override
	public void run(final @NotNull PipelineLogic pipelineLogic) {
		plp.resolve(pipelineLogic);
	}
}
