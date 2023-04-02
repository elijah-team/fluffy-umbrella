package tripleo.elijah.world.impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.GeneratePipeline;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.stages.garish.GarishClass;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.world.i.LivingClass;

public class DefaultLivingClass implements LivingClass, LivingNode {
	private final ClassStatement _element;
	private final EvaClass    _gc;
	private  GarishClass _garish;

	public DefaultLivingClass(final ClassStatement aElement) {
		_element = aElement;
		_gc      = null;
		_garish  = null;
	}

	public DefaultLivingClass(final @NotNull EvaClass aClass) {
		_element = aClass.getKlass();
		_gc      = aClass;
		_garish  = null;
	}

	@Override
	public ClassStatement getElement() {
		return _element;
	}

	@Override
	public int getCode() {
		return _gc.getCode();
	}

	@Override
	public EvaClass evaNode() {
		return _gc;
	}

	@Override
	public void garish(final GenerateC aGenerateC, final GenerateResult aGr, final GeneratePipeline.GenerateResultSink aResultSink) {
		if (_garish == null) {
			_garish = new GarishClass(this);
		}

		_garish.garish(aGenerateC, aGr, aResultSink);
	}

	public EvaClass gc() {
		return _gc;
	}

	//@Override
	//public void setGarish(final GarishClass aGarishClass) {
	//	_garish = aGarishClass;
	//}
}
