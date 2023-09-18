package tripleo.elijah.stages.gen_c;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.util.UnintendedUseException;

public class GetAssignmentValueArgsStatement implements EG_Statement {
	private final List<String> sll = new ArrayList<>();
	private final Instruction  inst;

	public GetAssignmentValueArgsStatement(final Instruction aInst) {
		inst = aInst;
	}

	public void add_string(final String aS) {
		sll.add(aS);
	}

	@Override
	public EX_Explanation getExplanation() {
		throw new UnintendedUseException();
		//return null;
	}

	@Override
	public @Nullable String getText() {
//		throw new UnintendedUseException();
		return null; // TODO hmm
	}

	public List<String> stringList() {
		return sll;
	}
}