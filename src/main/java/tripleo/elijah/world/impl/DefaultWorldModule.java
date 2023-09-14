package tripleo.elijah.world.impl;

import tripleo.elijah.*;
import tripleo.elijah.comp.*;
import tripleo.elijah.lang.*;
import tripleo.elijah.nextgen.inputtree.*;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.world.i.*;

public class DefaultWorldModule implements WorldModule {
	final Eventual<DeducePhase.GeneratedClasses> _p_GeneratedClasses = new Eventual<>();
//	private       ModuleThing thing;

//	@Getter
//	private GN_PL_Run2.GenerateFunctionsRequest rq;
	private final OS_Module   mod;

	public DefaultWorldModule(final OS_Module aMod, final CompilationEnclosure aCe) {
		mod = aMod;
	}

//	public DefaultWorldModule(final OS_Module aMod, final GN_PL_Run2.GenerateFunctionsRequest aRq) {
//		mod = aMod;
//		rq  = aRq;
//	}

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
//	}

//	public DefaultWorldModule(final OS_Module aMod, final @NotNull CompilationEnclosure ce) {
//		mod = aMod;
//		final ModuleThing mt = ce.addModuleThing(mod);
//		setThing(mt);
//	}

	@Override
	public Eventual<DeducePhase.GeneratedClasses> getEventual() {
		return _p_GeneratedClasses;
	}

//	public void setRq(final GN_PL_Run2.GenerateFunctionsRequest aRq) {
//		rq = aRq;
//	}

//	public ModuleThing thing() {
//		return thing;
//	}

//	public void setThing(final ModuleThing aThing) {
//		thing = aThing;
//	}
}
