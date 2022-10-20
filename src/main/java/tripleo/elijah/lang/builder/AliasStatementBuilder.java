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
 * Created 12/23/20 4:38 AM
 */
public class AliasStatementBuilder extends ElBuilder {
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
	public AliasStatement build() {
		AliasStatement aliasStatement = new AliasStatement(_parent);
		aliasStatement.setName(newAlias);
		aliasStatement.setExpression(oldElement);
		// no setContext!!
		return aliasStatement;
	}

	@Override
	protected void setContext(Context context) {
		_context = context;
	}

	public void setName(IdentExpression i1) {
		newAlias = i1;
	}

	public void setExpression(Qualident xy) {
		oldElement = xy;
	}
}

//
//
//
