package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.entrypoints.EntryPointProcessor;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface FlowK {
	public class DeduceTypes2__deduce_generated_function_base implements FlowK {
		private final BaseEvaFunction generatedFunction;
		private final BaseFunctionDef fd;

		public DeduceTypes2__deduce_generated_function_base(final @NotNull BaseEvaFunction aGeneratedFunction, final @NotNull BaseFunctionDef aFd) {
			generatedFunction = aGeneratedFunction;
			fd                = aFd;
		}

		@Override
		public String report() {
			return new StringBuilder()
			  .append("@@ DeduceTypes2::deduce_generated_function_base: ")
			  .append(fd.getNameNode().getText())
			  .append(" ")
			  .append(fd.getModule().getFileName())
			  .toString();
		}
	}

	public class DeduceTypes2__deduceFunctions__post implements FlowK {
		private final DeduceTypes2  deduceTypes2;
		private final List<EvaNode> generatedClasses;

		public DeduceTypes2__deduceFunctions__post(final DeduceTypes2 aDeduceTypes2, final @NotNull List<EvaNode> aGeneratedClasses) {
			deduceTypes2     = aDeduceTypes2;
			generatedClasses = aGeneratedClasses;
		}

		@Override
		public String report() {
			var l = generatedClasses.stream()
			  .map(evaNode -> evaNode.identityString())
			  .collect(Collectors.toList());

			return "@@ DeduceTypes2::deduceFunctions: [post] "+ deduceTypes2._module().getFileName() + " " + l;
		}
	}

	public class DeduceTypes2__FoundElement__do_assign_call implements FlowK {
		private final DeduceTypes2 deduceTypes2;
		private final IdentIA identIA;
		private final OS_Element e;
		private final ProcTableEntry pte;
		private final VariableTableEntry vte;
		private final BaseEvaFunction generatedFunction;

		public DeduceTypes2__FoundElement__do_assign_call(final DeduceTypes2 aDeduceTypes2, final @NotNull IdentIA aIdentIA, final OS_Element aE, final @NotNull ProcTableEntry aPte, final @NotNull VariableTableEntry aVte, final @NotNull BaseEvaFunction aGeneratedFunction) {
			deduceTypes2 = aDeduceTypes2;
			identIA      = aIdentIA;
			e            = aE;
			pte          = aPte;
			vte          = aVte;
			generatedFunction = aGeneratedFunction;
		}

		@Override
		public String report() {
			final String s = "@@ DeduceTypes2::...:FoundElement:do_assign_call " + identIA.getEntry().toString() + " " + e;
			return s;
		}
	}

	public class DeduceTypes2__onExitFunction implements FlowK {
		private final BaseEvaFunction generatedFunction;
		private final BaseFunctionDef fd;

		public DeduceTypes2__onExitFunction(final @NotNull BaseEvaFunction aGeneratedFunction, final BaseFunctionDef aFd) {
			generatedFunction = aGeneratedFunction;
			fd                = aFd;
		}

		@Override
		public String report() {
			return "@@ DeduceTypes2::onExitFunction: "+generatedFunction.getFunctionName();
		}
	}

	class EntryPointList_generateFromEntryPoints__epp_process__post implements FlowK {
		private final EntryPointProcessor epp;

		public EntryPointList_generateFromEntryPoints__epp_process__post(final EntryPointProcessor aEpp) {
			epp = aEpp;
		}

		@Override
		public String report() {
			return "";//EntryPointList::generateFromEntryPoints: epp [post: process]";
		}
	}

	class EntryPointList_generateFromEntryPoints__epp_process__pre implements FlowK {
		private final EntryPointProcessor epp;

		public EntryPointList_generateFromEntryPoints__epp_process__pre(final EntryPointProcessor aEpp) {
			epp = aEpp;
		}

		@Override
		public String report() {
			return "";//EntryPointList::generateFromEntryPoints: epp [pre: process]";
		}
	}

	class EntryPointList_generateFromEntryPoints__epp_size implements FlowK {
		private final int size;

		public EntryPointList_generateFromEntryPoints__epp_size(final int aSize) {
			size = aSize;
		}

		@Override
		public String report() {
			return "@@ EntryPointList::generateFromEntryPoints: epp size == "+size;
		}
	}

	class EntryPointList_generateFromEntryPoints__eps_isEmpty implements FlowK {
		private final OS_Module mod;

		public EntryPointList_generateFromEntryPoints__eps_isEmpty(final OS_Module aModule) {
			mod = aModule;
		}

		@Override
		public String report() {
			return "@@ EntryPointList::generateFromEntryPoints: eps isEmpty for " + mod.getFileName();
		}
	}

	public record DeducePhase__deduceModule(OS_Module module,
	                                        Iterable<EvaNode> generatedNodes) implements FlowK {
		@Override
		public String report() {
			final var l = new ArrayList<>();

			for (final EvaNode aEvaNode : generatedNodes) {
				l.add(aEvaNode);
			}

			return "@@ DeducePhase::deduceModule:"
			  + " " + module.getFileName()
			  + " " + l;
		}
	}

	String report();
}
