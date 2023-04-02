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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.entrypoints.EntryPoint;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.OutputFileFactory;
import tripleo.elijah.stages.gen_generic.OutputFileFactoryParams;
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
	private final List<OS_Module> mods              = new ArrayList<OS_Module>();
	final GenerateResult  gr                = new GenerateResult();
	final List<ElLog>     elLogs            = new LinkedList<ElLog>();

/*
	public PipelineLogic(ElLog.Verbosity aVerbosity) {
		verbosity     = aVerbosity;
		generatePhase = new GeneratePhase(aVerbosity, this);
		dp            = new DeducePhase(generatePhase, this, verbosity, null);
	}
*/

	public PipelineLogic(final @NotNull ICompilationAccess aCa) {
		verbosity     = aCa.testSilence();
		generatePhase = new GeneratePhase(verbosity, this);
		dp            = new DeducePhase(generatePhase, this, verbosity, aCa);

		aCa.setPipelineLogic(this);
	}

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
			NotImplementedException.raise();

			dp.finish();

//			dp.generatedClasses.addAll(lgc);

//			elLogs = dp.deduceLogs;
		}
	};
	
	public void everythingBeforeGenerate(final @NotNull List<EvaNode> lgc) {
		assert lgc.size() == 0;
		
		for (final OS_Module mod : mods) {
			om.onNext(mod);
		}
		
		om.onComplete();
	}

	public void generate(List<EvaNode> lgc) {
		final WorkManager wm = new WorkManager();

		for (final OS_Module mod : mods) {
			// README use any errSink, they should all be the same
			final ErrSink              errSink = mod.getCompilation().getErrSink();

			final LibraryStatementPart lsp     = mod.getLsp();






			if (lsp == null) {
				System.err.println("7777777777777777777 mod.getFilename "+mod.getFileName());
				continue;
			}





			final CompilerInstructions ci      = lsp.getInstructions();
			final @Nullable String     lang2    = ci.genLang();












			final @Nullable String     lang    = lang2==null?"c":lang2;












			final OutputFileFactoryParams params        = new OutputFileFactoryParams(mod, errSink, verbosity, this);
			final GenerateFiles           generateFiles = OutputFileFactory.create("c"/*lang*/, params);
			//final GenerateC               generateC     = new GenerateC(mod, errSink, verbosity, this);
			final GenerateResult          ggr           = run3(mod, lgc, wm, generateFiles);
			wm.drain();
			gr.results().addAll(ggr.results());
		}
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

	protected GenerateResult run3(OS_Module mod, @NotNull List<EvaNode> lgc, WorkManager wm, GenerateFiles ggc) {
		GenerateResult gr = new GenerateResult();

		for (EvaNode evaNode : lgc) {
			if (evaNode.module() != mod) continue; // README curious

			if (evaNode instanceof EvaContainerNC) {
				final EvaContainerNC nc = (EvaContainerNC) evaNode;

				nc.generateCode(ggc, gr);
				if (nc instanceof EvaClass) {
					final EvaClass evaClass = (EvaClass) nc;

					final @NotNull Collection<EvaNode> gn2 = GenerateFiles.constructors_to_list_of_generated_nodes(evaClass.constructors.values());
					GenerateResult                     gr3 = ggc.generateCode(gn2, wm);
					gr.additional(gr3);
				}

				final @NotNull Collection<EvaNode> gn1 = GenerateFiles.functions_to_list_of_generated_nodes(nc.functionMap.values());
				GenerateResult                     gr2 = ggc.generateCode(gn1, wm);
				gr.additional(gr2);

				final @NotNull Collection<EvaNode> gn2 = GenerateFiles.classes_to_list_of_generated_nodes(nc.classMap.values());
				GenerateResult                     gr3 = ggc.generateCode(gn2, wm);
				gr.additional(gr3);
			} else {
				System.out.println("2009 " + evaNode.getClass().getName());
			}
		}

		return gr;
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
//							System.out.println("91 Resolved IDENT "+ s);
//						} else {
////							assert identTableEntry.getStatus() == BaseTableEntry.Status.UNKNOWN;
////							identTableEntry.setStatus(BaseTableEntry.Status.UNKNOWN, null);
//							System.out.println("92 Unresolved IDENT "+ s);
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
