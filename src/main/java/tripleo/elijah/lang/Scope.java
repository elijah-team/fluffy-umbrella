/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

public interface Scope extends Documentable {

	void add(StatementItem aItem);

	void statementWrapper(IExpression aExpr);

	StatementClosure statementClosure();

	BlockStatement blockStatement();

	TypeAliasStatement typeAlias();

	InvariantStatement invariantStatement();

	OS_Element getParent();

	OS_Element getElement();
}

//
//
//
