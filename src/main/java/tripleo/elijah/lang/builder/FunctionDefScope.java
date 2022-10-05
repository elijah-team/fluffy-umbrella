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
 * Created 12/23/20 12:52 AM
 */
public class FunctionDefScope extends BaseFunctionDefScope implements Documentable {

	private List<Precondition> prec_list = new ArrayList<Precondition>();
	private List<Postcondition> postc_list = new ArrayList<Postcondition>();
	private boolean _isAbstract = false;

	@Override
	public void yield(IExpression expr) {
		// TODO add Context and porent
//		add(new StatementWrapper(new YieldExpression(expr), null,null));
		add(new StatementWrapperBuilder(expr)); // TODO this says nothing about a YieldExpression, which is actually a Statement
	}

	// endregion

	public void addPreCondition(Precondition p) {
		prec_list.add(p);
	}

	public void addPostCondition(Postcondition po) {
		postc_list.add(po);
	}

	@Override
	public void statementWrapper(IExpression expr) {
		add(new StatementWrapperBuilder(expr));
	}

	public boolean isAbstract() {
		return _isAbstract;
	}

	public void setAbstract() {
		_isAbstract = true;
	}

	@Override
	public void constructExpression(Qualident q, ExpressionList o) {
		add(new ConstructStatementBuilder(q, o));
	}
}

//
//
//
