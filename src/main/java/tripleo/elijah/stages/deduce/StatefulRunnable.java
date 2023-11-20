package tripleo.elijah.stages.deduce;

import tripleo.elijah.stages.deduce.post_bytecode.DefaultStateful;

class StatefulRunnable extends DefaultStateful implements IStateRunnable {
	private final Runnable runnable;

	public StatefulRunnable(final Runnable aRunnable) {
		runnable = aRunnable;
	}

	public void run() {
		runnable.run();
	}
}
