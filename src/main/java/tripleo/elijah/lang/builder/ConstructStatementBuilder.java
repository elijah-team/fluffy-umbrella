/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import tripleo.elijah.lang.ConstructStatement;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.ExpressionList;
import tripleo.elijah.lang.Qualident;

/**
 * Created 12/23/20 2:35 AM
 */
public class ConstructStatementBuilder extends ElBuilder {
	private final Qualident q;
	private final ExpressionList o;
	private Context _context;
	private String constructorName = null;

	public ConstructStatementBuilder(final Qualident q, final ExpressionList o) {
		super();
		this.q = q;
		this.o = o;
	}

	@Override
	protected ConstructStatement build() {
		return new ConstructStatement(_parent, _context, q, constructorName, o);
	}

	@Override
	protected void setContext(final Context context) {
		_context = context;
	}

	public void setConstructorName(final String aConstructorName) {
		constructorName = aConstructorName;
	}
}

//
//
//
