package tripleo.elijah.stages.gen_c;

import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.util.buffer.Buffer;

public interface C2C_Result {
	Buffer getBuffer();

	OS_Module getDefinedModule();

	EG_Statement getStatement();

	WhyNotGarish_BaseFunction getWhyNotGarishFunction();

	GenerateResult.TY ty();
}
