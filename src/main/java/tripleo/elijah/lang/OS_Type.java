package tripleo.elijah.lang;

import tripleo.elijah.lang2.BuiltInTypes;

public interface OS_Type {
	enum Type {
		BUILT_IN, USER, USER_CLASS, FUNC_EXPR, UNIT_TYPE, UNKNOWN, ANY, FUNCTION, GENERIC_TYPENAME
	}

	static boolean isConcreteType(final OS_Element element) {
		return element instanceof ClassStatement;
		// enum
		// type
	}

	String asString();

	/*@ requires type_of_type = Type.BUILT_IN; */
	BuiltInTypes getBType();

	ClassStatement getClassOf();

	OS_Element getElement();

	Type getType();

	/*@ requires type_of_type = Type.USER; */
	TypeName getTypeName();

	boolean isEqual(OS_Type aType);

	boolean isUnitType();

	OS_Type resolve(Context ctx);

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	String toString();

}
