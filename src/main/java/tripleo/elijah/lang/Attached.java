/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import tripleo.elijah.gen.Node;

/*
 * Created on 5/19/2019 at 02:09
 *
 * $Id$
 *
 */
public class Attached {
	int _code;
	Context _context;
	Node _node;
	
	public Attached(final Context aContext) {
		_context = aContext;
	}

	public Attached() {
		
		// TODO Auto-generated constructor stub
	}

	public int getCode() {
		return _code;
	}
	
	public void setCode(final int aCode) {
		_code = aCode;
	}
	
	public Context getContext() {
		return _context;
	}
	
	public void setContext(final Context aContext) {
		_context = aContext;
	}
	
	public Node getNode() {
		return _node;
	}
	
	public void setNode(final Node aNode) {
		_node = aNode;
	}
	
}

//
//
//
