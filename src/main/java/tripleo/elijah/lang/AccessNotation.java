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
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijjah.ElijjahTokenTypes;

/**
 * Created 9/22/20 1:39 AM
 */
// TODO Does this need to be Element?
public class AccessNotation implements OS_Element {
	private Token category;
	private Token shorthand;
	private TypeNameList tnl;

	public void setCategory(final Token category) {
		if (category == null) return;
		assert category.getType() == ElijjahTokenTypes.STRING_LITERAL;
		this.category = category;
	}

	public void setShortHand(final Token shorthand) {
		if (shorthand == null) return;
		assert shorthand.getType() == ElijjahTokenTypes.IDENT;
		this.shorthand = shorthand;
	}

	public void setTypeNames(final TypeNameList tnl) {
		this.tnl = tnl;
	}

	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitAccessNotation(this);
	}

	@Override
	public OS_Element getParent() {
		throw new NotImplementedException();
	}

	@Override
	public Context getContext() {
		throw new NotImplementedException();
	}

	public Token getCategory() {
		return category;
	}
}

//
//
//
