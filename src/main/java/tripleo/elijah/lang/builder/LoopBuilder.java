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
 * Created 12/22/20 11:50 PM
 */
public class LoopBuilder extends ElBuilder {
	private LoopTypes _type;
	private IExpression _frompart;
	private IExpression _topart;
	private IdentExpression _iterName;
	private LoopScope _scope = new LoopScope();
	private Context _context;
	private IExpression expr;

	public void type(LoopTypes type) {
		_type = type;
	}

	public void frompart(IExpression expr) {
		_frompart = expr;
	}

	public void topart(IExpression expr) {
		_topart = expr;
	}

	public void iterName(IdentExpression i1) {
		_iterName = i1;
	}

	@Override
	public Loop build() {
		Loop loop = new Loop(_parent);
		loop.type(_type);
		loop.frompart(_frompart);
		loop.topart(_topart);
		loop.iterName(_iterName);
		loop.expr(expr);
		Scope3 scope = new Scope3(loop);
		for (ElBuilder builder : _scope.items()) {
			builder.setParent(loop);
			builder.setContext(loop.getContext());
			OS_Element built = builder.build();
			scope.add(built);
		}
		loop.scope(scope);
		return loop;
	}

	@Override
	protected void setContext(Context context) {
		_context = context;
	}

	public LoopScope scope() {
		return _scope;
	}

	public void expr(IExpression expr) {
		this.expr = expr;
	}
}

//
//
//
