package tripleo.elijah.comp;

import tripleo.elijah.entrypoints.EntryPointProcessor;

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
}
