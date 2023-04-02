package tripleo.elijah.stages.instructions;

import tripleo.elijah.util.IFixedList;

public class InstructionFixedList implements IFixedList<InstructionArgument> {
	private final Instruction instruction;

	public InstructionFixedList(final Instruction aInstruction) {
		instruction = aInstruction;
	}

	@Override
	public int size() {
		return instruction.getArgsSize();
	}

	@Override
	public InstructionArgument get(final int at) {
		return instruction.getArg(at);
	}
}
