package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_c.statements.ReasonedStringListStatement;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.InstructionFixedList;

import java.util.List;

class __Pte_Dispatch_InstructionArgument_Statement extends ReasonedStringListStatement {
	private final InstructionArgument expression_num;
	private final Instruction         instruction;
	private final BaseEvaFunction     gf;
	private final GenerateC           gc;

	private final SpecialText[] xx;

	public __Pte_Dispatch_InstructionArgument_Statement(final InstructionArgument aExpressionNum,
														final Instruction aInstruction,
														final BaseEvaFunction aGf,
														final GenerateC aGc) {
		expression_num = aExpressionNum;
		instruction    = aInstruction;
		gf             = aGf;
		gc             = aGc;

		var z = this;
		xx = new SpecialText[1];

		z.append(Emit.emit("/*427-1*/"), "emit-code");
		z.append(() -> {
			final IdentIA identIA = (IdentIA) expression_num;

			final CReference reference = new CReference(gc.repo(), gc.ce);
			xx[0] = reference.getIdentIAPath(identIA, Generate_Code_For_Method.AOG.GET, null);
			final List<String> sl3 = gc.getArgumentStrings(() -> new InstructionFixedList(instruction));
			reference.args(sl3);
			final @NotNull String path = reference.build();

			return path;
		}, "path");
		z.append(";", "close-semi");
	}
}
