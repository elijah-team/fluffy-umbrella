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

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/23/20 4:46 AM
 */
public class MatchConditionalBuilder extends ElBuilder {
	private Context _context;
	private IExpression expr;

	@Override
	protected MatchConditional build() {
		final MatchConditional matchConditional = new MatchConditional(_parent, _context);
		matchConditional.expr(expr);

		matchConditional.postConstruct();
		return matchConditional;
	}

	@Override
	protected void setContext(final Context context) {
		_context = context;
	}

	public void expr(final IExpression expr) {
		this.expr = expr;
	}

	public BaseScope normalscope(final IExpression expr) {
		final Normal typeMatch = new Normal(expr);
		parts.add(typeMatch);
		return typeMatch.scope();
	}

	public BaseScope valNormalscope(final IdentExpression i1) {
		final ValNormal typeMatch = new ValNormal(i1);
		parts.add(typeMatch);
		return typeMatch.scope();
	}

	interface FakeMC1 {}

	class TypeMatch implements FakeMC1 {
		private final TypeName typeName;
		private final IdentExpression matchName;
		private BaseScope baseScope;

		public TypeMatch(final IdentExpression i1, final TypeName tn) {
			this.matchName = i1;
			this.typeName = tn;
		}

		public BaseScope scope() {
			final BaseScope baseScope = new BaseScope() {
			};
			this.baseScope = baseScope;
			return baseScope;
		}
	}

	class Normal implements FakeMC1 {

		private final IExpression expr;
		private BaseScope baseScope;

		public Normal(final IExpression expr) {
			this.expr = expr;
		}

		public BaseScope scope() {
			final BaseScope baseScope = new BaseScope() {
			};
			this.baseScope = baseScope;
			return baseScope;
		}
	}

	class ValNormal implements FakeMC1 {

		private final IdentExpression valMatch;
		private BaseScope baseScope;

		public ValNormal(final IdentExpression i1) {
			this.valMatch = i1;
		}
		public BaseScope scope() {
			final BaseScope baseScope = new BaseScope() {
			};
			this.baseScope = baseScope;
			return baseScope;
		}
	}

	List<FakeMC1> parts = new ArrayList<FakeMC1>();

	public BaseScope typeMatchscope(final IdentExpression i1, final TypeName tn) {
		final TypeMatch typeMatch = new TypeMatch(i1, tn);
		parts.add(typeMatch);
		return typeMatch.scope();
	}
}

//
//
//
