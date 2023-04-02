package tripleo.elijah.comp;

public interface CompilationClosure {
	Compilation getCompilation();

	ErrSink errSink();

	IO io();
}
