/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
/**
 * 
 */
package tripleo.elijah.lang;

import antlr.Token;
import tripleo.elijah.contexts.CaseContext;
import tripleo.elijah.contexts.SingleIdentContext;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.NotImplementedException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Tripleo
 *
 * Created 	Apr 15, 2020 at 10:09:03 PM
 */
public class CaseConditional implements OS_Element, StatementItem, FunctionItem {

    private final OS_Element parent;
    private IExpression expr;
	private SingleIdentContext _ctx = null;
    private final HashMap<IExpression, CaseScope> scopes = new LinkedHashMap<IExpression, CaseScope>();
    private CaseScope default_case_scope = null;
	private CaseContext __ctx = null; // TODO look into removing this

	public CaseConditional(final OS_Element parent, final Context parentContext) {
		this.parent = parent;
		this._ctx   = new SingleIdentContext(parentContext, this);
	}

	public void expr(final IExpression expr) {
		this.expr = expr;
	}

	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitCaseConditional(this);
	}

	public HashMap<IExpression, CaseScope> getScopes() {
		return scopes;
	}

	@Override
	public OS_Element getParent() {
		return parent;
	}

	@Override
	public Context getContext() {
		return _ctx;
	}

	public void addScopeFor(final IExpression expression, final Scope3 caseScope) {
		addScopeFor(expression, new CaseScope(expression, caseScope));
	}

	private void addScopeFor(final IExpression expression, final CaseScope caseScope) {
		if (scopes.containsKey(expression))
			tripleo.elijah.util.Stupidity.println_err2("already has an expression" + expression); // TODO put in some verify step
		scopes.put(expression, caseScope);
	}

	public void postConstruct() {
		// nop
	}

	public IExpression getExpr() {
		return expr;
	}

	public void setContext(final CaseContext ctx) {
		__ctx = ctx;
	}

	public void scope(final Scope3 sco, final IExpression expr1) {
		addScopeFor(expr1, new CaseScope(expr1, sco));
	}

	public class CaseScope implements OS_Container, OS_Element {

		private final IExpression expr;
		private final Scope3 cscope3;
		private boolean _isDefault = false;

		public CaseScope(final IExpression expression, final Scope3 aScope3) {
			this.expr = expression;
			this.cscope3 = aScope3;
		}

		@Override
		public List<OS_Element2> items() {
			throw new NotImplementedException();
		}

		@Override
		public void add(final OS_Element anElement) {
			cscope3.add(anElement);
		}

		public List<OS_Element> getItems() {
			return cscope3.items();
		}

		@Override
		public void addDocString(final Token s1) {
			cscope3.addDocString(s1);
		}

		@Override
		public void visitGen(final ElElementVisitor visit) {
			visit.visitCaseScope(this);
		}

		@Override
		public OS_Element getParent() {
			return CaseConditional.this;
		}

		@Override
		public Context getContext() {
			return getParent().getContext();
		}

		public void setDefault() {
			_isDefault = true;
			default_case_scope = this;
			_ctx.carrier = (IdentExpression) expr;
		}
	}
}

//
//
//
