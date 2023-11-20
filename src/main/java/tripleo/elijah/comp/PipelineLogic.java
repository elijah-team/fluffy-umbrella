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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/30/20 2:14 AM
 */
public class PipelineLogic implements AccessBus.AB_ModuleListListener {
	public final GeneratePhase generatePhase;
	public final DeducePhase   dp;
	final        AccessBus     __ab;

	private final ElLog.Verbosity verbosity;

	private final List<OS_Module> __mods_BACKING = new ArrayList<OS_Module>();
	final         EIT_ModuleList  mods           = new EIT_ModuleList(__mods_BACKING);

	public PipelineLogic(final AccessBus iab) {
		__ab = iab; // we're watching you

		final boolean sil = __ab.getCompilation().getSilence(); // ca.testSilence

		verbosity     = sil ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;
		generatePhase = new GeneratePhase(verbosity, this, __ab.getCompilation());
		dp            = new DeducePhase(generatePhase, this, verbosity, __ab.getCompilation());

		// FIXME examine if this is necessary and possibly or actually elsewhere
		//  and/or just another section
		subscribeMods(this);
	}

	public void subscribeMods(final AccessBus.AB_ModuleListListener l) {
		__ab.subscribe_moduleList(l);
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

	@NotNull GenerateFunctions getGenerateFunctions(final OS_Module mod) {
		return generatePhase.getGenerateFunctions(mod);
	}

	public void addModule(final OS_Module m) {
		mods.add(m);
	}

	public static void resolveCheck(final DeducePhase.@NotNull GeneratedClasses lgc) {
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
//							tripleo.elijah.util.Stupidity.println2("91 Resolved IDENT "+ s);
//						} else {
////							assert identTableEntry.getStatus() == BaseTableEntry.Status.UNKNOWN;
////							identTableEntry.setStatus(BaseTableEntry.Status.UNKNOWN, null);
//							tripleo.elijah.util.Stupidity.println2("92 Unresolved IDENT "+ s);
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
		__ab.getCompilation().elLogs.add(aLog);
	}

	@Override
	public void mods_slot(final @NotNull EIT_ModuleList aModuleList) {
		//
//		__ab.subscribePipelineLogic((x) -> aModuleList._set_PL(x));

		//
		aModuleList.process__PL(this::getGenerateFunctions, this);

		dp.finish(dp.generatedClasses);
//		dp.generatedClasses.addAll(lgc);
	}

	public GenerateResult getGR() {
		return __ab.gr;
	}

	public List<GeneratedNode> generatedClassesCopy() {
		return dp.generatedClasses.copy();
	}
}

//
//
//
