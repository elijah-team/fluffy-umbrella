package tripleo.elijah.comp.notation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;

import java.util.ArrayList;
import java.util.List;

public class GN_PL_Run2 implements GN_Notable {
	private final          PipelineLogic pipelineLogic;
	private final @NotNull OS_Module     mod;

	@Contract(pure = true)
	public GN_PL_Run2(final PipelineLogic aPipelineLogic, final @NotNull OS_Module aMod) {
		pipelineLogic = aPipelineLogic;
		mod           = aMod;
	}

	@Override
	public void run() {
		final GenerateFunctions gfm = pipelineLogic.getGenerateFunctions(mod);
		gfm.generateFromEntryPoints(mod.entryPoints, pipelineLogic.dp);

//		WorkManager wm = new WorkManager();
//		WorkList wl = new WorkList();

		DeducePhase.@NotNull GeneratedClasses lgc            = pipelineLogic.dp.generatedClasses;
		List<EvaNode>                         resolved_nodes = new ArrayList<EvaNode>();

		assert lgc.copy().size() >0;

		final ICodeRegistrar cr = pipelineLogic.generatePhase.codeRegistrar;

		for (final EvaNode evaNode : lgc) {
			if (evaNode instanceof GNCoded) {
				final GNCoded coded = (GNCoded) evaNode;


				switch (coded.getRole()) {
				case FUNCTION: {
//					EvaFunction generatedFunction = (EvaFunction) generatedNode;
					if (coded.getCode() == 0)
						coded.setCode(mod.getCompilation().nextFunctionCode());
					cr.registerFunction((BaseEvaFunction) evaNode);
					break;
				}
				case CLASS: {
					final EvaClass evaClass = (EvaClass) evaNode;

					assert  (evaClass.getCode() != 0);

//					if (generatedClass.getCode() == 0)
//						generatedClass.setCode(mod.getCompilation().nextClassCode());
					for (EvaClass evaClass2 : evaClass.classMap.values()) {
						if (evaClass2.getCode() == 0) {
							//evaClass2.setCode(mod.getCompilation().nextClassCode());
							cr.registerClass(evaClass2);
						}
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
				case NAMESPACE: {
					final EvaNamespace evaNamespace = (EvaNamespace) evaNode;
					if (coded.getCode() == 0) {
						//coded.setCode(mod.getCompilation().nextClassCode());
						cr.registerNamespace(evaNamespace);
					}
					for (EvaClass evaClass : evaNamespace.classMap.values()) {
						if (evaClass.getCode() == 0)
							evaClass.setCode(mod.getCompilation().nextClassCode());
					}
					for (EvaFunction generatedFunction : evaNamespace.functionMap.values()) {
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
			if (evaNode instanceof GNCoded) {
				final GNCoded coded = (GNCoded) evaNode;
				if (coded.getCode() == 0) {
					switch (coded.getRole()) {
					case FUNCTION:
						cr.registerFunction((BaseEvaFunction) coded);
						break;
					case NAMESPACE:
						cr.registerNamespace((EvaNamespace) coded);
						break;
					case CLASS:
						cr.registerClass((EvaClass) coded);
						break;
					default:  throw new IllegalStateException("Invalid coded role");
					}
				}
			} else
				throw new IllegalStateException("node is not coded");
		}

		pipelineLogic.dp.deduceModule(mod, lgc, pipelineLogic.getVerbosity());

		pipelineLogic.resolveCheck(lgc);

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
}
