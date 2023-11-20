/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.entrypoints.EntryPoint;
import tripleo.elijah.entrypoints.EntryPointList;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created 8/21/21 10:10 PM
 */
@SuppressWarnings("SimplifyStreamApiCallChains")
public class DeducePipeline implements PipelineMember, AccessBus.AB_ModuleListListener {
	private final AccessBus       __ab;
	private       PipelineLogic   pipelineLogic;
	private       List<OS_Module> ms;

	public DeducePipeline(final @NotNull AccessBus ab) {
		__ab = ab;

		ab.subscribePipelineLogic(result -> pipelineLogic = result);
	}

	@Override
	public void run() {
		// TODO move this into a latch and wait for pipelineLogic and modules

//		final List<GeneratedNode> lgc = pipelineLogic.generatedClassesCopy();

		resolveMods();

		final Function<OS_Module, PL_Run2> pl_run2_er = mod -> {
			final List<EntryPoint> entryPoints = mod.entryPoints._getMods();
			final PL_Run2          pl_run2     = new PL_Run2(mod, entryPoints, pipelineLogic::getGenerateFunctions, pipelineLogic);
			return pl_run2;
		};

		final List<PL_Run2> run2_work = pipelineLogic.mods.stream()
		                                                  .map(pl_run2_er)
		                                                  .collect(Collectors.toList());

		final List<DeducePhase.GeneratedClasses> lgc2 = run2_work.stream()
		                                                         .map(PL_Run2::run2)
		                                                         .collect(Collectors.toList());

		final List<GeneratedNode> lgc3 = new ArrayList<>();

		// TODO how to do this with streams
		for (final DeducePhase.GeneratedClasses generatedClasses : lgc2) {
			for (final GeneratedNode generatedClass : generatedClasses) {
				lgc3.add(generatedClass);
			}
		}

		__ab.resolveLgc(lgc3);
	}

	public void resolveMods() {
//		__ab.resolveModuleList(ms);
	}

	@Override
	public void mods_slot(final @NotNull EIT_ModuleList aModuleList) {
		final List<OS_Module> mods = aModuleList.getMods();

		ms = mods;
	}

	static class PL_Run2 {
		private final OS_Module                              mod;
		private final List<EntryPoint>                       entryPoints;
		private final Function<OS_Module, GenerateFunctions> mapper;
		private final PipelineLogic                          pipelineLogic;

		public PL_Run2(final OS_Module mod,
		               final List<EntryPoint> entryPoints,
		               final Function<OS_Module, GenerateFunctions> mapper,
		               final PipelineLogic pipelineLogic) {
			this.mod           = mod;
			this.entryPoints   = entryPoints;
			this.mapper        = mapper;
			this.pipelineLogic = pipelineLogic;
		}

		protected DeducePhase.@NotNull GeneratedClasses run2() {
			final GenerateFunctions gfm         = mapper.apply(mod);
			final DeducePhase       deducePhase = pipelineLogic.dp;

			{
				@NotNull final EntryPointList epl = new EntryPointList(mod);

				entryPoints.stream().forEach(epl::add);

				gfm.generateFromEntryPoints(epl, deducePhase);
			}

			final List<GeneratedNode> lgc = pipelineLogic.generatedClassesCopy();

			final ICodeRegistrar codeRegistrar  = deducePhase.codeRegistrar;
			final ResolvedNodes  resolved_nodes = new ResolvedNodes();

			resolved_nodes.initial_feed(mod, lgc, codeRegistrar);
			resolved_nodes.code_resolved();


			deducePhase.deduceModule(mod, lgc, true, pipelineLogic.getVerbosity());

//			PipelineLogic.resolveCheck(lgc);

//		for (final GeneratedNode gn : lgf) {
//			if (gn instanceof GeneratedFunction) {
//				GeneratedFunction gf = (GeneratedFunction) gn;
//				tripleo.elijah.util.Stupidity.println2("----------------------------------------------------------");
//				tripleo.elijah.util.Stupidity.println2(gf.name());
//				tripleo.elijah.util.Stupidity.println2("----------------------------------------------------------");
//				GeneratedFunction.printTables(gf);
//				tripleo.elijah.util.Stupidity.println2("----------------------------------------------------------");
//			}
//		}

			return deducePhase.generatedClasses; // NOTE .clone/immutable, etc
		}
	}

	public static class ResolvedNodes {
		private       tripleo.elijah.comp.Coder coder;
		private final List<GeneratedNode>       resolved_nodes = new ArrayList<GeneratedNode>();
		private       OS_Module           mod;

		public void addAll(final List<GeneratedNode> aGeneratedNodes) {
			resolved_nodes.addAll(aGeneratedNodes);
		}

		public void forEach(final Consumer<GeneratedNode> cgn) {
			resolved_nodes.forEach(cgn);
		}

		public tripleo.elijah.comp.Coder initial_feed(final OS_Module aMod, final List<GeneratedNode> lgc, final ICodeRegistrar codeRegistrar) {
			coder = new Coder(codeRegistrar);
			mod   = aMod;

			lgc.stream().forEach(generatedNode -> coder.codeNodes(mod, resolved_nodes, generatedNode));

			return coder;
		}

		public void code_resolved() {
			resolved_nodes.forEach(generatedNode -> coder.codeNode(generatedNode, mod));
		}

		public List<GeneratedNode> _nodeList() {
			return this.resolved_nodes;
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
