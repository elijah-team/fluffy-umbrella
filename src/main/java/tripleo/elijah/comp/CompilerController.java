package tripleo.elijah.comp;

import java.util.List;

public interface CompilerController {
	void printUsage();

	void processOptions();

	void runner();

	void _setInputs(Compilation aCompilation, List<CompilerInput> aInputs);
}
