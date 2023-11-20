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
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_fn.GeneratedNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created 8/21/21 10:10 PM
 */
public class DeducePipeline implements PipelineMember, AccessBus.AB_ModuleListListener {
	private final AccessBus __ab;
	private PipelineLogic pipelineLogic;
	private List<OS_Module> ms;

	public DeducePipeline(final @NotNull AccessBus ab) {
		__ab = ab;

		ab.subscribePipelineLogic(result -> pipelineLogic = result);
	}

	@Override
	public void run() {
		// TODO move this into a latch and wait for pipelineLogic and modules

/*
		final List<OS_Module> ms1 = __ab.getCompilation().getModules();

		if (ms != null) tripleo.elijah.util.Stupidity.println_err2("ms.size() == " + ms.size());
		else tripleo.elijah.util.Stupidity.println_err2("ms == null");
		tripleo.elijah.util.Stupidity.println_err2("ms1.size() == " + ms1.size());
*/

		final List<GeneratedNode> lgc = pipelineLogic.generatedClassesCopy();

		resolveMods();

		final List<PL_Run2> run2_work = pipelineLogic.mods.stream()
		                                                  .map(mod -> new PL_Run2(mod,
		                                                    mod.entryPoints._getMods(),
		                                                    pipelineLogic::getGenerateFunctions,
		                                                    pipelineLogic))
		                                                  .collect(Collectors.toList());

		final List<DeducePhase.GeneratedClasses> lgc2 = run2_work.stream()
		                                                         .map(PL_Run2::run2)
		                                                         .collect(Collectors.toList());

		final ArrayList<GeneratedNode> lgc3 = new ArrayList<>();

		// TODO how to do this with streams
		for (DeducePhase.GeneratedClasses generatedClasses : lgc2) {
			for (GeneratedNode generatedClass : generatedClasses) {
				lgc3.add(generatedClass);
			}
		}

		__ab.resolveLgc(lgc3);
	}

	@Override
	public void mods_slot(final @NotNull EIT_ModuleList aModuleList) {
		final List<OS_Module> mods = aModuleList.getMods();

		ms = mods;
	}

	public void resolveMods() {
//		__ab.resolveModuleList(ms);
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

			gfm.generateFromEntryPoints(entryPoints, deducePhase);

			final List<GeneratedNode>          lgc            = pipelineLogic.generatedClassesCopy();
			@NotNull final List<GeneratedNode> resolved_nodes = new ArrayList<GeneratedNode>();

			final Coder coder = new Coder(deducePhase.codeRegistrar);

			lgc.stream().forEach(generatedNode -> coder.codeNodes(mod, resolved_nodes, generatedNode));

			resolved_nodes.forEach(generatedNode -> coder.codeNode(generatedNode, mod));

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
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
