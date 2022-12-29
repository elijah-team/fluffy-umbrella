package tripleo.elijah.stages.deduce.post_bytecode;

import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;

public class DeduceElement3_ProcTableEntry implements IDeduceElement3 {
	private ProcTableEntry principal;
	
	public DeduceElement3_ProcTableEntry(ProcTableEntry procTableEntry) {
		principal = procTableEntry;
	}
	
	@Override
	public void resolve(IdentIA aIdentIA, Context aContext, FoundElement aFoundElement) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void resolve(Context aContext, DeduceTypes2 dt2) {
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
		throw new UnsupportedOperationException();
//		return null;
	}
	
	@Override
	public GeneratedFunction generatedFunction() {
		return null;
	}
	
	@Override
	public GenType genType() {
		return null;
	}
	
	@Override
	public DeduceElement3_Kind kind() {
		return DeduceElement3_Kind.GEN_FN__PTE;
	}
}
