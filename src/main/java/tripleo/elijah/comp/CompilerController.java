package tripleo.elijah.comp;

import tripleo.elijah.util.Ok;
import tripleo.elijah.util.Operation;

public interface CompilerController {
	void printUsage();

	void processOptions();

	Operation<Ok> runner();
}
