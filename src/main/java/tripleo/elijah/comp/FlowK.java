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
		@Override
		public String report() {
			return "@@ EntryPointList::generateFromEntryPoints: eps isEmpty";
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
			return new StringBuilder().append("@@ DeduceTypes2::deduce_generated_function_base: ").append(fd.getNameNode().getText()).append(" ").append(fd.getModule().getFileName()).toString();
		}
	}
}
