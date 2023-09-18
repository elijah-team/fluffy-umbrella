/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
/**
 * Created Apr 1, 2019 at 3:21:26 PM
 */
package tripleo.elijah.lang;

import antlr.Token;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Locatable;
import tripleo.elijah.lang.nextgen.names.i.EN_Name;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;

import java.io.File;

/**
 * @author Tripleo(sb)
 *
 */
public class IdentExpression implements IExpression, OS_Element, Resolvable, Locatable {

	@Contract("_ -> new")
	public static @NotNull IdentExpression forString(final String string) {
		return new IdentExpression(Helpers.makeToken(string));
	}
	public final  Attached   _a;
	private final Token      text;
	OS_Type _type;

	private       OS_Element _resolvedElement;

	EN_Name _name = EN_Name.create("dsmamjkdnasjdsajkldnsajkl");

	public IdentExpression(final Token r1) {
		this.text = r1;
		this._a   = new Attached();
	}

	public IdentExpression(final Token r1, final Context cur) {
		this.text = r1;
		this._a   = new Attached();
		setContext(cur);
	}

	@Override
	public int getColumn() {
		return token().getColumn();
	}

	@Override
	public int getColumnEnd() {
		return token().getColumn();
	}

	@Override
	public Context getContext() {
		return _a.getContext();
	}

	@Override
	public File getFile() {
		final String filename = token().getFilename();
		if (filename == null)
			return null;
		return new File(filename);
	}

	@Override
	public ExpressionKind getKind() {
		return ExpressionKind.IDENT;
	}

	@Override
	public IExpression getLeft() {
		return this;
	}

	@Override
	public int getLine() {
		return token().getLine();
	}

	@Override
	public int getLineEnd() {
		return token().getLine();
	}

	public EN_Name getName() {
		return _name;
	}

	@Override
	public OS_Element getParent() {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
//		return null;
	}

	@Override
	public OS_Element getResolvedElement() {
		return _resolvedElement;
	}

	public String getText() {
		return text.getText();
	}

	@Override
	public OS_Type getType() {
		return _type;
	}

	@Override
	public boolean hasResolvedElement() {
		return _resolvedElement != null;
	}

	@Override
	public boolean is_simple() {
		return true;
	}

	@Override
	public String repr_() {
		return String.format("IdentExpression(%s %d)", text.getText(), _a.getCode());
	}

	public void setContext(final Context cur) {
		_a.setContext(cur);
	}

	// region Locatable

	@Override
	public void setKind(final ExpressionKind aIncrement) {
		// log and ignore
		tripleo.elijah.util.Stupidity.println_err2("Trying to set ExpressionType of IdentExpression to " + aIncrement.toString());
	}

	@Override
	public void setLeft(final @NotNull IExpression iexpression) {
//		if (iexpression instanceof IdentExpression) {
//			text = ((IdentExpression) iexpression).text;
//		} else {
//			// NOTE was tripleo.elijah.util.Stupidity.println_err2
		throw new IllegalArgumentException("Trying to set left-side of IdentExpression to " + iexpression);
//		}
	}

	@Override
	public void setResolvedElement(final OS_Element element) {
		_resolvedElement = element;
	}

	@Override
	public void setType(final OS_Type deducedExpression) {
		_type = deducedExpression;
	}

	public Token token() {
		return text;
	}

	/**
	 * same as getText()
	 */
	@Override
	public String toString() {
		return getText();
	}

	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitIdentExpression(this);
	}

	// endregion
}

//
//
//
