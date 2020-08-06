/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */

/*
 * Created on Sep 2, 2005 2:08:03 PM
 * 
 * $Id$
 * 
 */
package tripleo.elijah.lang;

public interface IExpression {

	ExpressionKind getKind();

	void setKind(ExpressionKind aKind);

	IExpression getLeft();

	void setLeft(IExpression iexpression);

	@Deprecated String repr_();

	IExpression UNASSIGNED = new BasicBinaryExpression() {
	};

//	default boolean is_simple() {
//		switch(getType()) {
//		case STRING_LITERAL:
//			return true;
//		default:
//			return false;
//		}
//	}

	boolean is_simple();

	void setType(OS_Type deducedExpression);
    OS_Type getType();
}

//
//
//
