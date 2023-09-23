/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import java.util.ArrayList;
import java.util.List;

import tripleo.elijah.lang.CaseConditional;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.Scope3;

/**
 * Created 12/23/20 5:50 AM
 */
public class CaseConditionalBuilder extends ElBuilder {
	class Part {
		IExpression expr;
		BaseScope   scope;

		public Part(final IExpression expr, final BaseScope baseScope) {
			this.expr  = expr;
			this.scope = baseScope;
		}
	}
	private final List<Part>  parts = new ArrayList<Part>();
	private       Context     _context;
	private       IExpression expr;

	private       BaseScope   baseScope;

	@Override
	protected CaseConditional build() {
		final CaseConditional caseConditional = new CaseConditional(_parent, _context);
		caseConditional.expr(expr);
		for (final Part part : parts) {
			final Scope3 scope3 = new Scope3(caseConditional);
			for (final ElBuilder item : part.scope.items()) {
				item.setParent(caseConditional);
				item.setContext(caseConditional.getContext());
				final OS_Element built = item.build();
				scope3.add(built);
			}
//			Scope sc = caseConditional.scope(part.expr);
			caseConditional.addScopeFor(part.expr, scope3);
		}
		caseConditional.postConstruct();
		return caseConditional;
	}

	public void expr(final IExpression expr) {
		this.expr = expr;
	}

	public BaseScope scope(final IExpression expr) {
		final BaseScope baseScope = new BaseScope() {
		};
		final Part p = new Part(expr, baseScope);
		parts.add(p);
		return baseScope;
	}

	@Override
	protected void setContext(final Context context) {
		_context = context;
	}
}

//
//
//
