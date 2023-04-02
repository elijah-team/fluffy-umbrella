package tripleo.elijah.lang.types;

import tripleo.elijah.lang.OS_Element;


public class OS_UnitType extends __Abstract_OS_Type {

	@Override
	public OS_Element getElement() {
		return null;
	}

	@Override
	public Type getType() {
		return Type.UNIT_TYPE;
	}

	@Override
	public boolean isUnitType() {
		return true;
	}

	@Override
	public String toString() {
		return "<UnitType>";
	}
}
