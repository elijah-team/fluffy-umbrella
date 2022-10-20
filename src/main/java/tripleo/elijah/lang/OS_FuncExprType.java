/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

/**
 * Created 8/6/20 5:59 PM
 */
public class OS_FuncExprType extends OS_Type {
	private final FuncExpr func_expr;

	@Override
	public OS_Element getElement() {
		return func_expr;
	}

	public OS_FuncExprType(final FuncExpr funcExpr) {
		super(Type.FUNC_EXPR);
		this.func_expr = funcExpr;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("<OS_FuncExprType %s>", func_expr);
	}

}

//
//
//
