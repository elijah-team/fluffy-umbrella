package tripleo.elijah.comp.functionality.f203;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ErrSink;

import java.io.File;

public class F203 {
	private final ErrSink errSink;
	ChooseDirectoryNameBehavior cdn;

	@Contract(pure = true)
	public F203(final ErrSink aErrSink, final Compilation c) {
		errSink = aErrSink;
//		cdn = new ChooseCompilationNameBehavior(c);
		cdn = new ChooseHashDirectoryNameBehavior(c);
	}

	public File chooseDirectory() {
		return cdn.chooseDirectory();
	}
}
