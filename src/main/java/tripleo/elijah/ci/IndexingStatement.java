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
 *
 * Created 	Apr 15, 2020 at 4:59:21 AM
 * Created 1/8/21 7:19 AM
 */
public class IndexingStatement {

	private Token name;
	private ExpressionList exprs;
	private final CompilerInstructions parent;

	public IndexingStatement(final CompilerInstructions module) {
		this.parent = module;
	}

	public void setName(final Token i1) {
		this.name = i1;
	}

	public void setExprs(final ExpressionList el) {
		this.exprs = el;
	}

}

//
//
//
