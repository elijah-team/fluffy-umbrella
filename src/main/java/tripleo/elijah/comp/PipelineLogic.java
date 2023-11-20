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
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.work.WorkManager;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created 12/30/20 2:14 AM
 */
public class PipelineLogic implements AccessBus.AB_ModuleListListener {
	public final  GeneratePhase generatePhase;
	public final  DeducePhase   dp;
	private final AccessBus     __ab;

	public GenerateResult gr     = new GenerateResult();
	public List<ElLog>    elLogs = new LinkedList<ElLog>();

	private final ElLog.Verbosity verbosity;

	private final List<OS_Module> __mods_BACKING = new ArrayList<OS_Module>();
	final         EIT_ModuleList  mods           = new EIT_ModuleList(__mods_BACKING);

	public PipelineLogic(final AccessBus ab) {
		__ab = ab;

		final boolean         sil     = ab.getCompilation().getSilence();
		final ElLog.Verbosity silence = sil ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;

		verbosity = silence;
		generatePhase = new GeneratePhase(verbosity, this);
		dp = new DeducePhase(generatePhase, this, verbosity);


		subscribeMods(this);
	}

	public void everythingBeforeGenerate(final List<GeneratedNode> lgc) {
		resolveMods();

		final List<PL_Run2> run2_work = mods.stream()
				.map(mod -> new PL_Run2(mod, mod.entryPoints._getMods(), this::getGenerateFunctions, dp, this))
				.collect(Collectors.toList());

		final List<DeducePhase.GeneratedClasses> lgc2 = run2_work.stream()
				.map(PL_Run2::run2)
				.collect(Collectors.toList());

//		List<List<EntryPoint>> entryPoints = mods.stream().map(mod -> mod.entryPoints).collect(Collectors.toList());

		lgc2.forEach(dp::finish);

		dp.generatedClasses.addAll(lgc);

//		elLogs = dp.deduceLogs;
	}

	public void generate(final List<GeneratedNode> lgc) {
		final WorkManager wm = new WorkManager();
		// README use any errSink, they should all be the same
		for (final OS_Module mod : mods.getMods()) {
			final GenerateC generateC = new GenerateC(mod, mod.parent.getErrSink(), verbosity, this);
			final GenerateResult ggr = run3(mod, lgc, wm, generateC);
			wm.drain();
			gr.results().addAll(ggr.results());
		}

		__ab.resolveGenerateResult(gr);
	}

	public void subscribeMods(final AccessBus.AB_ModuleListListener l) {
		__ab.subscribe_moduleList(l);
	}

	public void resolveMods() {
		__ab.resolveModuleList(mods);
	}

/*
	public void generate__new(List<GeneratedNode> lgc) {
		final WorkManager wm = new WorkManager();
		// README use any errSink, they should all be the same
		for (OS_Module mod : mods.getMods()) {
			__ab.doModule(lgc, wm, mod, this);
		}

		__ab.resolveGenerateResult(gr);
	}
*/

	protected GenerateResult run3(final @NotNull OS_Module mod,
	                              final @NotNull List<GeneratedNode> lgc,
	                              final @NotNull WorkManager wm,
	                              final @NotNull GenerateC ggc) {
		final List<GeneratedNode> nodes = lgc.stream()
				.filter(aGeneratedNode -> aGeneratedNode.module() == mod)
				.collect(Collectors.toList());

		final GenerateResult gr2 = ggc.resultsFromNodes(nodes, wm);

		return gr2;
	}

	public static void debug_buffers(@NotNull final GenerateResult gr, final PrintStream stream) {
		for (final GenerateResultItem ab : gr.results()) {
			stream.println("---------------------------------------------------------------");
			stream.println(ab.counter);
			stream.println(ab.ty);
			stream.println(ab.output);
			stream.println(ab.node.identityString());
			stream.println(ab.buffer.getText());
			stream.println("---------------------------------------------------------------");
		}
	}

	@NotNull
	private GenerateFunctions getGenerateFunctions(final OS_Module mod) {
		return generatePhase.getGenerateFunctions(mod);
	}

	public void addModule(final OS_Module m) {
		mods.add(m);
	}

	public void resolveCheck(final DeducePhase.@NotNull GeneratedClasses lgc) {
		for (final GeneratedNode generatedNode : lgc) {
			if (generatedNode instanceof GeneratedFunction) {

			} else if (generatedNode instanceof GeneratedClass) {
//				final GeneratedClass generatedClass = (GeneratedClass) generatedNode;
//				for (GeneratedFunction generatedFunction : generatedClass.functionMap.values()) {
//					for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
//						final IdentIA ia2 = new IdentIA(identTableEntry.getIndex(), generatedFunction);
//						final String s = generatedFunction.getIdentIAPathNormal(ia2);
//						if (identTableEntry/*.isResolved()*/.getStatus() == BaseTableEntry.Status.KNOWN) {
////							GeneratedNode node = identTableEntry.resolved();
////							resolved_nodes.add(node);
//							System.out.println("91 Resolved IDENT "+ s);
//						} else {
////							assert identTableEntry.getStatus() == BaseTableEntry.Status.UNKNOWN;
////							identTableEntry.setStatus(BaseTableEntry.Status.UNKNOWN, null);
//							System.out.println("92 Unresolved IDENT "+ s);
//						}
//					}
//				}
			} else if (generatedNode instanceof GeneratedNamespace) {
//				final GeneratedNamespace generatedNamespace = (GeneratedNamespace) generatedNode;
//				NamespaceStatement namespaceStatement = generatedNamespace.getNamespaceStatement();
//				for (GeneratedFunction generatedFunction : generatedNamespace.functionMap.values()) {
//					for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
//						if (identTableEntry.isResolved()) {
//							GeneratedNode node = identTableEntry.resolved();
//							resolved_nodes.add(node);
//						}
//					}
//				}
			}
		}
	}

	public ElLog.Verbosity getVerbosity() {
		return verbosity;
	}

	public void addLog(final ElLog aLog) {
		elLogs.add(aLog);
	}

	@Override
	public void mods_slot(final @NotNull EIT_ModuleList aModuleList) {
		//
		__ab.subscribePipelineLogic((x) -> aModuleList._set_PL(x));

		//
		aModuleList.process__PL(this::getGenerateFunctions, this);

		dp.finish(dp.generatedClasses);
//		dp.generatedClasses.addAll(lgc);
	}

	static class PL_Run2 {
		private final OS_Module mod;
		private final List<EntryPoint> entryPoints;
		private final DeducePhase dp;
		private final Function<OS_Module, GenerateFunctions> mapper;
		private final PipelineLogic pipelineLogic;

		public PL_Run2(final OS_Module mod,
					   final List<EntryPoint> entryPoints,
					   final Function<OS_Module, GenerateFunctions> mapper,
					   final DeducePhase dp,
					   final PipelineLogic pipelineLogic) {
			this.mod = mod;
			this.entryPoints = entryPoints;
			this.dp = dp;
			this.mapper = mapper;
			this.pipelineLogic = pipelineLogic;
		}

		protected DeducePhase.@NotNull GeneratedClasses run2() {
			final GenerateFunctions gfm = mapper.apply(mod);
			final DeducePhase deducePhase = pipelineLogic.dp;

			gfm.generateFromEntryPoints(entryPoints, deducePhase);

			final DeducePhase.@NotNull GeneratedClasses lgc = dp.generatedClasses;
			@NotNull final List<GeneratedNode> resolved_nodes = new ArrayList<GeneratedNode>();

			final Coder coder = new Coder();

			lgc.copy().stream().forEach(generatedNode -> coder.codeNodes(mod, resolved_nodes, generatedNode));

			resolved_nodes.forEach(generatedNode -> coder.codeNode(generatedNode, mod));

			dp.deduceModule(mod, lgc, true, pipelineLogic.getVerbosity());

			pipelineLogic.resolveCheck(lgc);

//		for (final GeneratedNode gn : lgf) {
//			if (gn instanceof GeneratedFunction) {
//				GeneratedFunction gf = (GeneratedFunction) gn;
//				System.out.println("----------------------------------------------------------");
//				System.out.println(gf.name());
//				System.out.println("----------------------------------------------------------");
//				GeneratedFunction.printTables(gf);
//				System.out.println("----------------------------------------------------------");
//			}
//		}

			return lgc;
		}
	}

}

//
//
//
