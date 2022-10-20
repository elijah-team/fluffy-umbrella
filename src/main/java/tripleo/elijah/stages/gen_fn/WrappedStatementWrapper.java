/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import tripleo.elijah.lang.*;

/**
 * Created 9/18/21 4:03 AM
 */
public class WrappedStatementWrapper extends StatementWrapper {
	private final Wrapped wrapped;
	private final VariableStatement vs;

	public WrappedStatementWrapper(final IExpression aExpression, final Context aContext, final OS_Element aParent, final VariableStatement aVs) {
		super(aExpression, aContext, aParent);
		vs = aVs;
		wrapped = new Wrapped(aVs, aExpression);
	}

	public Wrapped getWrapped() {
		return wrapped;
	}

	public VariableStatement getVariableStatement() {
		return vs;
	}

	class Wrapped extends AbstractExpression {

		private final VariableStatement variableStatement;
		private final IExpression expression;

		public Wrapped(final VariableStatement aVariableStatement, final IExpression aExpression) {
			variableStatement = aVariableStatement;
			expression = aExpression;
		}

		@Override
		public boolean is_simple() {
			return expression.is_simple();
		}

		@Override
		public void setType(final OS_Type deducedExpression) {

		}

		@Override
		public OS_Type getType() {
			return null;
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
