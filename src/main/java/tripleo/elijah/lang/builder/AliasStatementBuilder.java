/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import tripleo.elijah.lang.AliasStatement;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.Qualident;

/**
 * Created 12/23/20 4:38 AM
 */
public class AliasStatementBuilder extends ElBuilder {
	private OS_Element      _parent;
	private Context         _context;
	private Qualident       oldElement;
	private IdentExpression newAlias;

	@Override
	public AliasStatement build() {
		final AliasStatement aliasStatement = new AliasStatement(_parent);
		aliasStatement.setName(newAlias);
		aliasStatement.setExpression(oldElement);
		// no setContext!!
		return aliasStatement;
	}

	public Qualident getBecomes() {
		return oldElement;
	}

	public IdentExpression getIdent() {
		return newAlias;
	}

	public void setBecomes(final Qualident oldElement) {
		this.oldElement = oldElement;
	}

	@Override
	protected void setContext(final Context context) {
		_context = context;
	}

	public void setExpression(final Qualident xy) {
		oldElement = xy;
	}

	public void setIdent(final IdentExpression newAlias) {
		this.newAlias = newAlias;
	}

	public void setName(final IdentExpression i1) {
		newAlias = i1;
	}
}

//
//
//
