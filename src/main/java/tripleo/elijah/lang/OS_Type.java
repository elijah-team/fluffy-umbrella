package tripleo.elijah.lang;

import tripleo.elijah.lang2.BuiltInTypes;

public interface OS_Type {
	static boolean isConcreteType(final OS_Element element) {
		return element instanceof ClassStatement;
		// enum
		// type
	}

	ClassStatement getClassOf();

	OS_Element getElement();

	OS_Type resolve(Context ctx);

	boolean isUnitType();

	Type getType();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	String toString();

	/*@ requires type_of_type = Type.BUILT_IN; */
	BuiltInTypes getBType();

	/*@ requires type_of_type = Type.USER; */
	TypeName getTypeName();

	boolean isEqual(OS_Type aType);

	String asString();

	enum Type {
		BUILT_IN, USER, USER_CLASS, FUNC_EXPR, UNIT_TYPE, UNKNOWN, ANY, FUNCTION, GENERIC_TYPENAME
	}

}
