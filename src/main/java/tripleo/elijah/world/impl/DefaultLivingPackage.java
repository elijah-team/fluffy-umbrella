package tripleo.elijah.world.impl;

import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.world.i.LivingPackage;

public class DefaultLivingPackage implements LivingPackage {
	private final OS_Package _element;

	public DefaultLivingPackage(final OS_Package aElement) {
		_element = aElement;
	}

	@Override
	public OS_Package getElement() {
		return _element;
	}

	@Override
	public int getCode() {
		return 0;
	}
}
