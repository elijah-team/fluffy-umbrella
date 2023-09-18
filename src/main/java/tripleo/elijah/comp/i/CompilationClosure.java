package tripleo.elijah.comp.i;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.IO;

public interface CompilationClosure {
	ErrSink errSink();

	Compilation getCompilation();

	IO io();
}
