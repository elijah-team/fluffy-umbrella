/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import tripleo.elijah.contexts.IfConditionalContext;
import tripleo.elijah.gen.ICodeGen;

import java.util.ArrayList;
import java.util.List;

public class IfConditional implements StatementItem, FunctionItem, OS_Element {

//	private final IfConditional sibling;
	private final List<IfConditional> parts = new ArrayList<IfConditional>();
	private IExpression expr;
//	private final List<OS_Element> _items = new ArrayList<OS_Element>();
//	final IfConditionalScope _scope = new IfConditionalScope(this);
	private final OS_Element _parent;
	private Context _ctx;
	private Scope3 scope3;

	public IfConditional(final OS_Element _parent) {
		this._parent = _parent;
		this._ctx = null;
//		this.sibling = null;
	}

	public IfConditional(final IfConditional ifExpression) {
//		this.sibling = ifExpression;
		//
		this._ctx = new IfConditionalContext(ifExpression._ctx, this, true);
		this._parent = ifExpression._parent;
	}
	
	@Override
	public void visitGen(final ICodeGen visit) {
		visit.visitIfConditional(this);
	}

	@Override
	public OS_Element getParent() {
		return _parent;
	}

	@Override
	public Context getContext() {
		return _ctx;
	}

	public IExpression getExpr() {
		return expr;
    }

	public IfConditional else_() {
		final IfConditional elsepart = new IfConditional(this);
		parts.add(elsepart);
		return elsepart;
	}

	public IfConditional elseif() {
		final IfConditional elseifpart = new IfConditional(this);
		parts.add(elseifpart);
		return elseifpart;
	}

	/**
	 * will not be null during if or elseif
	 *
	 * @param expr
	 */
	public void expr(final IExpression expr) {
		this.expr = expr;
	}

	public List<OS_Element> getItems() {
		return scope3.items();
//		return _items;
	}

	public List<IfConditional> getParts() {
		return parts;
	}

	public void setContext(final IfConditionalContext ifConditionalContext) {
		_ctx = ifConditionalContext;
	}

	public void scope(final Scope3 sco) {
		scope3 = sco;
	}

	/*private class IfConditionalScope extends AbstractScope2 {
		private List<Token> mDocs;

		protected IfConditionalScope(OS_Element aParent) {
			super(aParent);
			assert aParent == IfConditional.this;
		}

		@Override
		public void addDocString(final Token s) {
			if (mDocs == null)
				mDocs = new ArrayList<Token>();
			mDocs.add(s);
		}

//		/*@ requires parent != null; * /
//		@Override
//		public void statementWrapper(final IExpression aExpr) {
//			//if (parent_scope == null) throw new IllegalStateException("parent is null");
//			add(new StatementWrapper(aExpr, getContext(), getParent()));
//		}

	    @Override
		public StatementClosure statementClosure() {
			return new AbstractStatementClosure(this); // TODO
		}

		@Override
		public void add(final StatementItem aItem) {
			IfConditional.this.add(aItem);
		}
	}*/

	private void add(final StatementItem aItem) {
		scope3.add((OS_Element) aItem);
		//_items.add((OS_Element) aItem);
	}

}

//
//
//
