/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */

package tripleo.elijah.lang;

import antlr.Token;
import org.jetbrains.annotations.Contract;
import tripleo.elijah.contexts.IfConditionalContext;

import java.util.ArrayList;
import java.util.List;


public final class AbstractStatementClosure implements StatementClosure, StatementItem {

	private OS_Element _parent;

	public AbstractStatementClosure(final Scope aParent) {
		// TODO doesn't set _parent
		parent_scope = aParent;
	}

	public AbstractStatementClosure(final ClassStatement classStatement) {
		// TODO check final member
		_parent = classStatement;
		parent_scope = new AbstractScope2(_parent) {

			@Override
			public void addDocString(final Token s1) {
				classStatement.addDocString(s1);
			}

			@Override
			public StatementClosure statementClosure() {
				return AbstractStatementClosure.this; // TODO is this right?
			}

			@Override
			public void add(final StatementItem aItem) {
				classStatement.add((ClassItem) aItem);
			}

		};
	}

	public AbstractStatementClosure(final Scope scope, final OS_Element parent1) {
		parent_scope = scope;
		_parent = parent1;
	}

	@Override
	public BlockStatement blockClosure() {
		bs=new BlockStatement(this.parent_scope);
		add(bs);
		return bs;
	}
//	public IExpression constructExpression() {
//		ctex=new ConstructStatement(this.parent_scope);
//		add(ctex);
//		return ctex;
//	}

	@Override
	public void constructExpression(final IExpression aExpr, final ExpressionList aO) {
		add(new ConstructStatement(_parent, _parent.getContext(), aExpr, null, aO)); // TODO provide for name
	}

	@Override
 	public IfConditional ifConditional(final OS_Element aParent, final Context cur) {
		ifex=new IfConditional(aParent);
		ifex.setContext(new IfConditionalContext(cur, ifex));
		add(ifex);
		return ifex;
	}

	@Override
	public Loop loop() {
		loop = new Loop(this.parent_scope.getElement());
		add(loop);
		return loop;
	}

	@Override
	public ProcedureCallExpression procedureCallExpression() {
		pce=new ProcedureCallExpression();
		add(new StatementWrapper(pce, getParent().getContext(), getParent()));
		return pce;
	}

	@Override
	public VariableSequence varSeq(final Context ctx) {
		vsq=new VariableSequence(ctx);
		vsq.setParent(parent_scope.getParent()/*this.getParent()*/); // TODO look at this
//		vsq.setContext(ctx); //redundant
		return (VariableSequence) add(vsq);
	}

	private OS_Element getParent() {
		return _parent;
	}

	@Override
	public void yield(final IExpression aExpr) {
		add(new YieldExpression(aExpr));
	}

	@Contract("_ -> param1")
	private StatementItem add(final StatementItem aItem) {
		parent_scope.add(aItem);
		return aItem;
	}
	
	private BlockStatement bs;
	private ConstructStatement ctex;
	private IfConditional ifex;
	
	private Loop loop;
	private ProcedureCallExpression pce;
	private AbstractStatementClosure pcex;
	private VariableSequence vsq;
	private YieldExpression yiex;

	final List<StatementItem> items =new ArrayList<StatementItem>();

	final Scope parent_scope;

	@Override
	public CaseConditional caseConditional(final Context parentContext) {
		final CaseConditional caseConditional = new CaseConditional(getParent(), parentContext);
		add(caseConditional);
		return caseConditional;
	}

	@Override
	public MatchConditional matchConditional(final Context parentContext) {
		final MatchConditional matchConditional = new MatchConditional(getParent(), parentContext);
		add(matchConditional);
		return matchConditional;
	}

	@Override
	public void statementWrapper(final IExpression expr) {
		parent_scope.statementWrapper(expr);
	}
}

//
//
//
