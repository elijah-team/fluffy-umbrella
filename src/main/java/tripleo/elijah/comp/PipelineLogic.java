/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.comp.notation.GN_GenerateNodesIntoSink;
import tripleo.elijah.entrypoints.EntryPoint;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkManager;

/**
 * Created 12/30/20 2:14 AM
 */
public class PipelineLogic {
	public final  DeducePhase     dp;
	private final ElLog.Verbosity verbosity;
	public final  GeneratePhase   generatePhase;
	private final List<OS_Module> __mods_BACKING = new ArrayList<OS_Module>();
	final         EIT_ModuleList  mods           = new EIT_ModuleList(__mods_BACKING);
	public final  GenerateResult  gr             = new GenerateResult();
	public final  List<ElLog>     elLogs         = new LinkedList<ElLog>();

	public PipelineLogic(final @NotNull ICompilationAccess aCa) {
		verbosity     = aCa.testSilence();
		generatePhase = new GeneratePhase(verbosity, this);
		dp            = new DeducePhase(generatePhase, this, verbosity, aCa);

		aCa.setPipelineLogic(this);

		pa = aCa.getCompilation().pa();
	}

	final IPipelineAccess pa;

	public final Observer<OS_Module> om = new Observer<OS_Module>() {
		@Override
		public void onSubscribe(Disposable d) {
			throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
		}

		@Override
		public void onNext(OS_Module mod) {
			NotImplementedException.raise();

			//System.err.printf("7070 %s %d%n", mod.getFileName(), mod.entryPoints.size());

			run2(mod, mod.entryPoints);
		}

		@Override
		public void onError(Throwable e) {
			throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
		}

		@Override
		public void onComplete() {
			dp.finish();
		}
	};
	
	public void everythingBeforeGenerate(final @NotNull List<EvaNode> lgc) {
		assert lgc.size() == 0;

		mods.stream().forEach(mod ->
			om.onNext(mod));

		om.onComplete();
	}

	public void generate(List<EvaNode> lgc, final GenerateResultSink aResultSink) {
		pa.notate(117, new GN_GenerateNodesIntoSink(lgc, aResultSink, mods, verbosity, gr, this));
	}

	protected void run2(OS_Module mod, @NotNull List<EntryPoint> epl) {
		final GenerateFunctions gfm = getGenerateFunctions(mod);
		gfm.generateFromEntryPoints(epl, dp);

//		WorkManager wm = new WorkManager();
//		WorkList wl = new WorkList();

		DeducePhase.@NotNull GeneratedClasses lgc            = dp.generatedClasses;
		List<EvaNode>                         resolved_nodes = new ArrayList<EvaNode>();

		for (final EvaNode evaNode : lgc) {
			if (evaNode instanceof GNCoded) {
				final GNCoded coded = (GNCoded) evaNode;

				switch (coded.getRole()) {
				case FUNCTION: {
//					EvaFunction generatedFunction = (EvaFunction) generatedNode;
					if (coded.getCode() == 0)
						coded.setCode(mod.getCompilation().nextFunctionCode());
					break;
				}
				case CLASS: {
					final EvaClass evaClass = (EvaClass) evaNode;
//					if (generatedClass.getCode() == 0)
//						generatedClass.setCode(mod.getCompilation().nextClassCode());
					for (EvaClass evaClass2 : evaClass.classMap.values()) {
						if (evaClass2.getCode() == 0)
							evaClass2.setCode(mod.getCompilation().nextClassCode());
					}
					for (EvaFunction generatedFunction : evaClass.functionMap.values()) {
						for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
							if (identTableEntry.isResolved()) {
								EvaNode node = identTableEntry.resolvedType();
								resolved_nodes.add(node);
							}
						}
					}
					break;
				}
				case NAMESPACE:
				{
					final EvaNamespace generatedNamespace = (EvaNamespace) evaNode;
					if (coded.getCode() == 0)
						coded.setCode(mod.getCompilation().nextClassCode());
					for (EvaClass evaClass : generatedNamespace.classMap.values()) {
						if (evaClass.getCode() == 0)
							evaClass.setCode(mod.getCompilation().nextClassCode());
					}
					for (EvaFunction generatedFunction : generatedNamespace.functionMap.values()) {
						for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
							if (identTableEntry.isResolved()) {
								EvaNode node = identTableEntry.resolvedType();
								resolved_nodes.add(node);
							}
						}
					}
					break;
				}
				default:
					throw new IllegalStateException("Unexpected value: " + coded.getRole());
				}

			} else {
				throw new IllegalStateException("node must be coded");
			}
		}

		for (final EvaNode evaNode : resolved_nodes) {
/*
			if (generatedNode instanceof EvaFunction) {
				EvaFunction generatedFunction = (EvaFunction) generatedNode;
				if (generatedFunction.getCode() == 0)
					generatedFunction.setCode(mod.getCompilation().nextFunctionCode());
			} else if (generatedNode instanceof EvaClass) {
				final EvaClass generatedClass = (EvaClass) generatedNode;
				if (generatedClass.getCode() == 0)
					generatedClass.setCode(mod.getCompilation().nextClassCode());
			} else if (generatedNode instanceof EvaNamespace) {
				final EvaNamespace generatedNamespace = (EvaNamespace) generatedNode;
				if (generatedNamespace.getCode() == 0)
					generatedNamespace.setCode(mod.getCompilation().nextClassCode());
			}
*/
			if (evaNode instanceof GNCoded) {
				final GNCoded coded = (GNCoded) evaNode;
				final int code;
				if (coded.getCode() == 0) {
					switch (coded.getRole()) {
					case FUNCTION:
						code = (mod.getCompilation().nextFunctionCode());
						break;
					case NAMESPACE:
					case CLASS:
						code = mod.getCompilation().nextClassCode();
						break;
					default:
						throw new IllegalStateException("Invalid coded role");
					}
					coded.setCode(code);
				}
			} else
				throw new IllegalStateException("node is not coded");
		}

		dp.deduceModule(mod, lgc, verbosity);

		resolveCheck(lgc);

//		for (final GeneratedNode gn : lgf) {
//			if (gn instanceof EvaFunction) {
//				EvaFunction gf = (EvaFunction) gn;
//				System.out.println("----------------------------------------------------------");
//				System.out.println(gf.name());
//				System.out.println("----------------------------------------------------------");
//				EvaFunction.printTables(gf);
//				System.out.println("----------------------------------------------------------");
//			}
//		}

	}

	@NotNull
	private GenerateFunctions getGenerateFunctions(OS_Module mod) {
		return generatePhase.getGenerateFunctions(mod);
	}

	public void addModule(OS_Module m) {
		mods.add(m);
	}

	private void resolveCheck(DeducePhase.GeneratedClasses lgc) {
		for (final EvaNode evaNode : lgc) {
			if (evaNode instanceof EvaFunction) {

			} else if (evaNode instanceof EvaClass) {
//				final EvaClass generatedClass = (EvaClass) generatedNode;
//				for (EvaFunction generatedFunction : generatedClass.functionMap.values()) {
//					for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
//						final IdentIA ia2 = new IdentIA(identTableEntry.getIndex(), generatedFunction);
//						final String s = generatedFunction.getIdentIAPathNormal(ia2);
//						if (identTableEntry/*.isResolved()*/.getStatus() == BaseTableEntry.Status.KNOWN) {
////							GeneratedNode node = identTableEntry.resolved();
////							resolved_nodes.add(node);
//							tripleo.elijah.util.Stupidity.println_out_2("91 Resolved IDENT "+ s);
//						} else {
////							assert identTableEntry.getStatus() == BaseTableEntry.Status.UNKNOWN;
////							identTableEntry.setStatus(BaseTableEntry.Status.UNKNOWN, null);
//							tripleo.elijah.util.Stupidity.println_out_2("92 Unresolved IDENT "+ s);
//						}
//					}
//				}
			} else if (evaNode instanceof EvaNamespace) {
//				final EvaNamespace generatedNamespace = (EvaNamespace) generatedNode;
//				NamespaceStatement namespaceStatement = generatedNamespace.getNamespaceStatement();
//				for (EvaFunction generatedFunction : generatedNamespace.functionMap.values()) {
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

/*
	public ElLog.Verbosity getVerbosity() {
		return verbosity; // ? ElLog.Verbosity.VERBOSE : ElLog.Verbosity.SILENT;
	}
*/

	public void addLog(ElLog aLog) {
		elLogs.add(aLog);
	}

	public ElLog.Verbosity getVerbosity() {
		return this.verbosity;
	}
}

//
//
//
