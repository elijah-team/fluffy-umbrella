package tripleo.elijah.comp;

import tripleo.elijah.stages.deduce.post_bytecode.Maybe;

public class CompilerInput {
	private final String                           inp;
	private       Ty                               ty;
	private       Maybe<ILazyCompilerInstructions> acceptance;

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

	public void accept_ci(final Maybe<ILazyCompilerInstructions> aM3) {
		acceptance = aM3;
	}

	public Maybe<ILazyCompilerInstructions> acceptance_ci() {
		return acceptance;
	}

	enum Ty {NULL, SOURCE_ROOT}
}
