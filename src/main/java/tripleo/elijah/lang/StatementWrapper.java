package tripleo.elijah.lang;

import tripleo.elijah.lang2.ElElementVisitor;

public class StatementWrapper implements StatementItem, FunctionItem, OS_Element {

    private final IExpression expr;
    private final Context _ctx;
    private final OS_Element _parent;

    public StatementWrapper(final IExpression aExpression, final Context aContext, final OS_Element aParent) {
        expr = aExpression;
        _ctx = aContext;
        _parent = aParent;
    }

    @Override
    public Context getContext() {
        return _ctx;
    }

	@Override
	public OS_Element getParent() {
		return _parent;
	}

	@Override
	public String toString() {
		return expr.toString();
	}

	/**
	 * @return the expr
	 */
	public IExpression getExpr() {
		return expr;
	}

	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitStatementWrapper(this);
	}

}

//
//
//
