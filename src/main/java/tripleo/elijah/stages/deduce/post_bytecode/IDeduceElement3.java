/*
	package tripleo.elijah.stages.deduce.post_bytecode;
       
	import tripleo.elijah.stages.gen_fn.IdentTableEntry;
	import tripleo.elijah.stages.gen_fn.VariableTableEntry;
       
	public class DED_VTE implements DED {
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
*/
package tripleo.elijah.stages.deduce.post_bytecode;

import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.instructions.IdentIA;

public interface IDeduceElement3 {
	void resolve(IdentIA aIdentIA, Context aContext, FoundElement aFoundElement);

	void resolve(Context aContext, final DeduceTypes2 dt2);

	OS_Element getPrincipal();

	DED elementDiscriminator();

	DeduceTypes2 deduceTypes2();

	BaseGeneratedFunction generatedFunction();

	GenType genType();

	/**
	 * how is this different from {@link DED.Kind} ??
	 *
	 * @return
	 */
	DeduceElement3_Kind kind();

	enum DeduceElement3_Kind {
		CLASS,
		NAMESPACE,
		FUNCTION,
		GEN_FN__VTE,
		GEN_FN__ITE,
		GEN_FN__CTE,
		GEN_FN__PTE
		// ...
	}
}
