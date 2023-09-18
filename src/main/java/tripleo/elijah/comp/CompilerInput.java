package tripleo.elijah.comp;

import tripleo.elijah.util.Maybe;

import java.io.File;

public class CompilerInput {
	public enum Ty {NULL, SOURCE_ROOT, ARG}
	private final String                           inp;
	private       Maybe<ILazyCompilerInstructions> accept_ci;
	private       File                             dir_carrier;
	private       Ty                               ty;

	private       String                           hash;

	public CompilerInput(final String aS) {
		inp = aS;
		ty  = Ty.NULL;
	}

	public void accept_ci(final Maybe<ILazyCompilerInstructions> aM3) {
		accept_ci = aM3;
	}

	public void accept_hash(final String hash) {
		this.hash = hash;
	}

	public Maybe<ILazyCompilerInstructions> acceptance_ci() {
		return accept_ci;
	}

	public String getInp() {
		return inp;
	}

	public boolean isNull() {
		return ty == Ty.NULL;
	}

	public boolean isSourceRoot() {
		return ty == Ty.SOURCE_ROOT;
	}

	public void setArg() {
		ty = Ty.ARG;
	}

	public void setDirectory(File f) {
		ty          = Ty.SOURCE_ROOT;
		dir_carrier = f;
	}

	public void setSourceRoot() {
		ty = Ty.SOURCE_ROOT;
	}

	public Ty ty() {
		return ty;
	}
}
