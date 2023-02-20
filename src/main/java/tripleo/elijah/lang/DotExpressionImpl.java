/**
 * 
 */
package tripleo.elijah.lang;

/**
 * @author Tripleo(envy)
 * <p>
 * Created 	Mar 27, 2020 at 12:59:41 AM
 */
public class DotExpressionImpl extends BasicBinaryExpression {

	public DotExpressionImpl(final IExpression ee, final IdentExpression identExpression) {
		left  = ee;
		right = identExpression;
		_kind = ExpressionKind.DOT_EXP;
	}

	public DotExpressionImpl(final IExpression ee, final IExpression aExpression) {
		left  = ee;
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
