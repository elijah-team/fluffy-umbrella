package tripleo.elijah.comp.internal;

import antlr.Token;
import tripleo.elijah.ci.*;
import tripleo.elijah.ci_impl.CiExpressionListImpl;
import tripleo.elijah.ci_impl.CiProcedureCallExpressionImpl;
import tripleo.elijah.ci_impl.CompilerInstructionsImpl;
import tripleo.elijah.ci_impl.LibraryStatementPartImpl;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang2.BuiltInTypes;

public class PCon {
	public IExpression ExpressionBuilder_build(final IExpression aEe, final ExpressionKind aEk,
	                                           final IExpression aE2) {
		return ExpressionBuilder.build(aEe, aEk, aE2);
	}

	public IExpression newCharLitExpression(final Token aC) {
		return new CharLitExpression(aC);
	}

	public CompilerInstructions newCompilerInstructions() {
		return new CompilerInstructionsImpl();
	}

	public IExpression newDotExpression(final IExpression aDotExpressionLeft, final IdentExpression aDotExpressionRightIdent) {
		return new DotExpression(aDotExpressionLeft, aDotExpressionRightIdent);
	}

	public ExpressionList newExpressionList() {
		return new ExpressionList();
	}

	public IExpression newFloatExpression(final Token aF) {
		return new FloatExpression(aF);
	}

	public GenerateStatement newGenerateStatement() {
		return new GenerateStatement();
	}

	public IExpression newGetItemExpression(final IExpression aEe, final IExpression aExpr) {
		return new GetItemExpression(aEe, aExpr);
	}

	public IdentExpression newIdentExpression(final Token aR1, final String aFilename, final Context aCur) {
		return new IdentExpression(aR1, aFilename, aCur);
	}

	public IdentExpression newIdentExpression(final Token aR1, final Context aCur) {
		return new IdentExpression(aR1, aCur);
	}

	public LibraryStatementPart newLibraryStatementPart() {
		return new LibraryStatementPartImpl();
	}

	public IExpression newListExpression() {
		return new ListExpression();
	}

	public IExpression newNumericExpression(final Token aN) {
		return new NumericExpression(aN);
	}

	public OS_Type newOS_BuiltinType(final BuiltInTypes aBuiltInTypes) {
		return new OS_BuiltinType(aBuiltInTypes);
	}

	public ProcedureCallExpression newProcedureCallExpression() {
		return new ProcedureCallExpression();
	}

	public Qualident newQualident() {
		return new Qualident();
	}

	public IExpression newSetItemExpression(final GetItemExpression aEe, final IExpression aExpr) {
		return new SetItemExpression(aEe, aExpr);
	}

	public IExpression newStringExpression(final Token aS) {
		return new StringExpression(aS);
	}

	public IExpression newSubExpression(final IExpression aEe) {
		return new SubExpression(aEe);
	}

	public CiExpressionList newCiExpressionList() {
		return new CiExpressionListImpl();
	}

	public CiProcedureCallExpression newCiProcedureCallExpression() {
		return new CiProcedureCallExpressionImpl();
	}

	public IExpression ExpressionBuilder_build(final IExpression aEe, final ExpressionKind aE2, final IExpression aE3, final OS_Type aT) {
		// TODO 10/15 look at me
		return ExpressionBuilder_build(aEe, aE2, aE3);
	}

	public CompilerInstructions new_CompilerInstructionsImpl() {
		return new CompilerInstructionsImpl();
	}
}
