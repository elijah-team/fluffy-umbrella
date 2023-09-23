/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import tripleo.elijah.contexts.LoopContext;
import tripleo.elijah.lang2.ElElementVisitor;

import java.util.ArrayList;
import java.util.List;

public class Loop implements StatementItem, FunctionItem, OS_Element {

	private final OS_Element parent;
	private final Attached    _a = new Attached();
	IdentExpression iterName;
	private       Scope3     scope3;
	/**
	 * @category type
	 */
	private LoopTypes type;
	private IExpression topart, frompart;
	private       IExpression expr;

	@Deprecated
	public Loop(final OS_Element aParent) {
		// document assumption
		if (!(aParent instanceof FunctionDef) && !(aParent instanceof Loop))
			tripleo.elijah.util.Stupidity.println2("parent is not FunctionDef or Loop");
		parent = aParent;
	}

	public Loop(final OS_Element aParent, final Context ctx) {
		// document assumption
		if (!(aParent instanceof FunctionDef) && !(aParent instanceof Loop))
			tripleo.elijah.util.Stupidity.println2("parent is not FunctionDef or Loop");
		parent = aParent;
		_a.setContext(new LoopContext(ctx, this));
	}

	public void expr(final IExpression aExpr) {
		expr = aExpr;
	}

	public void frompart(final IExpression aExpr) {
		frompart = aExpr;
	}

	@Override
	public Context getContext() {
		return _a.getContext();
	}

	public IExpression getExpr() {
		return expr;
	}

	public IExpression getFromPart() {
		return frompart;
	}

	public List<StatementItem> getItems() {
		final List<StatementItem> collection = new ArrayList<StatementItem>();
		for (final OS_Element element : scope3.items()) {
			if (element instanceof FunctionItem)
				collection.add((StatementItem) element);
		}
		return collection;
//		return items;
	}

	public String getIterName() {
		return iterName.getText();
	}

	public IdentExpression getIterNameToken() {
		return iterName;
	}

	@Override
	public OS_Element getParent() {
		return parent;
	}

	public IExpression getToPart() {
		return topart;
	}

	public LoopTypes getType() {
		return type;
	}

	public void iterName(final IdentExpression s) {
//		assert type == ITER_TYPE;
		iterName = s;
	}

	public void scope(final Scope3 sco) {
		scope3 = sco;
	}

	public void setContext(final LoopContext ctx) {
		_a.setContext(ctx);
	}

	public void topart(final IExpression aExpr) {
		topart = aExpr;
	}

	public void type(final LoopTypes aType) {
		type = aType;
	}

	@Override // OS_Element
	public void visitGen(final ElElementVisitor visit) {
		visit.visitLoop(this);
	}

}

//
//
//
