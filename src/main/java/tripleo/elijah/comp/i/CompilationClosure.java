package tripleo.elijah.comp.i;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.IO;

public interface CompilationClosure {
	Compilation getCompilation();

	ErrSink errSink();

	IO io();
}
