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
 * Created 1/22/21 8:34 AM
 */
public class OS_UnknownType extends OS_Type {
	private OS_Element element;

	public OS_UnknownType(OS_Element aElement) {
		super(Type.UNKNOWN);
		element = aElement;
	}
}

//
//
//
