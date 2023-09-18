package tripleo.elijah.world.i;

import java.util.Collection;
import java.util.List;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.util.CompletableProcess;
import tripleo.elijah.world.impl.DefaultLivingClass;
import tripleo.elijah.world.impl.DefaultLivingFunction;
import tripleo.elijah.world.impl.DefaultLivingNamespace;

public interface LivingRepo {
	enum Add {MAIN_CLASS, MAIN_FUNCTION, NONE}

	LivingClass addClass(ClassStatement cs);

	DefaultLivingClass addClass(EvaClass aClass, Add addFlag);

	DefaultLivingFunction addFunction(BaseEvaFunction aFunction, Add aMainFunction);

	LivingFunction addFunction(BaseFunctionDef fd);

	void addModule(OS_Module mod, String aFilename, final Compilation aC);

	//DefaultLivingClass addClass(EvaClass aClass, Add aMainClass);

	void addModule2(WorldModule aWorldModule);

	void addModuleProcess(CompletableProcess<WorldModule> wmcp);

	DefaultLivingNamespace addNamespace(EvaNamespace aNamespace, Add aNone);

	LivingPackage addPackage(OS_Package pk);

	LivingClass getClass(EvaClass aEvaClass);

	List<LivingClass> getClassesForClassNamed(String string);

	List<LivingClass> getClassesForClassStatement(ClassStatement cls);

	LivingFunction getFunction(BaseEvaFunction aBaseEvaFunction);

	WorldModule getModule(OS_Module aSuccess);

	LivingNamespace getNamespace(EvaNamespace aEvaNamespace);

	OS_Package getPackage(String aPackageName);

	boolean hasPackage(String aPackageName);

	OS_Package makePackage(Qualident aPkgName);

	Collection<WorldModule> modules();
}
