package tripleo.elijah.world.i;

import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.world.impl.DefaultLivingClass;
import tripleo.elijah.world.impl.DefaultLivingFunction;

public interface LivingRepo {
	LivingClass addClass(ClassStatement cs);

	LivingFunction addFunction(BaseFunctionDef fd);

	LivingPackage addPackage(OS_Package pk);

	OS_Package getPackage(String aPackageName);

	DefaultLivingFunction addFunction(BaseEvaFunction aFunction, Add aMainFunction);

	//DefaultLivingClass addClass(EvaClass aClass, Add aMainClass);

	DefaultLivingClass addClass(EvaClass aClass, Add addFlag);

	void addNamespace(EvaNamespace aNamespace, Add aNone);

	LivingClass getClass(EvaClass aEvaClass);

	OS_Package makePackage(Qualident aPkgName);

	enum Add {NONE, MAIN_FUNCTION, MAIN_CLASS}
}
