package tripleo.elijah.stages.deduce.post_bytecode;

import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;

public class DeduceElement3_ProcTableEntry implements IDeduceElement3 {
	private final ProcTableEntry principal;
	private final DeduceTypes2 deduceTypes2;
	private final BaseGeneratedFunction generatedFunction;
	private Instruction instruction;

	public DeduceElement3_ProcTableEntry(final ProcTableEntry aProcTableEntry, final DeduceTypes2 aDeduceTypes2, final BaseGeneratedFunction aGeneratedFunction) {
		principal = aProcTableEntry;
		deduceTypes2 = aDeduceTypes2;
		generatedFunction = aGeneratedFunction;
	}

	@Override
	public void resolve(final IdentIA aIdentIA, final Context aContext, final FoundElement aFoundElement) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resolve(final Context aContext, final DeduceTypes2 dt2) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public OS_Element getPrincipal() {
		//return principal.getDeduceElement3(deduceTypes2, generatedFunction).getPrincipal(); // README infinite loop

		return principal.getResolvedElement();//getDeduceElement3(deduceTypes2, generatedFunction).getPrincipal();
	}
	
	@Override
	public DED elementDiscriminator() {
		return new DED.DED_PTE(principal);
	}
	
	@Override
	public DeduceTypes2 deduceTypes2() {
		return deduceTypes2;
	}

	@Override
	public BaseGeneratedFunction generatedFunction() {
		return generatedFunction;
	}

	@Override
	public GenType genType() {
		throw new UnsupportedOperationException("no type for PTE");
	} // TODO check correctness

	@Override
	public DeduceElement3_Kind kind() {
		return DeduceElement3_Kind.GEN_FN__PTE;
	}

	public ProcTableEntry getTablePrincipal() {
		return principal;
	}

	public BaseGeneratedFunction getGeneratedFunction() {
		return generatedFunction;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public void setInstruction(final Instruction aInstruction) {
		instruction = aInstruction;
	}
}
