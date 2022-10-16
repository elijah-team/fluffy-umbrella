package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.deduce.post_bytecode.DED.DED_VTE;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;

public class DeduceElement3_VariableTableEntry implements IDeduceElement3 {

	private final VariableTableEntry principal;
	private       DeduceTypes2       deduceTypes2;
	private       GeneratedFunction  generatedfunction;
	private       GenType            genType;

	@Contract(pure = true)
	public DeduceElement3_VariableTableEntry(final VariableTableEntry aVariableTableEntry) {
		principal = aVariableTableEntry;
	}

	DeduceElement3_VariableTableEntry(OS_Type vte_type_attached) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	public void resolve(final IdentIA aIdentIA, final Context aContext, final FoundElement aFoundElement) {
		throw new UnsupportedOperationException("Should not be reached");
	}

	@Override
	public void resolve(final Context aContext, final DeduceTypes2 aDeduceTypes2) {
		throw new UnsupportedOperationException("Should not be reached");
	}

	@Override
	public OS_Element getPrincipal() {
		return principal.getDeduceElement3().getPrincipal();
	}

	@Override
	public DED elementDiscriminator() {
		return new DED_VTE(principal);
	}

	@Override
	public DeduceTypes2 deduceTypes2() {
		return deduceTypes2;
	}

	@Override
	public GeneratedFunction generatedFunction() {
		return generatedfunction;
	}

	@Override
	public GenType genType() {
		return genType;
	}

	@Override
	public DeduceElement3_Kind kind() {
		return DeduceElement3_Kind.GEN_FN__VTE;
	}
}
