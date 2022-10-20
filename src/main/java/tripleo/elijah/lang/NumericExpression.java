/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
/*
 * Created on Sep 1, 2005 8:16:32 PM
 * 
 * $Id$
 *
 */
package tripleo.elijah.lang;

import antlr.Token;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Locatable;
import tripleo.elijah.util.NotImplementedException;

import java.io.File;

public class NumericExpression implements IExpression, Locatable {

	int carrier;
	private Token n;

	public NumericExpression(final int aCarrier) {
		carrier = aCarrier;
	}

	public NumericExpression(final @NotNull Token n) {
		this.n = n;
		carrier = Integer.parseInt(n.getText());
	}

	@Override
	public IExpression getLeft() {
		return this;
	}

	@Override
	public void setLeft(final IExpression aLeft) {
		throw new NotImplementedException(); // TODO
	}

	// region kind

	@Override  // IExpression
	public ExpressionKind getKind() {
		return ExpressionKind.NUMERIC; // TODO
	}

	@Override  // IExpression
	public void setKind(final ExpressionKind aType) {
		// log and ignore
		System.err.println("Trying to set ExpressionType of NumericExpression to "+aType.toString());
	}

	// endregion

	// region representation

	@Override
	public String repr_() {
		return toString();
	}

	@Override
	public String toString() {
		return String.format("NumericExpression (%d)", carrier);
	}

	//endregion

	@Override
	public boolean is_simple() {
		return true;
	}

	// region type

	OS_Type _type;

	@Override  // IExpression
	public void setType(final OS_Type deducedExpression) {
		_type = deducedExpression;
    }

	@Override  // IExpression
	public OS_Type getType() {
    	return _type;
	}

	// endregion

	public int getValue() {
		return carrier;
	}

	// region Locatable

	private Token token() {
		return n;
	}

	@Override
	public int getLine() {
		if (token() != null)
			return token().getLine();
		return 0;
	}

	@Override
	public int getColumn() {
		if (token() != null)
			return token().getColumn();
		return 0;
	}

	@Override
	public int getLineEnd() {
		if (token() != null)
			return token().getLine();
		return 0;
	}

	@Override
	public int getColumnEnd() {
		if (token() != null)
			return token().getColumn();
		return 0;
	}

	@Override
	public File getFile() {
		if (token() != null) {
			String filename = token().getFilename();
			if (filename != null)
				return new File(filename);
		}
		return null;
	}

	// endregion
}

//
//
//
