package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.nextgen.outputstatement.EG_CompoundStatement;
import tripleo.elijah.nextgen.outputstatement.EG_SingleStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_ProcTableEntry;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;

public enum _GF {
	;

	private static @NotNull EG_Statement forDeduceElement3_ProcTableEntry(@NotNull final DeduceElement3_ProcTableEntry de_pte, final @NotNull GenerateC gc) {
		final EG_SingleStatement beginning;
		final EG_SingleStatement ending;
		final EG_Statement       middle;
		final boolean            indent = false;
		final EX_Explanation     explanation;

		final ProcTableEntry pte = de_pte.getTablePrincipal();

		final BaseEvaFunction gf          = de_pte.getGeneratedFunction();
		final Instruction     instruction = de_pte.getInstruction();

		final EG_Statement sb = __Pte_Dispatch.dispatch(pte, new __Pte_Dispatch() {
			// README funny thing is, this is a class vv
			@Override
			public @NotNull EG_Statement statementForExpression(final IExpression expression) {
				return new __Pte_Dispatch_IExpression_Statement(expression, instruction, gf, gc);
			}

			@Override
			public @NotNull EG_Statement statementForExpressionNum(final InstructionArgument expression_num) {
				return new __Pte_Dispatch_InstructionArgument_Statement(expression_num, instruction, gf, gc);
			}
		});

		beginning   = new EG_SingleStatement("", EX_Explanation.withMessage("forDeduceElement3_ProcTableEntry >> beginning"));
		ending      = new EG_SingleStatement("", EX_Explanation.withMessage("forDeduceElement3_ProcTableEntry >> ending"));
		explanation = new EX_ProcTableEntryExplanation(de_pte);
		middle      = sb;

		final EG_CompoundStatement stmt = new EG_CompoundStatement(beginning, ending, middle, indent, explanation);
		return stmt;
	}

	interface __Pte_Dispatch {
		static EG_Statement dispatch(@NotNull final ProcTableEntry pte, final @NotNull _GF.__Pte_Dispatch xy) {
			if (pte.expression_num == null) {
				return xy.statementForExpression(pte.__debug_expression());
			} else {
				return xy.statementForExpressionNum(pte.expression_num);
			}
		}

		EG_Statement statementForExpression(IExpression expression);

		EG_Statement statementForExpressionNum(InstructionArgument expreesion_num);
	}
}
