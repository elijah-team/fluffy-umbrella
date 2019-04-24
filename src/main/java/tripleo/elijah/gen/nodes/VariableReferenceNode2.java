/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.gen.nodes;

import antlr.CommonToken;
import antlr.Token;
import tripleo.elijah.gen.Node;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.util.NotImplementedException;

public class VariableReferenceNode2 extends ExpressionNode {
	private  String _declared;
	private final String _type;
	private  boolean _perm;
	
//	private final CompilerContext _cctx;
//	private final VariableReference _varref;
	
//	/**
//	 * Do not call this
//	 * @param string
//	 * @param container
//	 */
//	public VariableReferenceNode2(String string, Node container) {
//		this._declared = string;
//		this._perm =
//		throw new IllegalStateException();
//	}
	
	public VariableReferenceNode2(String declared, String t, boolean b) {
		super();
		final Token ct = new CommonToken();
		ct.setText(declared);
		this._declared = declared;
		this._perm = b;
		this._type = t;
		setText(new IdentExpression(ct)); // TODO
	}
	
	private void setText(IdentExpression identExpression) {
		//NotImplementedException.raise();
		_declared = identExpression.getText();
		_perm = true;
	}
	
//	public VariableReferenceNode(CompilerContext cctx, VariableReference varref) {
//		NotImplementedException.raise();
//		this._cctx=cctx;
//		this._varref=varref;
//	}
	
	public TypeNameNode getType() {
		NotImplementedException.raise();
		return null;
	}
	
	public String genText() {
		if (_perm) return _declared;
		NotImplementedException.raise();
		return "vtn"; // TODO hardcoded
	}
}

//
//
//
