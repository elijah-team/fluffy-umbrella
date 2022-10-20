package tripleo.elijah.lang;

import antlr.Token;

/**
 * Created 8/30/20 2:54 PM
 */
public abstract class AbstractBlockScope implements Scope {

	private final AbstractStatementClosure asc;

	public AbstractBlockScope(final OS_Container _element) {
		this._element = _element;
		this.asc = new AbstractStatementClosure(this, getParent());
	}

	private final OS_Container _element;

	@Override
	public void add(final StatementItem aItem) {
		if (aItem instanceof FunctionItem)
			_element.add((OS_Element) aItem);
		else
			System.err.println(String.format("adding false FunctionItem %s", aItem.getClass().getName()));
	}

	@Override
	public void addDocString(final Token aS) {
		_element.addDocString(aS);
	}

	@Override
	public BlockStatement blockStatement() {
		return new BlockStatement(this);
	}

	@Override
	public InvariantStatement invariantStatement() {
		return null;
	}

	@Override
	public StatementClosure statementClosure() {
		return asc;
	}

	@Override
	public void statementWrapper(final IExpression aExpr) {
		add(new StatementWrapper(aExpr, getContext(), getParent()));
	}

	public abstract Context getContext();

	@Override
	public TypeAliasStatement typeAlias() {
		return null;
	}

	/* (non-Javadoc)
	 * @see tripleo.elijah.lang.Scope#getParent()
	 */
	@Override
	public OS_Element getParent() {
		return (OS_Element) _element;
	}

	@Override
	public OS_Element getElement() {
		return (OS_Element) _element;
	}

}
