package tripleo.elijah.nextgen.inputtree;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Coder;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.entrypoints.EntryPointList;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.GNCoded;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.util.Stupidity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EIT_ModuleList {
	private final List<OS_Module> mods;

	@Contract(pure = true)
	public EIT_ModuleList(List<OS_Module> aMods) {
		mods = aMods;
	}

	public List<OS_Module> getMods() {
		return mods;
	}

	public void process__PL(final Function<OS_Module, GenerateFunctions> ggf, PipelineLogic pipelineLogic) {
		for (final OS_Module mod : mods) {
			final GenerateFunctions gfm = ggf.apply(mod);

			final @NotNull EntryPointList               epl            = mod.entryPoints;
			final DeducePhase                           deducePhase    = pipelineLogic.dp;
			final DeducePhase.@NotNull GeneratedClasses lgc            = deducePhase.generatedClasses;
			final List<GeneratedNode>                   resolved_nodes = new ArrayList<GeneratedNode>();

//			assert lgc.size() == 0;

			if (lgc.size() != 0) {
				int y = 2;
				Stupidity.println_err(String.format("lgc.size() != 0: %d", lgc.size()));
			}

			epl.generate(gfm, deducePhase, () -> pipelineLogic.generatePhase.wm);

			//assert lgc.size() == epl.size(); //hmm

			if (false) {
				________NONO_processByEntryPoint(lgc, resolved_nodes, mod);
			} else {
				final Coder coder = new Coder();

				for (final GeneratedNode generatedNode : lgc) {
					coder.codeNodes(mod, resolved_nodes, generatedNode);
				}

				resolved_nodes.forEach(generatedNode -> coder.codeNode(generatedNode, mod));
			}

			deducePhase.deduceModule(mod, lgc, pipelineLogic.getVerbosity());

			pipelineLogic.resolveCheck(lgc);

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
	}

	private void ________NONO_processByEntryPoint(DeducePhase.@NotNull GeneratedClasses lgc, List<GeneratedNode> resolved_nodes, OS_Module mod) {
		for (final GeneratedNode generatedNode : lgc) {
			if (generatedNode instanceof GNCoded) {
				final GNCoded coded = (GNCoded) generatedNode;

				switch (coded.getRole()) {
					case FUNCTION: {
						//						GeneratedFunction generatedFunction = (GeneratedFunction) generatedNode;
						if (coded.getCode() == 0) {
							coded.setCode(mod.parent.nextFunctionCode());
						}
						break;
					}
					case CLASS: {
						final GeneratedClass generatedClass = (GeneratedClass) generatedNode;
						//						if (generatedClass.getCode() == 0)
						//							generatedClass.setCode(mod.parent.nextClassCode());
						for (GeneratedClass generatedClass2 : generatedClass.classMap.values()) {
							if (generatedClass2.getCode() == 0) {
								generatedClass2.setCode(mod.parent.nextClassCode());
							}
						}
						for (GeneratedFunction generatedFunction : generatedClass.functionMap.values()) {
							for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
								if (identTableEntry.isResolved()) {
									GeneratedNode node = identTableEntry.resolvedType();
									resolved_nodes.add(node);
								}
							}
						}
						break;
					}
					case NAMESPACE: {
						final GeneratedNamespace generatedNamespace = (GeneratedNamespace) generatedNode;
						if (coded.getCode() == 0) {
							coded.setCode(mod.parent.nextClassCode());
						}
						for (GeneratedClass generatedClass : generatedNamespace.classMap.values()) {
							if (generatedClass.getCode() == 0) {
								generatedClass.setCode(mod.parent.nextClassCode());
							}
						}
						for (GeneratedFunction generatedFunction : generatedNamespace.functionMap.values()) {
							for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
								if (identTableEntry.isResolved()) {
									GeneratedNode node = identTableEntry.resolvedType();
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


		for (final GeneratedNode generatedNode : resolved_nodes) {
			if (generatedNode instanceof GNCoded) {
				final GNCoded coded = (GNCoded) generatedNode;
				final int     code;
				if (coded.getCode() == 0) {
					switch (coded.getRole()) {
						case FUNCTION:
							code = mod.parent.nextFunctionCode();
							break;
						case NAMESPACE:
						case CLASS:
							code = mod.parent.nextClassCode();
							break;
						default:
							throw new IllegalStateException("Invalid coded role");
					}
					coded.setCode(code);
				}
			} else {
				throw new IllegalStateException("node is not coded");
			}
		}
	}

}
