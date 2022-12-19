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
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Stupidity;
import tripleo.elijah.work.WorkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class EIT_ModuleList {
	private final List<OS_Module> mods;
	private       PipelineLogic   __pl;

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

			final @NotNull EntryPointList epl         = mod.entryPoints;
			final DeducePhase             deducePhase = pipelineLogic.dp;
			//final DeducePhase.@NotNull GeneratedClasses lgc            = deducePhase.generatedClasses;

			final _ProcessParams plp = new _ProcessParams(mod, pipelineLogic, gfm, epl, deducePhase);

			__process__PL__each(plp);
		}
	}

	private void __process__PL__each(final @NotNull _ProcessParams plp) {

		assert plp.pipelineLogic == __pl; // TODO see if this holds

		//plp.pipelineLogic = __pl;

		final List<GeneratedNode> resolved_nodes = new ArrayList<GeneratedNode>();

//		assert lgc.size() == 0;

		int size = plp.getLgc().size();
		if (size != 0) {
			int y = 2;
			Stupidity.println_err(String.format("lgc.size() != 0: %d", size));
		}

		plp.generate();

		//assert lgc.size() == epl.size(); //hmm

		if (false) {
			________NONO_processByEntryPoint(plp.getLgc(), resolved_nodes, plp.getMod());
		} else {
			final Coder coder = new Coder();

			for (final GeneratedNode generatedNode : plp.getLgc()) {
				coder.codeNodes(plp.getMod(), resolved_nodes, generatedNode);
			}

			resolved_nodes.forEach(generatedNode -> coder.codeNode(generatedNode, plp.getMod()));
		}

		plp.deduceModule();

		plp.lgcThing();

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

	private void ________NONO_processByEntryPoint(DeducePhase.@NotNull GeneratedClasses lgc, List<GeneratedNode> resolved_nodes, OS_Module mod) {

		assert false;

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

	public void _set_PL(PipelineLogic aPipelineLogic) {
		__pl = aPipelineLogic;
	}

	public void add(OS_Module aM) {
		mods.add(aM);
	}

	private static class _ProcessParams {
		private final OS_Module         mod;
		private       PipelineLogic     pipelineLogic;
		private final GenerateFunctions gfm;
		@NotNull
		private final EntryPointList    epl;
		private final DeducePhase       deducePhase;
//		@NotNull
//		private final ElLog.Verbosity                         verbosity;

		private _ProcessParams(@NotNull OS_Module aModule,
							   @NotNull PipelineLogic aPipelineLogic,
							   @NotNull GenerateFunctions aGenerateFunctions,
							   @NotNull EntryPointList aEntryPointList,
							   @NotNull DeducePhase aDeducePhase) {
			mod = aModule;
			pipelineLogic = aPipelineLogic;
			gfm = aGenerateFunctions;
			epl = aEntryPointList;
			deducePhase = aDeducePhase;
//			verbosity = mod.getCompilation().pipelineLogic.getVerbosity();
		}

		@Contract(pure = true)
		public OS_Module getMod() {
			return mod;
		}

//		public GenerateFunctions getGfm() {
//			return gfm;
//		}
//
//		public EntryPointList getEpl() {
//			return epl;
//		}
//
//		public DeducePhase getDeducePhase() {
//			return deducePhase;
//		}

		@Contract(pure = true)
		public DeducePhase.GeneratedClasses getLgc() {
			return deducePhase.generatedClasses;
		}

		@Contract(pure = true)
		public @NotNull Supplier<WorkManager> getWorkManagerSupplier() {
			return () -> pipelineLogic.generatePhase.wm;
		}

		@Contract(pure = true)
		public ElLog.@NotNull Verbosity getVerbosity() {
//			return verbosity;
			return /*mod.getCompilation().*/pipelineLogic.getVerbosity();
		}

		//
		//
		//

		public void generate() {
			epl.generate(gfm, deducePhase, getWorkManagerSupplier());
		}

		public void deduceModule() {
			deducePhase.deduceModule(mod, getLgc(), getVerbosity());
		}

		public void lgcThing() {
			pipelineLogic.resolveCheck(getLgc());
		}
	}
}
