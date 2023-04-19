/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

/**
 * Created 8/23/21 2:32 AM
 */
public class FunctionHeader {
	private IdentExpression   name;
	private FunctionModifiers mod;
	private FormalArgList     fal;
	private TypeName          ret;

	public IdentExpression getName() {
		return name;
	}

	public void setName(IdentExpression aIdentExpression) {
		name = aIdentExpression;
	}

	public FunctionModifiers getModifier() {
		return mod;
	}

	public void setModifier(FunctionModifiers aModifiers) {
		mod = aModifiers;
	}

	public FormalArgList getFal() {
		return fal;
	}

	public void setFal(FormalArgList aFal) {
		fal = aFal;
	}

	public TypeName getReturnType() {
		return ret;
	}

	public void setReturnType(TypeName aTypeName) {
		ret = aTypeName;
	}
}

//
//
//
