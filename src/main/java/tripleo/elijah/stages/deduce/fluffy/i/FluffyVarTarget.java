package tripleo.elijah.stages.deduce.fluffy.i;

public interface FluffyVarTarget {
	T getT();

	/**
	 * MEMBER means class or namespace
	 * FUNCTION means a function or something "under" it (loop, etc)
	 * <br/>
	 * ARGUMENT means a function argument (not used...)
	 */
	enum T {MEMBER, FUNCTION}

}
