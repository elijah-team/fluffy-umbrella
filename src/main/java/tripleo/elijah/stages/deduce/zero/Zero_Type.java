package tripleo.elijah.stages.deduce.zero;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.stages.gen_fn.GenType;

class Zero_Type {

	private final GenType gt;

	@Contract(pure = true)
	public Zero_Type(final GenType aGt) {
		gt = aGt;
	}

	public GenType genType() {
		return gt;
	}
}
