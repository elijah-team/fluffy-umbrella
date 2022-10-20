/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang2;

/**
 * @author Tripleo
 *
 * Created 	Mar 27, 2020 at 2:08:59 AM
 */
public enum BuiltInTypes {
	SystemInteger(80),
	Boolean(79),
	Unit(0),
	String_(8),
	SystemCharacter(9);

	final int _code;
	
	BuiltInTypes(final int aCode) {
		_code = aCode;
	}
	
	public int getCode() {
		return _code;
	}

	public static boolean isBooleanText(String name) {
		return name.equals("true") || name.equals("false")
			|| name.equals("True") || name.equals("False");
	}
}

//
//
//
