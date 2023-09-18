package tripleo.elijah.world.i;

import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.util.CompletableProcess;
import tripleo.elijah.world.impl.DefaultLivingClass;
import tripleo.elijah.world.impl.DefaultLivingFunction;

import java.util.Collection;

public interface LivingRepo {
	LivingClass addClass(ClassStatement cs);

	LivingFunction addFunction(BaseFunctionDef fd);

	LivingPackage addPackage(OS_Package pk);

	OS_Package getPackage(String aPackageName);

	DefaultLivingFunction addFunction(BaseEvaFunction aFunction, Add addFlag);

	DefaultLivingClass addClass(EvaClass aClass, Add aMainClass);

	void addNamespace(EvaNamespace aNamespace, Add aNone);

	void addModuleProcess(CompletableProcess<WorldModule> aCompletableProcess);

	Collection<WorldModule> modules();

	LivingNamespace getNamespace(EvaNamespace aEvaNamespace);

	LivingClass getClass(EvaClass aEvaClass);

	LivingFunction getFunction(BaseEvaFunction aEvaNode);

	enum Add {NONE, MAIN_FUNCTION, MAIN_CLASS}
}
