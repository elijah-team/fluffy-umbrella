package tripleo.elijah.stages.deduce.tastic;

import tripleo.elijah.lang.Context;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.Instruction;

public interface ITastic {
	void do_assign_call(BaseEvaFunction aGeneratedFunction, Context aContext, VariableTableEntry aVte, Instruction aInstruction);

	void do_assign_call(BaseEvaFunction aGeneratedFunction, Context aFdCtx, IdentTableEntry aIdte, int aIndex);
}
