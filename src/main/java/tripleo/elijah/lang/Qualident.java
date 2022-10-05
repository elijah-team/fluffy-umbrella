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
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.util.Helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created Mar 27, 2019 at 2:24:09 PM
 *
 * @author Tripleo(sb)
 *
 */
public class Qualident  implements IExpression {

	/** Look into creating a {@link DotExpression} from here */
	public void append(final IdentExpression r1) {
		if (r1.getText().contains("."))
			throw new IllegalArgumentException("trying to create a Qualident with a dot from a user created Token");
		parts.add(r1);
	}

	public void appendDot(final Token d1) {
//		_syntax.appendDot(d1, parts.size());//parts.add(d1);
	}
	
	private final List<IdentExpression> parts = new ArrayList<IdentExpression>();

	@Override
	public String toString() {
		return asSimpleString();
	}

	@NotNull
	public String asSimpleString() {
		return Helpers.String_join(".", Collections2.transform(parts, new Function<IdentExpression, String>() {
			@Nullable
			@Override
			public String apply(@Nullable IdentExpression input) {
				assert input != null;
				return input.getText();
			}
		}));
//		final StringBuilder sb=new StringBuilder();
//		for (final Token part : parts) {
//			sb.append(part.getText());
//			sb.append('.');
//		}
//		final String s = sb.toString();
//		final String substring = s.substring(0, s.length() - 1);
//		return substring;
	}
	
	@Override
	public ExpressionKind getKind() {
		return ExpressionKind.QIDENT;
	}
	
	@Override
	public void setKind(final ExpressionKind aIncrement) {
		throw new IllegalArgumentException(); // TODO is this right?
	}
	
	@Override
	public IExpression getLeft() {
		return this;
	}
	
	/** Not sure what this should do */
	@Override
	public void setLeft(final IExpression iexpression) {
		throw new IllegalArgumentException(); // TODO is this right?
	}
	
	@Override
	public String repr_() {
		return String.format("Qualident (%s)", toString());
	}
	
	@Override
	public boolean is_simple() {
		return true;  // TODO is this true?
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
	public List<IdentExpression> parts() {
		return parts;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Qualident)) return false;
		final Qualident qualident = (Qualident) o;
		if (qualident.parts.size() != parts.size()) return false;
		for (int i=0; i< parts.size();i++) {
			final IdentExpression ppart = qualident.parts.get(i);
			final IdentExpression part  = parts.get(i);
//			if (!equivalentTokens(ppart.token(), part.token()))
			if (!part.getText().equals(ppart.getText()))
				return false;
//			if (!qualident.parts.contains(token))
//				return false;
		}
//		if (Objects.equals(parts, qualident.parts))
		return true;//Objects.equals(_type, qualident._type);
	}

	private static boolean equivalentTokens(final Token token1, final Token token2) {
		return token2.getText().equals(token1.getText()) &&
			token2.getLine() == token1.getLine() &&
			token2.getColumn() == token1.getColumn() &&
			token2.getType() == token1.getType();
	}

	@Override
	public int hashCode() {
		return Objects.hash(parts, _type);
	}
}

//
//
//
