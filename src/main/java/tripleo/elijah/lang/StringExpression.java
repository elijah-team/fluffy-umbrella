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
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;

public class StringExpression extends AbstractExpression {

public StringExpression(final Token g) { // TODO List<Token>
set(g.getText());
}

	@Override
	public ExpressionKind getKind() {
		return ExpressionKind.STRING_LITERAL;
	}

	@Override public boolean is_simple() {
		return true;
	}

	@Override
	public  IExpression getLeft() {
//		assert false;
//		return this;
		throw new NotImplementedException();
	}

	@Override
	public void setLeft(final IExpression iexpression) {
		throw new IllegalArgumentException("Should use set()");
	}

	@Override
	public String repr_() {return repr_;}

	public String getText() {
		return Helpers.remove_single_quotes_from_string(repr_); // TODO wont work with triple quoted string
	}

	public void set(final String g) {repr_ = g;}

	@Override
	public String toString() {
		return String.format("<StringExpression %s>", repr_);
	}

	private String repr_;

	OS_Type _type;

	@Override
	public void setType(final OS_Type deducedExpression) {
		_type = deducedExpression;
    }

	@Override
	public OS_Type getType() {
    	return _type;
	}

}

//
//
//
