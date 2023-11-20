package tripleo.elijah.stages.deduce;

import tripleo.elijah.stages.deduce.post_bytecode.DefaultStateful;

public class StatefulBool extends DefaultStateful {
	private boolean value;

	public boolean getValue() {
		return value;
	}

	public void setValue(final boolean aValue) {
		value = aValue;
	}
}
