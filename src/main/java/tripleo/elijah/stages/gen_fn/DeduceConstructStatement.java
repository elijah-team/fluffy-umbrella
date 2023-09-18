package tripleo.elijah.stages.gen_fn;

import tripleo.elijah.lang.ConstructStatement;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.deduce.DeclAnchor;
import tripleo.elijah.stages.deduce.DeduceElement;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.ProcIA;

import java.util.List;

public class DeduceConstructStatement implements DeduceElement {
	public InstructionArgument       target;
	public boolean                   toEvaluateTarget;
	public ProcIA                    call;
	public List<InstructionArgument> args;

	public DeduceConstructStatement(final BaseEvaFunction aGf, final ConstructStatement aConstructStatement) {
	}

	@Override
	public DeclAnchor declAnchor() {
		return null;
	}

	@Override
	public OS_Element element() {
		return null;
	}
}
