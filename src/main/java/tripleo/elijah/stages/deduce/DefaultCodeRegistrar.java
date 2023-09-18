package tripleo.elijah.stages.deduce;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.util.NotImplementedException;

public class DefaultCodeRegistrar implements ICodeRegistrar {
	public DefaultCodeRegistrar(final Compilation aCompilation) {
	}

	@Override
	public void registerNamespace(final EvaNamespace aNamespace) {
		throw new NotImplementedException();

	}

	@Override
	public void registerClass(final EvaClass aClass) {
		throw new NotImplementedException();

	}

	@Override
	public void registerFunction(final BaseEvaFunction aFunction) {
		throw new NotImplementedException();

	}
}
