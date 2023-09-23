package tripleo.elijah.stages.deduce.pipeline_impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.i.IPipelineAccess;

import java.util.ArrayList;
import java.util.List;

public class DeducePipelineImpl {
	static class DeducePipelineImplInjector {
		public PipelineLogicRunnable new_PL_AddModules(final @NotNull IPipelineAccess aPipelineAccess) {
			return new PL_AddModules(aPipelineAccess);
		}

		public PipelineLogicRunnable new_PL_EverythingBeforeGenerate() {
			return new PL_EverythingBeforeGenerate();
		}

		public PipelineLogicRunnable new_PL_SaveGeneratedClasses(final IPipelineAccess aPa) {
			return new PL_SaveGeneratedClasses(aPa);
		}
	}
	private final @NotNull IPipelineAccess             pa;

	@SuppressWarnings("TypeMayBeWeakened")
	private final          List<PipelineLogicRunnable> plrs = new ArrayList<>();

	private final DeducePipelineImplInjector __inj = new DeducePipelineImplInjector();

	public DeducePipelineImpl(final @NotNull IPipelineAccess pa0) {
		pa = pa0;

		addRunnable(_inj().new_PL_AddModules(pa));
		addRunnable(_inj().new_PL_EverythingBeforeGenerate());
		addRunnable(_inj().new_PL_SaveGeneratedClasses(pa));
	}

	private DeducePipelineImplInjector _inj() {
		return __inj;
	}

	private void addRunnable(final PipelineLogicRunnable plr) {
		plrs.add(plr);
	}

	public void run() {
		final Compilation          c                    = pa.getCompilation();
		final CompilationEnclosure compilationEnclosure = c.getCompilationEnclosure();
		final PipelineLogic        pipelineLogic        = compilationEnclosure.getPipelineLogic();

		assert pipelineLogic != null;

		for (final PipelineLogicRunnable plr : plrs) {
			plr.run(pipelineLogic);
		}
	}
}
