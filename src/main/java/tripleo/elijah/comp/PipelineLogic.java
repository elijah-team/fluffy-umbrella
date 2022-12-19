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
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_fn.GeneratePhase;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.work.WorkManager;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/30/20 2:14 AM
 */
public class PipelineLogic implements AccessBus.AB_ModuleListListener {
	public final  GeneratePhase generatePhase;
	public final  DeducePhase   dp;
	private final AccessBus     __ab;

	public GenerateResult gr     = new GenerateResult();

	private final ElLog.Verbosity verbosity;

	private final List<OS_Module> __mods_BACKING = new ArrayList<OS_Module>();
	final         EIT_ModuleList  mods           = new EIT_ModuleList(__mods_BACKING);

	public PipelineLogic(final AccessBus iab) {
		__ab = iab; // we're watching you

		boolean         sil     = __ab.getCompilation().getSilence();
		ElLog.Verbosity silence = sil ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;

		verbosity = silence;
		generatePhase = new GeneratePhase(verbosity, this);
		dp = new DeducePhase(generatePhase, this, verbosity);

		// FIXME examine if this is necessary and possibly or actually elsewhere
		//  and/or just another section
		subscribeMods(this);
	}

	public void subscribeMods(AccessBus.AB_ModuleListListener l) {
		__ab.subscribe_moduleList(l);
	}

	public void resolveMods() {
		__ab.resolveModuleList(mods);
	}

	public void everythingBeforeGenerate() {
//		resolveMods();
	}

	public void generate(List<GeneratedNode> lgc, ErrSink aErrSink) {
		final WorkManager wm = new WorkManager();

//		__mods_BACKING.addAll((__ab.getCompilation().getModules()));

		for (OS_Module mod : mods.getMods()) {
			__ab.doModule(lgc, wm, mod, this, aErrSink);
		}

		__ab.resolveGenerateResult(gr);
	}

	public static void debug_buffers(@NotNull GenerateResult gr, PrintStream stream) {
		for (GenerateResultItem ab : gr.results()) {
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
	private GenerateFunctions getGenerateFunctions(OS_Module mod) {
		return generatePhase.getGenerateFunctions(mod);
	}

	public void addModule(OS_Module m) {
		mods.add(m);
	}

	public void resolveCheck(DeducePhase.@NotNull GeneratedClasses lgc) {
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

	public void addLog(ElLog aLog) {
		__ab.getCompilation().elLogs.add(aLog);
	}

	@Override
	public void mods_slot(final @NotNull EIT_ModuleList aModuleList) {
		//
		__ab.subscribePipelineLogic((x) -> aModuleList._set_PL(x));

		//
		aModuleList.process__PL(this::getGenerateFunctions, this);

		dp.finish();
//		dp.generatedClasses.addAll(lgc);
	}

}

//
//
//
