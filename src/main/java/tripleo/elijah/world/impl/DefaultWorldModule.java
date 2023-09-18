package tripleo.elijah.world.impl;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleInput;
import tripleo.elijah.stages.inter.ModuleThing;
import tripleo.elijah.world.i.WorldModule;

public class DefaultWorldModule implements WorldModule {
	private final OS_Module   mod;
	private       ModuleThing thing;

//	private GN_PL_Run2.GenerateFunctionsRequest rq;

	public DefaultWorldModule(final OS_Module aMod, final @NotNull CompilationEnclosure ce) {
		mod = aMod;
		final ModuleThing mt = ce.addModuleThing(mod);
		setThing(mt);
	}

	public void setThing(final ModuleThing aThing) {
		thing = aThing;
	}

	@Override
	public OS_Module module() {
		return mod;
	}

	@Override
	public EIT_ModuleInput input() {
		return null;
	}

//	@Override
//	public GN_PL_Run2.GenerateFunctionsRequest rq() {
//		return rq;
		// //throw new NotImplementedException("Unexpected");
//	}

	public ModuleThing thing() {
		return thing;
	}

//	public void setRq(final GN_PL_Run2.GenerateFunctionsRequest aRq) {
//		rq = aRq;
//		//throw new NotImplementedException("Unexpected");
//	}

	@Override
	public String toString() {
		return "DefaultWorldModule{%s}".formatted(mod.getFileName());
	}
}
