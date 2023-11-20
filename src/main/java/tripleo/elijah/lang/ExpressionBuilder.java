/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
/*
 * Created on Sep 2, 2005 2:28:42 PM
 *
 * $Id$
 *
 */
package tripleo.elijah.lang;


public class ExpressionBuilder {

	public static IBinaryExpression buildPartial(final IExpression left, final ExpressionKind aType) {
		return new BasicBinaryExpression(left, aType, null);
	}

	public static IBinaryExpression build(final IExpression left, final ExpressionKind aType, final IExpression aExpression) {
		return new BasicBinaryExpression(left, aType, aExpression);
	}

	public static IExpression build(final IExpression left, final ExpressionKind aType) {
		return new AbstractExpression(left, aType) {
			OS_Type _type;

			@Override
			public boolean is_simple() {
				return false; // TODO whoa
			}

			@Override
			public OS_Type getType() {
				return _type;
			}

			@Override
			public void setType(final OS_Type deducedExpression) {
				_type = deducedExpression;
			}
		};
	}

}

//
//
//
