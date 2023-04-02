/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.types;


import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.lang2.BuiltInTypes;

/**
 * Created 4/25/21 4:43 AM
 */
public class OS_AnyType extends __Abstract_OS_Type {
	public OS_AnyType() {
	}

	@Override
	public ClassStatement getClassOf() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OS_Type resolve(final Context ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUnitType() {
		return false;
	}

	@Override
	public BuiltInTypes getBType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeName getTypeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OS_Element getElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getType() {
		return Type.ANY;
	}
}

//
//
//
