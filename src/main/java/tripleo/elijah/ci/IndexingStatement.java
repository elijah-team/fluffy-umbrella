/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.ci;

import antlr.Token;
import tripleo.elijah.lang.ExpressionList;

/**
 * @author Tripleo
 * <p>
 * Created 	Apr 15, 2020 at 4:59:21 AM
 * Created 1/8/21 7:19 AM
 */
public class IndexingStatement {

	private final CompilerInstructions parent;
	private       Token                name;
	private       ExpressionList       exprs;

	public IndexingStatement(final CompilerInstructions module) {
		this.parent = module;
	}

	public void setExprs(final ExpressionList el) {
		this.exprs = el;
	}

	public void setName(final Token i1) {
		this.name = i1;
	}

}

//
//
//
