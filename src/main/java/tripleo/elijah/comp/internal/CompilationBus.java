package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationChange;
import tripleo.elijah.comp.ICompilationBus;
import tripleo.elijah.comp.ILazyCompilerInstructions;
import tripleo.elijah.comp.i.CB_Monitor;
import tripleo.elijah.comp.i.CB_OutputString;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static tripleo.elijah.util.Helpers.List_of;

public class CompilationBus implements ICompilationBus {
	private static class SingleActionProcess implements CB_Process {
		private final CB_Action action;

		public SingleActionProcess(final CB_Action aAction) {
			action = aAction;
		}

		@Override
		public List<CB_Action> steps() {
			final var a = new CB_Action() {
				@Override
				public void execute(final CB_Monitor aMonitor) {
					action.execute(aMonitor);
				}

				@Override
				public String name() {
					return "Single Action Process";
				}

				@Override
				public List<CB_OutputString> outputStrings() {
					return List_of();
				}
			};
			return List_of(a);
		}
	}
	private static final Logger LOG = LoggerFactory.getLogger(CompilationBus.class);


	@SuppressWarnings("TypeMayBeWeakened")
	private final Queue<CB_Process> pq = new ConcurrentLinkedQueue<CB_Process>();

	private final Compilation       c;

	public CompilationBus(final Compilation aC) {
		c = aC;
	}

	public void add(final CB_Action action) {
		System.err.println("5756a "+ action.name());
		add(new SingleActionProcess(action));
	}

	@Override
	public void add(final CB_Process aProcess) {
		System.err.println("5756b "+ aProcess.name());
		pq.add(aProcess);
	}

	@Override
	public void inst(final @NotNull ILazyCompilerInstructions aLazyCompilerInstructions) {
		// TODO 09/15 how many times are we going to do this?
		System.out.println("** [ci] " + aLazyCompilerInstructions.get());
	}

	@Override
	public void option(final @NotNull CompilationChange aChange) {
		aChange.apply(c);
	}

	@Override
	public void run_all() {
		var procs = pq;

		final Thread thread = new Thread(() -> {
			LOG.debug("Polling...");
			boolean x = true;
			while (x) {
				final CB_Process poll = procs.poll();
				LOG.debug("Polled: " + poll);
				System.err.println("5759 poll: "+poll);

				if (poll != null) {
					System.err.println("5757 "+ poll.name());
					poll.execute(this);
				} else {
					System.err.println("5758 poll returned null");
					LOG.debug("poll returned null");
					x = false;
				}
			}
		});
		thread.start();

//		for (final CB_Process process : pq) {
//			System.err.println("5757 "+process.name());
//			process.execute(this);
//		}

		try {
			thread.join();//TimeUnit.MINUTES.toMillis(1));
			thread.stop();
		} catch (InterruptedException aE) {
			throw new RuntimeException(aE);
		}
	}
}
