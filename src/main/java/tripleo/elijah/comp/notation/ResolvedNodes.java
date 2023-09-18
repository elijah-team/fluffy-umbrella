package tripleo.elijah.comp.notation;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.GNCoded;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.world.i.WorldModule;

import java.util.ArrayList;
import java.util.List;

// FIXME: curiously unused
public class ResolvedNodes {
	final         List<EvaNode>  resolved_nodes = new ArrayList<EvaNode>();
	private final ICodeRegistrar cr;

	public ResolvedNodes(final ICodeRegistrar aCr) {
		cr = aCr;
	}

	public void init(final DeducePhase.@NotNull GeneratedClasses c) {
		System.err.println("2222 " + c);

		for (final EvaNode evaNode : c) {
			if (!(evaNode instanceof final @NotNull GNCoded coded)) {
				throw new IllegalStateException("node must be coded");
			}

			switch (coded.getRole()) {
			case FUNCTION -> {
				cr.registerFunction((BaseEvaFunction) evaNode);
			}
			case CLASS -> {
				final EvaClass evaClass = (EvaClass) evaNode;

				//assert (evaClass.getCode() != 0);
				if (evaClass.getCode() == 0) {
					cr.registerClass(evaClass);
				}

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
			}
			case NAMESPACE -> {
				final EvaNamespace evaNamespace = (EvaNamespace) evaNode;
				if (coded.getCode() == 0) {
					//coded.setCode(mod.getCompilation().nextClassCode());
					cr.registerNamespace(evaNamespace);
				}
				for (EvaClass evaClass3 : evaNamespace.classMap.values()) {
					if (evaClass3.getCode() == 0) {
						//evaClass.setCode(mod.getCompilation().nextClassCode());
						cr.registerClass(evaClass3);
					}
				}
				for (EvaFunction generatedFunction : evaNamespace.functionMap.values()) {
					for (IdentTableEntry identTableEntry : generatedFunction.idte_list) {
						if (identTableEntry.isResolved()) {
							EvaNode node = identTableEntry.resolvedType();
							resolved_nodes.add(node);
						}
					}
				}
			}
			default -> throw new IllegalStateException("Unexpected value: " + coded.getRole());
			}
		}
	}

	public void part2() {
		__processResolvedNodes(resolved_nodes, cr);
	}

	private void __processResolvedNodes(final @NotNull List<EvaNode> resolved_nodes, final ICodeRegistrar cr) {
		resolved_nodes.stream()
				.filter(evaNode -> evaNode instanceof GNCoded)
				.map(evaNode -> (GNCoded) evaNode)
				.filter(coded -> coded.getCode() == 0)
				.forEach(coded -> {
					System.err.println("-*-*- __processResolvedNodes [NOT CODED] " + coded);
					coded.register(cr);
				});
	}

	public void part3(final @NotNull PipelineLogic pipelineLogic, final WorldModule mod, final DeducePhase.GeneratedClasses lgc) {
		pipelineLogic.dp.deduceModule(mod, lgc, pipelineLogic.getVerbosity());
	}
}
