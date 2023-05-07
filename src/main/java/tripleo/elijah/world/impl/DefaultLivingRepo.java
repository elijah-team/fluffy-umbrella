package tripleo.elijah.world.impl;

import tripleo.elijah.entrypoints.MainClassEntryPoint;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.world.i.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultLivingRepo implements LivingRepo {
	private final Map<String, OS_Package> _packages     = new HashMap<String, OS_Package>();
	List<LivingNode> repo = new ArrayList<>();
	private       int                     _packageCode  = 1;
	private       int                     _classCode    = 101;
	private       int                     _functionCode = 1001;

	@Override
	public OS_Package makePackage(final Qualident pkg_name) {
		final String pkg_name_s = pkg_name.toString();
		if (!isPackage(pkg_name_s)) {
			final OS_Package newPackage = new OS_Package(pkg_name, nextPackageCode());
			_packages.put(pkg_name_s, newPackage);
			return newPackage;
		} else
			return _packages.get(pkg_name_s);
	}

	public boolean isPackage(final String pkg) {
		return _packages.containsKey(pkg);
	}

	private int nextPackageCode() {
		return _packageCode++;
	}

	@Override
	public LivingClass addClass(final ClassStatement cs) {
		return null;
	}

	@Override
	public LivingFunction addFunction(final BaseFunctionDef fd) {
		return null;
	}

	@Override
	public LivingPackage addPackage(final OS_Package pk) {
		return null;
	}

	@Override
	public OS_Package getPackage(final String aPackageName) {
		return _packages.get(aPackageName);
	}

	@Override
	public DefaultLivingFunction addFunction(final BaseEvaFunction aFunction, final Add addFlag) {
		switch (addFlag) {
		case NONE -> {
			aFunction.setCode(nextFunctionCode());
		}
		case MAIN_FUNCTION -> {
			if (aFunction.getFD() instanceof FunctionDef &&
					MainClassEntryPoint.is_main_function_with_no_args((FunctionDef) aFunction.getFD())) {
				aFunction.setCode(1000);
				//compilation.notifyFunction(code, aFunction);
			} else {
				throw new IllegalArgumentException("not a main function");
			}
		}
		case MAIN_CLASS -> {
			throw new IllegalArgumentException("not a class");
		}
		}

		final DefaultLivingFunction living = new DefaultLivingFunction(aFunction);
		aFunction._living = living;

		return living;
	}

	public int nextFunctionCode() {
		return _functionCode++;
	}

	@Override
	public DefaultLivingClass addClass(final EvaClass aClass, final Add addFlag) {
		switch (addFlag) {
		case NONE -> {
			aClass.setCode(nextClassCode());
		}
		case MAIN_FUNCTION -> {
			throw new IllegalArgumentException("not a function");
		}
		case MAIN_CLASS -> {
			final boolean isMainClass = MainClassEntryPoint.isMainClass(aClass.getKlass());
			if (!isMainClass) {
				throw new IllegalArgumentException("not a main class");
			}
			aClass.setCode(100);
		}
		}

		final DefaultLivingClass living = new DefaultLivingClass(aClass);
		aClass._living = living;

		repo.add(living);

		return living;
	}

	public int nextClassCode() {
		return _classCode++;
	}

	@Override
	public void addNamespace(final EvaNamespace aNamespace, final Add aNone) {
		throw new NotImplementedException();
	}

	@Override
	public LivingClass getClass(final EvaClass aEvaClass) {
		for (LivingNode livingNode : repo) {
			if (livingNode instanceof LivingClass) {
				final LivingClass livingClass = (LivingClass) livingNode;
				if (livingClass.evaNode().equals(aEvaClass))
					return livingClass;
			}
		}

		final DefaultLivingClass living = new DefaultLivingClass(aEvaClass);
		//klass._living = living;

		repo.add(living);

		return living;
	}
}
