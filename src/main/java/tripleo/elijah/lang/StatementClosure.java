/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah.lang;

public interface StatementClosure {

	VariableSequence varSeq(Context ctx);

	ProcedureCallExpression procedureCallExpression();

	Loop loop();

//	StatementClosure procCallExpr();

	void constructExpression(IExpression aExpr, ExpressionList aO);

	void yield(IExpression aExpr);

	IfConditional ifConditional(OS_Element aParent, Context aCtx);

	BlockStatement blockClosure();

	CaseConditional caseConditional(Context parentContext);

	MatchConditional matchConditional(Context parentContext);

    void statementWrapper(IExpression expr);

    // TODO new
	//IdentList identList();
}

//
//
//
