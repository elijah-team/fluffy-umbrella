package tripleo.elijah.lang.types;

import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang2.BuiltInTypes;

public class OS_BuiltinType extends __Abstract_OS_Type {
	private final BuiltInTypes _bit;

	@Override
	public BuiltInTypes getBType() {
		return _bit;
	}

	public OS_BuiltinType(final BuiltInTypes aTypes) {
		_bit = (aTypes);
	}

	@Override
	public OS_Element getElement() {
		return null;
	}

	@Override
	public Type getType() {
		return Type.BUILT_IN;
	}
}
