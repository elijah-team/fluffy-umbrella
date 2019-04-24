/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
/*
 * Created on Sep 2, 2005 2:28:42 PM
 * 
 * $Id$
 *
 */
package tripleo.elijah.gen.nodes;

import java.util.List;

import antlr.CommonToken;
import antlr.Token;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.gen.Node;
import tripleo.elijah.gen.TypeRef;
import tripleo.elijah.lang.*;
import tripleo.elijah.util.NotImplementedException;

public class ExpressionNodeBuilder {

	public static OS_Ident ident(String string) {
		// TODO Auto-generated method stub
		return new OS_Ident(string);
	}

	public static OS_Integer integer(int i) {
		// TODO Auto-generated method stub
		return new OS_Integer(i);
	}

	public static VariableReferenceNode3 varref(String string, Node container, TypeRef typeRef) {
 		return new VariableReferenceNode3(string, container, typeRef);
	}

	public static IExpression binex(VariableReference left, ExpressionOperators middle, IExpression right) {
		// TODO Auto-generated method stub
		ExpressionType middle1;
		switch (middle) {
		case OP_MINUS: middle1 = ExpressionType.SUBTRACTION; break;
		case OP_MULT:  middle1 = ExpressionType.MULTIPLY; break;
		default: throw new NotImplementedException();
		}
		return new AbstractBinaryExpression(left, middle1, right);
	}

	public static IExpression fncall(String string, List<LocalAgnTmpNode> of) { // todo wrong
		// TODO Auto-generated method stub
		final ProcedureCallExpression pce = new ProcedureCallExpression();
		final Qualident xyz = new Qualident();
		final Token t = new CommonToken();
		t.setText(string);
		xyz.append(t);
		pce.identifier(xyz);
//		pce.setArgs(of);
		return pce;
	}

	@NotNull
	@Contract("_, _, _ -> new")
	public static IExpression binex(VariableReference left, ExpressionOperators middle, TmpSSACtxNode right) { // todo wrong again
		// TODO Auto-generated method stub
		ExpressionType middle1;
		switch (middle) {
		case OP_MINUS: middle1 = ExpressionType.SUBTRACTION; break;
		case OP_MULT:  middle1 = ExpressionType.MULTIPLY; break;
		default: throw new NotImplementedException();
		}
		return new AbstractBinaryExpression(left, middle1, new StringExpression(right.text())); // TODO !!!
	}
	
	public static IExpression binex(VariableReferenceNode3 n, ExpressionOperators opMinus, OS_Integer integer) {
		return null;
	}
	
	public static IExpression binex(VariableReferenceNode3 varref, ExpressionOperators operators, TmpSSACtxNode node) {
		return null;
	}
}

//
//
//
