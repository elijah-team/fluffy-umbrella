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

import tripleo.elijah.lang.Documentable;
import tripleo.elijah.lang.ExpressionList;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.Postcondition;
import tripleo.elijah.lang.Precondition;
import tripleo.elijah.lang.Qualident;

/**
 * Created 12/23/20 12:52 AM
 */
public class FunctionDefScope extends BaseFunctionDefScope implements Documentable {

	private final List<Precondition>  prec_list   = new ArrayList<Precondition>();
	private final List<Postcondition> postc_list  = new ArrayList<Postcondition>();
	private       boolean             _isAbstract = false;

	public void addPostCondition(final Postcondition po) {
		postc_list.add(po);
	}

	// endregion

	public void addPreCondition(final Precondition p) {
		prec_list.add(p);
	}

	@Override
	public void constructExpression(final Qualident q, final ExpressionList o) {
		add(new ConstructStatementBuilder(q, o));
	}

	public boolean isAbstract() {
		return _isAbstract;
	}

	public void setAbstract() {
		_isAbstract = true;
	}

	@Override
	public void statementWrapper(final IExpression expr) {
		add(new StatementWrapperBuilder(expr));
	}

	@Override
	public void yield(final IExpression expr) {
		// TODO add Context and porent
//		add(new StatementWrapper(new YieldExpression(expr), null,null));
		add(new StatementWrapperBuilder(expr)); // TODO this says nothing about a YieldExpression, which is actually a Statement
	}
}

//
//
//
