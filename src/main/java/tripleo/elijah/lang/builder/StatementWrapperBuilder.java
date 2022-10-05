/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.StatementWrapper;

/**
 * Created 12/23/20 1:04 AM
 */
public class StatementWrapperBuilder extends ElBuilder {
	private final IExpression expr;
	private Context _context;

	public StatementWrapperBuilder(IExpression expr) {
		this.expr = expr;
	}

	@Override
	protected OS_Element build() {
		return new StatementWrapper(expr, _context, _parent);
	}

	@Override
	protected void setContext(Context context) {
		_context = context;
	}
}

//
//
//
