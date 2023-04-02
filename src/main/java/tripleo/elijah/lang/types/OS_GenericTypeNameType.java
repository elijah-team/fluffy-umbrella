/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.types;

import tripleo.elijah.contexts.ClassContext;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.TypeName;


/**
 * Created 7/8/21 6:00 AM
 */
public class OS_GenericTypeNameType extends __Abstract_OS_Type {

	private final ClassContext.OS_TypeNameElement genericTypename;

	public OS_GenericTypeNameType(final ClassContext.OS_TypeNameElement aGenericTypename) {
		genericTypename = aGenericTypename;
	}

	@Override
	public OS_Element getElement() {
		return genericTypename;
	}

	@Override
	public Type getType() {
		return Type.GENERIC_TYPENAME;
	}

	public TypeName getRealTypeName() {
		return genericTypename.getTypeName();
	}
}

//
//
//
