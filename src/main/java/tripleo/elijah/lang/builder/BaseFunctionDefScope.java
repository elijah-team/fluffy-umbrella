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

/**
 * Created 12/23/20 3:57 AM
 */
public abstract class BaseFunctionDefScope extends BaseScope {
	@Override
	public abstract void statementWrapper(IExpression expr);

	@Override
	public abstract void yield(IExpression expr);

}

//
//
//
