/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */

package tripleo.elijah.lang.builder;

import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.IdentExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/23/20 2:54 AM
 */
public abstract class ClassOrNamespaceScope extends BaseScope {

	public void addProp(PropertyStatementBuilder ps) {
		add(ps);
	}

	static class IspPart {
		final IdentExpression ident;
		final IExpression expr;

		public IspPart(IdentExpression i1, IExpression expr) {
			this.ident = i1;
			this.expr = expr;
		}
	}

	List<IspPart> isps = new ArrayList<IspPart>();

	public void addInvariantStatementPart(IdentExpression i1, IExpression expr) {
		isps.add(new IspPart(i1, expr));
	}

}

//
//
//
