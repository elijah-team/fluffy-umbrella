/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang2.ElElementVisitor;

/*
 * Created on Sep 1, 2005 6:47:16 PM
 *
 * $Id$
 *
 */
public class ConstructStatement implements FunctionItem, StatementItem, OS_Element {
	private final OS_Element parent;
	private final Context context;
	private final IExpression _expr;
	private final ExpressionList _args;
	private final String constructorName;
//	private OS_Type _type;

	public ConstructStatement(@NotNull final OS_Element aParent,
							  @NotNull final Context aContext,
							  @NotNull final IExpression aExpr,
							  @Nullable final String aConstructorName,
							  @Nullable final ExpressionList aExpressionList) {
		parent = aParent;
		context = aContext;
		_expr = aExpr;
		constructorName = aConstructorName;
		_args = aExpressionList;
	}

//	@Override
//	public boolean is_simple() {
//		return false;
//	}
//
//	@Override
//	public void setType(final OS_Type deducedExpression) {
//		_type = deducedExpression;
//	}
//
//	@Override
//	public OS_Type getType() {
//		return _type;
//	}

	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitConstructStatement(this);
	}

	@Override
	public OS_Element getParent() {
		return parent;
	}

	@Override
	public Context getContext() {
		return context;
	}

	public IExpression getExpr() {
		return _expr;
	}

	public @Nullable ExpressionList getArgs() {
		return _args;
	}
}

//
//
//
