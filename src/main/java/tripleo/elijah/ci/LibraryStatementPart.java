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
import tripleo.elijah.lang.IExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 9/6/20 12:06 PM
 */
public class LibraryStatementPart {
	private String name;
	private String dirName;
   	private List<Directive> dirs = null;

	private CompilerInstructions ci;

	public void setName(final Token i1) {
		name = i1.getText();
	}

	public void setDirName(final Token dirName) {
		this.dirName = dirName.getText();
	}

	public String getName() {
		return name;
	}

	public String getDirName() {
		return dirName;
	}

	public void addDirective(final Token token, final IExpression iExpression) {
		if (dirs == null)
			dirs = new ArrayList<Directive>();
		dirs.add(new Directive(token, iExpression));
	}

	public CompilerInstructions getInstructions() {
		return ci;
	}

	public void setInstructions(CompilerInstructions instructions) {
		ci = instructions;
	}

	public class Directive {

		private final IExpression expression;
		private final String name;

		public Directive(final Token token_, final IExpression expression_) {
			name = token_.getText();
			expression = expression_;
		}
	}

}

//
//
//
