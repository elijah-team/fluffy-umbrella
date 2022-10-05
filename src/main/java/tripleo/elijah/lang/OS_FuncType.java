/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */

/**
 *
 */
package tripleo.elijah.lang;

public class OS_FuncType extends OS_Type {
	private final FunctionDef function_def;

	@Override
	public OS_Element getElement() {
		return function_def;
	}

	public OS_FuncType(final FunctionDef functionDef) {
		super(Type.FUNCTION);
		this.function_def = functionDef;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("<OS_FuncType %s>", function_def);
	}

}

//
//
//
