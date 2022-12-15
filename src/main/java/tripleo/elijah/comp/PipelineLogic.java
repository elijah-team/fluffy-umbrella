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
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.work.WorkManager;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created 12/30/20 2:14 AM
 */
public class PipelineLogic {
	public final GeneratePhase generatePhase;
	public final DeducePhase dp;

	public GenerateResult gr = new GenerateResult();
	public List<ElLog> elLogs = new LinkedList<ElLog>();
	public boolean verbose = true;

	private final ElLog.Verbosity verbosity;

	final List<OS_Module> mods = new ArrayList<OS_Module>();

	public boolean postDeduceEnabled = false;

	public PipelineLogic(ElLog.Verbosity aVerbosity) {
		verbosity = aVerbosity;
		generatePhase = new GeneratePhase(aVerbosity, this);
		dp = new DeducePhase(generatePhase, this, verbosity);
	}

	public void everythingBeforeGenerate(List<GeneratedNode> lgc) {
		for (OS_Module mod : mods) {
			run2(mod, mod.entryPoints);
		}
//		List<List<EntryPoint>> entryPoints = mods.stream().map(mod -> mod.entryPoints).collect(Collectors.toList());
		dp.finish();

		dp.generatedClasses.addAll(lgc);

//		elLogs = dp.deduceLogs;
	}

	public void generate(List<GeneratedNode> lgc) {
		final WorkManager wm = new WorkManager();
		// README use any errSink, they should all be the same
		for (OS_Module mod : mods) {
			final GenerateC generateC = new GenerateC(mod, mod.parent.getErrSink(), verbosity, this);
			final GenerateResult ggr = run3(mod, lgc, wm, generateC);
			wm.drain();
			gr.results().addAll(ggr.results());
		}
	}

	public static void debug_buffers(GenerateResult gr, PrintStream stream) {
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

	protected void run2(OS_Module mod, @NotNull List<EntryPoint> epl) {
		final GenerateFunctions gfm = getGenerateFunctions(mod);
		gfm.generateFromEntryPoints(epl, dp);

//		WorkManager wm = new WorkManager();
//		WorkList wl = new WorkList();

		DeducePhase.@NotNull GeneratedClasses lgc = dp.generatedClasses;
		List<GeneratedNode> resolved_nodes = new ArrayList<GeneratedNode>();

		for (final GeneratedNode generatedNode : lgc) {
			if (generatedNode instanceof GeneratedFunction) {
				GeneratedFunction generatedFunction = (GeneratedFunction) generatedNode;
				if (generatedFunction.getCode() == 0)
					generatedFunction.setCode(mod.parent.nextFunctionCode());
			} else if (generatedNode instanceof GeneratedClass) {
				final GeneratedClass generatedClass = (GeneratedClass) generatedNode;
//				if (generatedClass.getCode() == 0)
//					generatedClass.setCode(mod.parent.nextClassCode());
				for (GeneratedClass generatedClass2 : generatedClass.classMap.values()) {
					generatedClass2.setCode(mod.parent.nextClassCode());
				}
				for (GeneratedFunction generatedFunction : generatedClass.functionMap.values()) {
					for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
						if (identTableEntry.isResolved()) {
							GeneratedNode node = identTableEntry.resolvedType();
							resolved_nodes.add(node);
						}
					}
				}
			} else if (generatedNode instanceof GeneratedNamespace) {
				final GeneratedNamespace generatedNamespace = (GeneratedNamespace) generatedNode;
				if (generatedNamespace.getCode() == 0)
					generatedNamespace.setCode(mod.parent.nextClassCode());
				for (GeneratedClass generatedClass : generatedNamespace.classMap.values()) {
					generatedClass.setCode(mod.parent.nextClassCode());
				}
				for (GeneratedFunction generatedFunction : generatedNamespace.functionMap.values()) {
					for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
						if (identTableEntry.isResolved()) {
							GeneratedNode node = identTableEntry.resolvedType();
							resolved_nodes.add(node);
						}
					}
				}
			}
		}

		for (final GeneratedNode generatedNode : resolved_nodes) {
			if (generatedNode instanceof GeneratedFunction) {
				GeneratedFunction generatedFunction = (GeneratedFunction) generatedNode;
				if (generatedFunction.getCode() == 0)
					generatedFunction.setCode(mod.parent.nextFunctionCode());
			} else if (generatedNode instanceof GeneratedClass) {
				final GeneratedClass generatedClass = (GeneratedClass) generatedNode;
				if (generatedClass.getCode() == 0)
					generatedClass.setCode(mod.parent.nextClassCode());
			} else if (generatedNode instanceof GeneratedNamespace) {
				final GeneratedNamespace generatedNamespace = (GeneratedNamespace) generatedNode;
				if (generatedNamespace.getCode() == 0)
					generatedNamespace.setCode(mod.parent.nextClassCode());
			}
		}

		dp.deduceModule(mod, lgc, true, getVerbosity());

		resolveCheck(lgc);

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

	}

	@NotNull
	private GenerateFunctions getGenerateFunctions(OS_Module mod) {
		return generatePhase.getGenerateFunctions(mod);
	}

	protected GenerateResult run3(OS_Module mod, List<GeneratedNode> lgc, WorkManager wm, GenerateC ggc) {
		GenerateResult gr = new GenerateResult();

		for (GeneratedNode generatedNode : lgc) {
			if (generatedNode.module() != mod) continue; // README curious

			if (generatedNode instanceof GeneratedContainerNC) {
				final GeneratedContainerNC nc = (GeneratedContainerNC) generatedNode;

				nc.generateCode(ggc, gr);
				final @NotNull Collection<GeneratedNode> gn1 = ggc.functions_to_list_of_generated_nodes(nc.functionMap.values());
				GenerateResult gr2 = ggc.generateCode(gn1, wm);
				gr.results().addAll(gr2.results());
				final @NotNull Collection<GeneratedNode> gn2 = ggc.classes_to_list_of_generated_nodes(nc.classMap.values());
				GenerateResult gr3 = ggc.generateCode(gn2, wm);
				gr.results().addAll(gr3.results());
			} else {
				System.out.println("2009 " + generatedNode.getClass().getName());
			}
		}

		return gr;
	}

	public void addModule(OS_Module m) {
		mods.add(m);
	}

	private void resolveCheck(DeducePhase.GeneratedClasses lgc) {
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
		return verbose ? ElLog.Verbosity.VERBOSE : ElLog.Verbosity.SILENT;
	}

	public void addLog(ElLog aLog) {
		elLogs.add(aLog);
	}

}

//
//
//
