/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import tripleo.elijah.lang.*;

/**
 * Created 12/22/20 10:22 PM
 */
public class TypeAliasBuilder extends ElBuilder {
	private OS_Element _parent;
	private Context _context;
	private Qualident oldElement;
	private IdentExpression newAlias;

	public IdentExpression getIdent() {
		return newAlias;
	}

	public void setIdent(IdentExpression newAlias) {
		this.newAlias = newAlias;
	}

	public Qualident getBecomes() {
		return oldElement;
	}

	public void setBecomes(Qualident oldElement) {
		this.oldElement = oldElement;
	}

	@Override
	public TypeAliasStatement build() {
		TypeAliasStatement typeAliasStatement = new TypeAliasStatement(_parent);
		typeAliasStatement.make(newAlias, oldElement);
		return typeAliasStatement;
	}

	@Override
	public void setContext(Context context) {
		_context = context;
		// TODO this is a very important potential bug
		//  where ident's may not be getting the right context
		//  because of non-use of Parser.cur in the Builders
		newAlias.setContext(context);
	}
}

//
//
//
