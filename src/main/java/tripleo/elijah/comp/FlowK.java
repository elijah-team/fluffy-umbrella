package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.entrypoints.EntryPointProcessor;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNode;

import java.util.ArrayList;

public interface FlowK {
	String report();

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

	class EntryPointList_generateFromEntryPoints__epp_process__pre implements FlowK {
		private final EntryPointProcessor epp;

		public EntryPointList_generateFromEntryPoints__epp_process__pre(final EntryPointProcessor aEpp) {
			epp = aEpp;
		}

		@Override
		public String report() {
			return "EntryPointList::generateFromEntryPoints: epp [pre: process]";
		}
	}

	class EntryPointList_generateFromEntryPoints__epp_process__post implements FlowK {
		private final EntryPointProcessor epp;

		public EntryPointList_generateFromEntryPoints__epp_process__post(final EntryPointProcessor aEpp) {
			epp = aEpp;
		}

		@Override
		public String report() {
			return "EntryPointList::generateFromEntryPoints: epp [post: process]";
		}
	}

	public record DeducePhase__deduceModule(OS_Module module,
	                                        Iterable<GeneratedNode> generatedNodes) implements FlowK {
		@Override
		public String report() {
			final var l = new ArrayList<>();

			for (final GeneratedNode aGeneratedNode : generatedNodes) {
				l.add(aGeneratedNode);
			}

			return "DeducePhase::deduceModule:"
			  + " " + module.getFileName()
			  + " " + l;
		}
	}

	public class DeduceTypes2__deduce_generated_function_base implements FlowK {
		private final BaseGeneratedFunction generatedFunction;
		private final BaseFunctionDef fd;

		public DeduceTypes2__deduce_generated_function_base(final @NotNull BaseGeneratedFunction aGeneratedFunction, final @NotNull BaseFunctionDef aFd) {
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

	public class DeduceTypes2__onExitFunction implements FlowK {
		private final BaseGeneratedFunction generatedFunction;
		private final BaseFunctionDef fd;

		public DeduceTypes2__onExitFunction(final @NotNull BaseGeneratedFunction aGeneratedFunction, final BaseFunctionDef aFd) {
			generatedFunction = aGeneratedFunction;
			fd                = aFd;
		}

		@Override
		public String report() {
			return "@@ DeduceTypes2::onExitFunction: "+generatedFunction.getFunctionName();
		}
	}

	public class DeduceTypes2__deduceFunctions__post implements FlowK {
		private final DeduceTypes2 deduceTypes2;
		private final List<GeneratedNode> generatedClasses;

		public DeduceTypes2__deduceFunctions__post(final DeduceTypes2 aDeduceTypes2, final @NotNull List<GeneratedNode> aGeneratedClasses) {
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
		private final BaseGeneratedFunction generatedFunction;

		public DeduceTypes2__FoundElement__do_assign_call(final DeduceTypes2 aDeduceTypes2, final @NotNull IdentIA aIdentIA, final OS_Element aE, final @NotNull ProcTableEntry aPte, final @NotNull VariableTableEntry aVte, final @NotNull BaseGeneratedFunction aGeneratedFunction) {
			deduceTypes2 = aDeduceTypes2;
			identIA      = aIdentIA;
			e            = aE;
			pte          = aPte;
			vte          = aVte;
			generatedFunction = aGeneratedFunction;
		}

		@Override
		public String report() {
			return "@@ DeduceTypes2::...:FoundElement:do_assign_call " + identIA.getEntry().toString() + " " + e;
		}
	}
}
