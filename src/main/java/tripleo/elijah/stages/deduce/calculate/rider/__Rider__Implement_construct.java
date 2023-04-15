package tripleo.elijah.stages.deduce.calculate.rider;

import org.jetbrains.annotations.NotNull;

import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;

public class __Rider__Implement_construct {
	public __Rider__Implement_construct(BaseEvaFunction generatedFunction2, Instruction instruction2,
			InstructionArgument expression2, @NotNull ProcTableEntry pte2) {
		generatedFunction = generatedFunction2;
		instruction = instruction2;
		expression = expression2;
		pte = pte2;
	}

	private final BaseEvaFunction     generatedFunction;
	private final Instruction         instruction;
	private final InstructionArgument expression;

	private final @NotNull ProcTableEntry pte;

	public BaseEvaFunction getGeneratedFunction() {
		return generatedFunction;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public InstructionArgument getExpression() {
		return expression;
	}

	public ProcTableEntry getPte() {
		return pte;
	}
}