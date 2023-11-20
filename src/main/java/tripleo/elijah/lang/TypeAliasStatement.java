/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
/**
 * Created Apr 2, 2019 at 11:04:35 AM
 *
 */
package tripleo.elijah.lang;

import tripleo.elijah.gen.ICodeGen;
import tripleo.elijah.util.NotImplementedException;

/**
 * @author Tripleo(sb)
 *
 */
public class TypeAliasStatement implements OS_Element {

	private final OS_Element parent;
	private IdentExpression x;
	private Qualident y;

	public TypeAliasStatement(final OS_Element aParent) {
		this.parent = aParent;
	}

    public void make(final IdentExpression x, final Qualident y) {
		this.x=x;
		this.y=y;
	}
	
	public void setIdent(final IdentExpression aToken) {
		x = aToken;
	}
	
	public void setBecomes(final Qualident qq) {
		y=qq;
	}

	@Override
	public void visitGen(final ICodeGen visit) {
		visit.visitTypeAlias(this);
	}

	@Override
	public OS_Element getParent() {
		return parent;
	}

	@Override
	public Context getContext() {
		throw new NotImplementedException();
	}
}

//
//
//
