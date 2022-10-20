/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import antlr.Token;
import tripleo.elijah.lang.Documentable;
import tripleo.elijah.lang.ExpressionList;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/23/20 12:49 AM
 */
public class BaseScope implements Documentable {
	protected final List<ElBuilder> bs = new ArrayList<ElBuilder>();

	public void return_expression(IExpression expr) {
		throw new NotImplementedException();
	}

	public void add(ElBuilder b) {
		bs.add(b);
	}

	private final List<Token> _docstrings = new ArrayList<Token>();

	@Override
	public void addDocString(Token s1) {
		_docstrings.add(s1);
	}

	public void continue_statement() {
		throw new NotImplementedException();
	}

	public void break_statement() {
		throw new NotImplementedException();
	}

	public void statementWrapper(IExpression expr) {
		throw new NotImplementedException();
	}

	public Iterable<ElBuilder> items() {
		return bs;
	}

	public void constructExpression(Qualident q, ExpressionList o) {
		throw new NotImplementedException();
	}

	public void yield(IExpression expr) {
		throw new NotImplementedException();
	}
}

//
//
//
