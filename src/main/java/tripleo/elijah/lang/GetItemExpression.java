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

/**
 * @author Tripleo
 *
 * Created 	Apr 16, 2020 at 7:58:36 AM
 */
public class GetItemExpression extends AbstractExpression { // TODO binary?

	public IExpression index; // TODO what about multidimensional arrays?

	public GetItemExpression(final IExpression ee, final IExpression expr) {
		this.left    = ee;
		this.index   = expr;
		this._kind   = ExpressionKind.GET_ITEM;
	}

	/* (non-Javadoc)
	 * @see tripleo.elijah.lang.IExpression#getKind()
	 */
	@Override
	public ExpressionKind getKind() {
		return ExpressionKind.GET_ITEM;
	}

	/* (non-Javadoc)
	 * @see tripleo.elijah.lang.IExpression#is_simple()
	 */
	@Override
	public boolean is_simple() {
		return false; // TODO is this correct? Let's err on the side of caution
	}

	OS_Type _type;

	@Override
	public void setType(final OS_Type deducedExpression) {
		_type = deducedExpression;
	}

	@Override
	public OS_Type getType() {
		return _type;
	}

	public void parens(final Token lb, final Token rb) {
		// TODO implement me later
		
	}

}

//
//
//
