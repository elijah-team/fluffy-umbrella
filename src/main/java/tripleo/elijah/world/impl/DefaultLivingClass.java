package tripleo.elijah.world.impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.world.i.LivingClass;

public class DefaultLivingClass implements LivingClass {
	private final ClassStatement _element;
	private final EvaClass _gc;

	public DefaultLivingClass(final ClassStatement aElement) {
		_element = aElement;
		_gc      = null;
	}

	public DefaultLivingClass(final @NotNull EvaClass aClass) {
		_element = aClass.getKlass();
		_gc      = aClass;
	}

	@Override
	public ClassStatement getElement() {
		return _element;
	}

	@Override
	public int getCode() {
		return _gc.getCode();
	}
}
