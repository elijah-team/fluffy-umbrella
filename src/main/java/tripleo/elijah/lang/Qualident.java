/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
/**
 * Created Mar 27, 2019 at 2:24:09 PM
 *
 */
package tripleo.elijah.lang;

import antlr.Token;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Tripleo(sb)
 *
 */
public class Qualident  implements IExpression {

	/** Look into creating a {@link DotExpression} from here */
	public void append(Token r1) {
		parts.add(r1);		
	}
	public void appendDot(Token d1) {
//		parts.add(d1);
	}
	
	private List<Token> parts = new ArrayList<Token>();

	@Override
	public String toString() {
		return asSimpleString();
	}

	@NotNull
	public String asSimpleString() {
		final StringBuilder sb=new StringBuilder();
		for (Token part : parts) {
			sb.append(part.getText());
			sb.append('.');
		}
		final String s = sb.toString();
		final String substring = s.substring(0, s.length() - 1);
		return substring;
	}
	
	@Override
	public ExpressionKind getKind() {
		return ExpressionKind.QIDENT;
	}
	
	@Override
	public void setKind(ExpressionKind aIncrement) {
		throw new IllegalArgumentException(); // TODO is this right?
	}
	
	@Override
	public IExpression getLeft() {
		return this;
	}
	
	/** Not sure what this should do */
	@Override
	public void setLeft(IExpression iexpression) {
		throw new IllegalArgumentException(); // TODO is this right?
	}
	
	@Override
	public String repr_() {
		return String.format("Qualident (%s)", toString());
	}
	
	@Override
	public boolean is_simple() {
		// TODO Auto-generated method stub
		return true;  // TODO is this true?
	}

	OS_Type _type;

	public void setType(OS_Type deducedExpression) {
		_type = deducedExpression;
    }

	public OS_Type getType() {
    	return _type;
	}
	public List<Token> parts() {
		return parts;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Qualident)) return false;
		Qualident qualident = (Qualident) o;
		for (int i=0; i< parts.size();i++) {
			Token ppart = qualident.parts.get(i);
			Token part  = parts.get(i);
			if (!equivalentTokens(ppart, part))
				return false;
//			if (!qualident.parts.contains(token))
//				return false;
		}
//		if (Objects.equals(parts, qualident.parts))
			if (Objects.equals(_type, qualident._type))
				return true;
		return false;
	}

	private static boolean equivalentTokens(Token token1, Token token2) {
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
