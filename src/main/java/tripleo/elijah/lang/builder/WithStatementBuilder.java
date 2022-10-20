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
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.lang.WithStatement;

/**
 * Created 12/23/20 4:57 AM
 */
public class WithStatementBuilder extends ElBuilder {
	private Context _context;
	private VariableSequenceBuilder _sb = new VariableSequenceBuilder();
	private WithStatementScope _scope = new WithStatementScope();

	@Override
	protected WithStatement build() {
		WithStatement withStatement = new WithStatement(_parent);
		for (VariableSequenceBuilder.Triple triple : _sb.triples) {
			VariableStatement vs = withStatement.nextVarStmt();
			vs.setName(triple._name);
			vs.initial(triple._initial);
			vs.setTypeName(triple._tn);
		}
		for (ElBuilder builder : _scope.items()) {
			OS_Element built;
			builder.setParent(_parent);
			builder.setContext(_context);
			built = builder.build();
			withStatement.add(built);
		}
		withStatement.postConstruct();
		return withStatement;
	}

	@Override
	protected void setContext(Context context) {
		_context = context;
	}

	public VariableSequenceBuilder sb() {
		return _sb;
	}

	public BaseScope scope() {
		return _scope;
	}
}

//
//
//
