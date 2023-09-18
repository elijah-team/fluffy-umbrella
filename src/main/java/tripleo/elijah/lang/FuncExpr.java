/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import tripleo.elijah.contexts.FuncExprContext;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;

/**
 * @author Tripleo
 * <p>
 * Created 	Mar 30, 2020 at 7:41:52 AM
 */
public class FuncExpr extends BaseFunctionDef implements IExpression, OS_Element {

	//	private FormalArgList argList = new FormalArgList();
	private TypeName        _returnType;
	private OS_Type         _type;
	private FuncExprContext _ctx;
//	private Scope3 scope3;

	@Override
	public List<FormalArgListItem> getArgs() {
		return mFal.falis;
	}

	@Override
	public Context getContext() {
		return _ctx;
	}

	/****** FOR IEXPRESSION ******/
	@Override
	public ExpressionKind getKind() {
		return ExpressionKind.FUNC_EXPR;
	}

//	public List<FunctionItem> getItems() {
//		List<FunctionItem> collection = new ArrayList<FunctionItem>();
//		for (OS_Element element : scope3.items()) {
//			if (element instanceof FunctionItem)
//				collection.add((FunctionItem) element);
//		}
//		return collection;
////		return items;
//	}

	@Override
	public IExpression getLeft() {
		return null;
	}

	@Override
	public OS_Element getParent() {
//		throw new NotImplementedException();
		return null; // getContext().getParent().carrier() except if it is an Expression; but Expression is not an Element
	}

	// region arglist

	public Scope3 getScope() {
		return scope3;
	}

	@Override
	public OS_Type getType() {
		return _type;
	}

	// endregion

	@Override
	public boolean is_simple() {
		return false;
	}

	@Override
	public void postConstruct() {
		// nop
	}

	@Override
	public String repr_() {
		return null;
	}

	public TypeName returnType() {
		return _returnType;
	}

	public void setArgList(final FormalArgList argList) {
		mFal = argList;
	}

	public void setContext(final FuncExprContext ctx) {
		_ctx = ctx;
	}

	@Override
	public void setKind(final ExpressionKind aKind) {
		throw new NotImplementedException();
	}	@Override
	public void setLeft(final IExpression iexpression) {
		throw new NotImplementedException();
	}

	public void setReturnType(final TypeName tn) {
		_returnType = tn;
	}	/************* FOR THE OTHER ONE ******************/
	@Override
	public void setType(final OS_Type deducedExpression) {
		_type = deducedExpression;
	}

	public void type(final TypeModifiers modifier) {
		assert modifier == TypeModifiers.FUNCTION ||
		  modifier == TypeModifiers.PROCEDURE;
	}

	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitFuncExpr(this);
	}



//	@Override
//	public void scope(Scope3 sco) {
//		scope3 = sco;
//	}


}

//
//
//
