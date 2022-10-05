/**
 * 
 */
package tripleo.elijah.lang;

/**
 * @author Tripleo(envy)
 *
 * Created 	Mar 27, 2020 at 12:59:41 AM
 */
public class DotExpression extends BasicBinaryExpression {

	public DotExpression(final IExpression ee, final IdentExpression identExpression) {
		left = ee;
		right = identExpression;
		_kind = ExpressionKind.DOT_EXP;
	}

	public DotExpression(final IExpression ee, final IExpression aExpression) {
		left = ee;
		right = aExpression;
		_kind = ExpressionKind.DOT_EXP;
	}

	@Override
	public String toString() {
		return String.format("%s.%s", left, right);
	}

	@Override
	public boolean is_simple() {
		return false; // TODO when is this true or not? see {@link Qualident}
	}

}

//
//
//
