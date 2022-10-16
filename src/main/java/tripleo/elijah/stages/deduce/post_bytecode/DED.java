package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.stages.gen_fn.*;

public interface DED {

	static DED dispatch(ConstantTableEntry aCte) {
		return new DED_CTE(aCte);
	}

	static DED dispatch(IdentTableEntry aIte) {
		return new DED_ITE(aIte);
	}

	static DED dispatch(VariableTableEntry aVte) {
		return new DED_VTE(aVte);
	}

	static DED dispatch(ProcTableEntry aPte) {
		return new DED_PTE(aPte);
	}

	static DED dispatch(TypeTableEntry aCte) {
		return new DED_TTE(aCte);
	}

	Kind kind();

	enum Kind {
		DED_Kind_GeneratedFunction,
		DED_Kind_ProcTableEntry,
		DED_Kind_IdentTableEntry,
		DED_Kind_VariableTableEntry,
		DED_Kind_ConstantTableEntry,
		/*
			DED_Kind_GeneratedFunction,
			DED_Kind_GeneratedFunction,
			DED_Kind_GeneratedFunction,
			DED_Kind_GeneratedFunction,
		 */
		DED_Kind_Type,
		DED_Kind_TypeTableEntry
	}

	class DED_PTE implements DED {

		private final ProcTableEntry principal;

		public DED_PTE(ProcTableEntry aPte) {
			principal = aPte;
		}

		@Override
		public Kind kind() {
			throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
		}

	}

	class DED_TTE implements DED {

		private final TypeTableEntry principal;

		public DED_TTE(TypeTableEntry aTte) {
			principal = aTte;
		}

		@Override
		public Kind kind() {
			return Kind.DED_Kind_TypeTableEntry;
		}

	}

	class DED_CTE implements DED {

		private final ConstantTableEntry constantTableEntry;

		@Contract(pure = true)
		public DED_CTE(ConstantTableEntry aConstantTableEntry) {
			constantTableEntry = aConstantTableEntry;
		}

		public ConstantTableEntry getConstantTableEntry() {
			return constantTableEntry;
		}

		@Override
		public DED.Kind kind() {
			return DED.Kind.DED_Kind_ConstantTableEntry;
		}
	}

	class DED_ITE implements DED {

		private final IdentTableEntry identTableEntry;

		public DED_ITE(IdentTableEntry aIdentTableEntry) {
			identTableEntry = aIdentTableEntry;
		}

		public IdentTableEntry getIdentTableEntry() {
			return identTableEntry;
		}

		@Override
		public Kind kind() {
			return Kind.DED_Kind_IdentTableEntry;
		}

	}

	class DED_VTE implements DED {

		private final VariableTableEntry variableTableEntry;

		public DED_VTE(VariableTableEntry aVariableTableEntry) {
			variableTableEntry = aVariableTableEntry;
		}

		public VariableTableEntry getVariableTableEntry() {
			return variableTableEntry;
		}

		@Override
		public Kind kind() {
			return Kind.DED_Kind_VariableTableEntry;
		}

	}

}
