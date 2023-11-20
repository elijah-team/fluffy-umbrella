package tripleo.elijah.lang.types;

import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang2.BuiltInTypes;

import java.text.MessageFormat;

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

	@Override
	public String asString() {
		return MessageFormat.format("<OS_BuiltinType {0}>", _bit);
	}

	protected boolean _isEqual(final OS_Type aType) {
		return aType.getType() == Type.BUILT_IN && _bit.equals(aType.getBType());
	}
}


