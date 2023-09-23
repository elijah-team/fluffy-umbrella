package tripleo.elijah.stages.gen_c;

import static tripleo.elijah.stages.gen_c.Generate_Code_For_Method.AOG.GET;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.stages.gen_c.statements.ReasonedStringListStatement;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.InstructionFixedList;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.util.Helpers;

class __Pte_Dispatch_IExpression_Statement extends ReasonedStringListStatement {
	private final IExpression expression;
	private final Instruction instruction;
	private final BaseEvaFunction gf;
	private final GenerateC       gc;

	public __Pte_Dispatch_IExpression_Statement(final IExpression aExpression,
												final Instruction aInstruction,
												final BaseEvaFunction aGf,
												final @NotNull GenerateC aGc) {
		expression  = aExpression;
		instruction = aInstruction;
		gf          = aGf;
		gc          = aGc;

		var z = this;

		final IdentExpression ptex = (IdentExpression) expression;
		final String          text = ptex.getText();

		@Nullable final InstructionArgument xx = gf.vte_lookup(text);
		assert xx != null;

		final List<String> sl3 = gc.getArgumentStrings(() -> new InstructionFixedList(instruction));

		z.append(Emit.emit("/*424*/"), "emit-code");
		z.append(() -> gc.getRealTargetName((IntegerIA) xx, GET), "real-target-name");
		z.append("(", "open-brace");
		z.append(Helpers.String_join(", ", sl3), "arguments");
		z.append(");", "close-brace");
	}
}
