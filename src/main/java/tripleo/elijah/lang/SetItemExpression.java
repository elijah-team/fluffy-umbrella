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
 * Created 8/6/20 1:15 PM
 */
public class SetItemExpression extends BasicBinaryExpression {
	public SetItemExpression(final GetItemExpression left_, final IExpression right_) {
		this.setLeft(left_);
		this.setRight(right_);
		this._kind = ExpressionKind.SET_ITEM;
	}
}

//
//
//
