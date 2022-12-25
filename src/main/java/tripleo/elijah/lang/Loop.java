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

public class Loop implements  StatementItem, FunctionItem, OS_Element {

	private final OS_Element parent;
	private Scope3 scope3;

	@Deprecated public Loop(final OS_Element aParent) {
		// document assumption
		if (!(aParent instanceof FunctionDef) && !(aParent instanceof Loop))
			System.out.println("parent is not FunctionDef or Loop");
		parent = aParent;
	}

	public Loop(final OS_Element aParent, final Context ctx) {
		// document assumption
		if (!(aParent instanceof FunctionDef) && !(aParent instanceof Loop))
			System.out.println("parent is not FunctionDef or Loop");
		parent = aParent;
		_a.setContext(new LoopContext(ctx, this));
	}

	public void type(final LoopTypes aType) {
		type = aType;
	}

	public void expr(final IExpression aExpr) {
		expr=aExpr;
	}

	public void topart(final IExpression aExpr) {
		topart=aExpr;
	}

	public void frompart(final IExpression aExpr) {
		frompart=aExpr;
	}

	public void iterName(final IdentExpression s) {
//		assert type == ITER_TYPE;
		iterName=s;
	}

	IdentExpression iterName;
	/**
	 * @category type
	 */
	private LoopTypes type;

private IExpression topart,frompart;
private IExpression expr;
private final Attached _a = new Attached();

	public List<StatementItem> getItems() {
		final List<StatementItem> collection = new ArrayList<StatementItem>();
		for (final OS_Element element : scope3.items()) {
			if (element instanceof FunctionItem)
				collection.add((StatementItem) element);
		}
		return collection;
//		return items;
	}

	@Override // OS_Element
	public void visitGen(final ElElementVisitor visit) {
		visit.visitLoop(this);
	}

	public String getIterName() {
		return iterName.getText();
	}
	
	public LoopTypes getType() {
		return type;
	}
	
	public IExpression getToPart() {
		return topart;
	}
	
	public IExpression getExpr() {
		return expr;
	}
	
	public IExpression getFromPart() {
		return frompart;
	}

	public void setContext(final LoopContext ctx) {
		_a.setContext(ctx);
	}

	public IdentExpression getIterNameToken() {
		return iterName;
	}

	public void scope(final Scope3 sco) {
		scope3 = sco;
	}

	@Override
	public OS_Element getParent() {
		return parent;
	}

	@Override
	public Context getContext() {
		return _a.getContext();
	}

}

//
//
//
