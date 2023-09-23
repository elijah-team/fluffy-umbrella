package tripleo.elijah.world.i;

import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleInput;

public interface WorldModule {
	EIT_ModuleInput input();

	OS_Module module();

//	GN_PL_Run2.GenerateFunctionsRequest rq();
}
