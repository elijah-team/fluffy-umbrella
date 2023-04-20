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
 * Created 5/8/21 7:13 AM
 */
public class UnaryExpression extends AbstractExpression {

	public UnaryExpression(final ExpressionKind aKind, final IExpression aExpression) {
		_kind = aKind;
		left  = aExpression;
	}

	@Override
	public boolean is_simple() {
		return false;
	}

	@Override
	public OS_Type getType() {
		return null;
	}

	@Override
	public void setType(final OS_Type deducedExpression) {

	}


}

//
//
//
