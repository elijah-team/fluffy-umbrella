package tripleo.elijah.world.i;

import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleInput;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.util.Eventual;

public interface WorldModule {
	OS_Module module();

	EIT_ModuleInput input();

//	GN_PL_Run2.GenerateFunctionsRequest rq();

	Eventual<DeducePhase.GeneratedClasses> getEventual();
}
