/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah.lang;

public class BlockStatementImpl implements BlockStatement {

	final private Scope            parent;
	private final NormalTypeName   tn  = new RegularTypeNameImpl(); // FIXME
	private final FormalArgList    fal = new FormalArgListImpl();
	private final StatementClosure scope;

	public BlockStatementImpl(final Scope aParent) {
		parent = aParent;
		scope  = new AbstractStatementClosure(parent);
	}

	public StatementClosure scope() {
		return scope;
	}

	public FormalArgList opfal() {
		return fal;
	}

	public NormalTypeName returnType() {
		return tn;
	}
}
