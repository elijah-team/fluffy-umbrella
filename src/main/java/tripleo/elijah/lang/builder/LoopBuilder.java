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
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.Loop;
import tripleo.elijah.lang.LoopTypes;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.Scope3;

/**
 * Created 12/22/20 11:50 PM
 */
public class LoopBuilder extends ElBuilder {
	private final LoopScope       _scope = new LoopScope();
	private       LoopTypes       _type;
	private       IExpression     _frompart;
	private       IExpression     _topart;
	private       IdentExpression _iterName;
	private       Context         _context;
	private       IExpression     expr;

	@Override
	public Loop build() {
		final Loop loop = new Loop(_parent);
		loop.type(_type);
		loop.frompart(_frompart);
		loop.topart(_topart);
		loop.iterName(_iterName);
		loop.expr(expr);
		final Scope3 scope = new Scope3(loop);
		for (final ElBuilder builder : _scope.items()) {
			builder.setParent(loop);
			builder.setContext(loop.getContext());
			final OS_Element built = builder.build();
			scope.add(built);
		}
		loop.scope(scope);
		return loop;
	}

	public void expr(final IExpression expr) {
		this.expr = expr;
	}

	public void frompart(final IExpression expr) {
		_frompart = expr;
	}

	public void iterName(final IdentExpression i1) {
		_iterName = i1;
	}

	public LoopScope scope() {
		return _scope;
	}

	@Override
	protected void setContext(final Context context) {
		_context = context;
	}

	public void topart(final IExpression expr) {
		_topart = expr;
	}

	public void type(final LoopTypes type) {
		_type = type;
	}
}

//
//
//
