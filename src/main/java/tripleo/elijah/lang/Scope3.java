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
import tripleo.elijah.contexts.IfConditionalContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 1/4/21 3:10 AM
 */
public class Scope3 implements Documentable {
	private final OS_Element parent;
	private final Scope3StatementClosure asc = new Scope3StatementClosure();
	private final List<OS_Element> _items = new ArrayList<OS_Element>();
	private final List<Token> _docstrings = new ArrayList<Token>();

	public Scope3(OS_Element aParent) {
		parent = aParent;
	}

	public List<OS_Element> items() {
		return _items;
	}

	public Iterable<? extends Token> docstrings() {
		return _docstrings;
	}

	@Override
	public void addDocString(Token aText) {
		_docstrings.add(aText);
	}

	public void add(OS_Element element) {
		_items.add(element);
	}

	public OS_Element getParent() {
		return parent;
	}

	public StatementClosure statementClosure() {
		return asc;
	}

	public void statementWrapper(IExpression expr) {
		add(new StatementWrapper(expr, parent.getContext(), parent)); // TODO is this right?
	}

	public VariableSequence varSeq() {
		return asc.varSeq(asc.getParent().getContext());
	}

	private class Scope3StatementClosure implements StatementClosure {
		@Override
		public void constructExpression(final IExpression aExpr, final ExpressionList aO) {
			final ConstructStatement constructExpression = new ConstructStatement(parent, parent.getContext(), aExpr, null, aO); // TODO provide for name
			add(constructExpression);
		}

		@Override
		public IfConditional ifConditional(final OS_Element aParent, final Context cur) {
			IfConditional ifex = new IfConditional(aParent);
			ifex.setContext(new IfConditionalContext(cur, ifex));
			add(ifex);
			return ifex;
		}

		@Override
		public BlockStatement blockClosure() {
			BlockStatement bs = new BlockStatement(null);
//			add(bs);  // TODO make this an Element
			return bs;
		}

		@Override
		public Loop loop() {
			Loop loop = new Loop(parent, parent.getContext());
			add(loop);
			return loop;
		}

		@Override
		public ProcedureCallExpression procedureCallExpression() {
			ProcedureCallExpression pce = new ProcedureCallExpression();
			add(new StatementWrapper(pce, getParent().getContext(), getParent()));
			return pce;
		}

		@Override
		public VariableSequence varSeq(final Context ctx) {
			VariableSequence vsq = new VariableSequence(ctx);
			vsq.setParent(parent); // TODO look at this
			assert ctx == parent.getContext();
			vsq.setContext(ctx);
			add(vsq);
			return vsq;
		}

		private OS_Element getParent() {
			return parent;
		}

		@Override
		public void yield(final IExpression aExpr) {
			final YieldExpression yiex = new YieldExpression(aExpr);
			add(yiex);
		}

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
//			parent_scope.statementWrapper(expr);
			add(new StatementWrapper(expr, parent.getContext(), parent)); // TODO is this right?
		}
	}
}

//
//
//
