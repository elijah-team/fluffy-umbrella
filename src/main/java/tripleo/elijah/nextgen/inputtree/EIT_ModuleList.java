package tripleo.elijah.nextgen.inputtree;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Coder;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.entrypoints.EntryPointList;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Stupidity;
import tripleo.elijah.work.WorkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EIT_ModuleList {
	private final List<OS_Module> mods;
	private PipelineLogic __pl;

	@Contract(pure = true)
	public EIT_ModuleList(final List<OS_Module> aMods) {
		mods = aMods;
	}

	public List<OS_Module> getMods() {
		return mods;
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

	private void __process__PL__each(final @NotNull _ProcessParams plp) {
		final List<GeneratedNode> resolved_nodes = new ArrayList<GeneratedNode>();

//		assert lgc.size() == 0;

		final int size = plp.getLgc().size();
		if (size != 0) {
			final int y = 2;
			Stupidity.println_err(String.format("lgc.size() != 0: %d", size));
		}

		plp.generate();

		//assert lgc.size() == epl.size(); //hmm

		final Coder coder = new Coder();

		for (final GeneratedNode generatedNode : plp.getLgc()) {
			coder.codeNodes(plp.getMod(), resolved_nodes, generatedNode);
		}

		resolved_nodes.forEach(generatedNode -> coder.codeNode(generatedNode, plp.getMod()));

		plp.deduceModule();

		plp.lgcThing();

//			for (final GeneratedNode gn : lgf) {
//				if (gn instanceof GeneratedFunction) {
//					GeneratedFunction gf = (GeneratedFunction) gn;
//					System.out.println("----------------------------------------------------------");
//					System.out.println(gf.name());
//					System.out.println("----------------------------------------------------------");
//					GeneratedFunction.printTables(gf);
//					System.out.println("----------------------------------------------------------");
//				}
//			}
	}

	public void _set_PL(final PipelineLogic aPipelineLogic) {
		__pl = aPipelineLogic;
	}

	public Stream<OS_Module> stream() {
		return mods.stream();
	}

	public void add(final OS_Module m) {
		mods.add(m);
	}

	private static class _ProcessParams {
		private final OS_Module mod;
		private PipelineLogic pipelineLogic;
		private final GenerateFunctions gfm;
		@NotNull
		private final EntryPointList epl;
		private final DeducePhase deducePhase;
//		@NotNull
//		private final ElLog.Verbosity                         verbosity;

		private _ProcessParams(@NotNull final OS_Module aModule,
							   @NotNull final PipelineLogic aPipelineLogic,
							   @NotNull final GenerateFunctions aGenerateFunctions,
							   @NotNull final EntryPointList aEntryPointList,
							   @NotNull final DeducePhase aDeducePhase) {
			mod = aModule;
			pipelineLogic = aPipelineLogic;
			gfm = aGenerateFunctions;
			epl = aEntryPointList;
			deducePhase = aDeducePhase;
//			verbosity = mod.getCompilation().pipelineLogic.getVerbosity();
		}

		@Contract(pure = true)
		public OS_Module getMod() {
			return mod;
		}

//		public GenerateFunctions getGfm() {
//			return gfm;
//		}
//
//		public EntryPointList getEpl() {
//			return epl;
//		}
//
//		public DeducePhase getDeducePhase() {
//			return deducePhase;
//		}

		@Contract(pure = true)
		public DeducePhase.GeneratedClasses getLgc() {
			return deducePhase.generatedClasses;
		}

		@Contract(pure = true)
		public @NotNull Supplier<WorkManager> getWorkManagerSupplier() {
			return () -> pipelineLogic.generatePhase.wm;
		}

		@Contract(pure = true)
		public ElLog.@NotNull Verbosity getVerbosity() {
//			return verbosity;
			return /*mod.getCompilation().*/pipelineLogic.getVerbosity();
		}

		//
		//
		//

		public void generate() {
			epl.generate(gfm, deducePhase, getWorkManagerSupplier());
		}

		public void deduceModule() {
			deducePhase.deduceModule(mod, getLgc(), getVerbosity());
		}

		public void lgcThing() {
			pipelineLogic.resolveCheck(getLgc());
		}
	}
}
