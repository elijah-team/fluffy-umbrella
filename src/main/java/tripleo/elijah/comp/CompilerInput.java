package tripleo.elijah.comp;

import tripleo.elijah.comp.i.ILazyCompilerInstructions;
import tripleo.elijah.util.Maybe;

import java.io.File;

public class CompilerInput {
	private final String                           inp;
	private       Ty                               ty;
	private       Maybe<ILazyCompilerInstructions> acceptance;
	private       File                             dir_carrier;

	public CompilerInput(final String aS) {
		inp = aS;
		ty  = Ty.NULL;
	}

	public String getInp() {
		return inp;
	}

	public void setSourceRoot() {
		ty = Ty.SOURCE_ROOT;
	}

	public boolean isSourceRoot() {
		return ty == Ty.SOURCE_ROOT;
	}

	public boolean isNull() {
		return ty == Ty.NULL;
	}

	public void accept_ci(final Maybe<ILazyCompilerInstructions> aM3) {
		acceptance = aM3;
	}

	public Maybe<ILazyCompilerInstructions> acceptance_ci() {
		return acceptance;
	}

	public void setDirectory(File f) {
		ty          = Ty.SOURCE_ROOT;
		dir_carrier = f;
	}

	enum Ty {NULL, SOURCE_ROOT}
}
