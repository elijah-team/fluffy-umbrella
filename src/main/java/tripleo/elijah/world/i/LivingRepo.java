package tripleo.elijah.world.i;

import tripleo.elijah.comp.CompilationEnclosure;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.world.impl.DefaultLivingClass;
import tripleo.elijah.world.impl.DefaultLivingFunction;

import java.util.List;

public interface LivingRepo {
	LivingClass addClass(ClassStatement cs);

	LivingFunction addFunction(BaseFunctionDef fd);

	LivingPackage addPackage(OS_Package pk);

	OS_Package getPackage(String aPackageName);

	DefaultLivingFunction addFunction(BaseGeneratedFunction aFunction, Add addFlag);

	DefaultLivingClass addClass(GeneratedClass aClass, Add aMainClass);

	void addNamespace(GeneratedNamespace aNamespace, Add aNone);

	void __addModule(OS_Module aModule, final CompilationEnclosure aC);

	List<ClassStatement> findClass(String aClassName);

	enum Add {NONE, MAIN_FUNCTION, MAIN_CLASS}
}
