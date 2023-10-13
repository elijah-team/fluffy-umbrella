package tripleo.elijah.comp.i;

import tripleo.elijah.comp.*;

public interface CompilationClosure {
	ErrSink errSink();

	Compilation getCompilation();

	IO io();
}
