package tripleo.elijah.nextgen.inputtree;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.DeducePipeline;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.entrypoints.EntryPointList;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.util.Stupidity;
import tripleo.elijah.work.WorkManager;
import tripleo.elijah.world.i.WorldModule;
import tripleo.elijah.world.impl.DefaultWorldModule;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EIT_ModuleList {
	public static class _ProcessParams {
		private final          OS_Module         mod;
		private final          PipelineLogic     pipelineLogic;
		private final          GenerateFunctions gfm;

		private final          DeducePhase    deducePhase;

		private final @NotNull EntryPointList epl;
		//private final @NotNull ElLog.Verbosity   verbosity;
		private _ProcessParams(@NotNull final OS_Module aModule,
		                       @NotNull final PipelineLogic aPipelineLogic,
		                       @NotNull final GenerateFunctions aGenerateFunctions,
		                       @NotNull final EntryPointList aEntryPointList,
		                       @NotNull final DeducePhase aDeducePhase) {
			mod           = aModule;
			pipelineLogic = aPipelineLogic;
			gfm           = aGenerateFunctions;
			epl           = aEntryPointList;
			deducePhase   = aDeducePhase;
//			verbosity = mod.getCompilation().pipelineLogic.getVerbosity();
		}

		public void deduceModule() {
			var wm = new DefaultWorldModule(mod, null);
			deducePhase.deduceModule(wm, getLgc(), getVerbosity());
		}

		public void generate() {
			epl.generate(gfm, deducePhase, getWorkManagerSupplier());
		}

		public DeducePhase getDeducePhase() {
			return deducePhase;
		}

		@Contract(pure = true)
		public DeducePhase.GeneratedClasses getLgc() {
			return deducePhase.generatedClasses;
		}

		//
		//
		//

		@Contract(pure = true)
		public OS_Module getMod() {
			return mod;
		}

		@Contract(pure = true)
		public ElLog.@NotNull Verbosity getVerbosity() {
			return pipelineLogic.getVerbosity();
		}

		@Contract(pure = true)
		public @NotNull Supplier<WorkManager> getWorkManagerSupplier() {
			return () -> pipelineLogic.generatePhase.wm;
		}
	}

	private final List<OS_Module> mods;
//	private PipelineLogic __pl;

	@Contract(pure = true)
	public EIT_ModuleList(final List<OS_Module> aMods) {
		mods = aMods;
	}

	private void __process__PL__each(final @NotNull _ProcessParams plp) {
		var env = new PL_Each_Env(plp);
		__process__PL__each(env);
	}

	private void __process__PL__each(final @NotNull PL_Each_Env env) {
		final _ProcessParams               plp            = env.getProcessParams();

		final DeducePipeline.ResolvedNodes resolved_nodes = env.getResolvedNodes();

		final OS_Module                    mod            = plp.getMod();

		final DeducePhase.GeneratedClasses lgc            = plp.getLgc();
		final int                          size           = lgc.size();

		// assert lgc.size() == 0;

		if (size != 0) {
			NotImplementedException.raise();
			Stupidity.println_err(String.format("lgc.size() != 0: %d", size));
		}

		plp.generate();

		//assert lgc.size() == epl.size(); //hmm

		final ICodeRegistrar codeRegistrar = env.getCodeRegistrar();

		resolved_nodes.initial_feed(mod, lgc.copy(), codeRegistrar);

		plp.deduceModule();

		PipelineLogic.resolveCheck(lgc);

//			for (final GeneratedNode gn : lgf) {
//				if (gn instanceof EvaFunction) {
//					EvaFunction gf = (EvaFunction) gn;
//					tripleo.elijah.util.Stupidity.println2("----------------------------------------------------------");
//					tripleo.elijah.util.Stupidity.println2(gf.name());
//					tripleo.elijah.util.Stupidity.println2("----------------------------------------------------------");
//					EvaFunction.printTables(gf);
//					tripleo.elijah.util.Stupidity.println2("----------------------------------------------------------");
//				}
//			}
	}

	public void add(final OS_Module m) {
		mods.add(m);
	}

//	public void _set_PL(final PipelineLogic aPipelineLogic) {
//		__pl = aPipelineLogic;
//	}

	public List<OS_Module> getMods() {
		return mods;
	}

	public List<WorldModule> getMods2() {
		return this.mods.stream()
		  .map(mod -> new DefaultWorldModule(mod, null))
		  .collect(Collectors.toList());
	}

	public void process__PL(final Function<OS_Module, GenerateFunctions> ggf, final PipelineLogic pipelineLogic) {
		for (final OS_Module mod : mods) {
			final @NotNull EntryPointList epl = mod.entryPoints;

			if (epl.size() == 0) {
				continue;
			}


			final GenerateFunctions gfm = ggf.apply(mod);

			final DeducePhase deducePhase = pipelineLogic.dp;
			//final DeducePhase.@NotNull GeneratedClasses lgc            = deducePhase.generatedClasses;

			final _ProcessParams plp = new _ProcessParams(mod, pipelineLogic, gfm, epl, deducePhase);

			__process__PL__each(plp);
		}
	}

	public Stream<OS_Module> stream() {
		return mods.stream();
	}
}
