package tripleo.elijah.comp;

import tripleo.elijah.comp.internal.CompilationBus;
import tripleo.elijah.comp.internal.DefaultProgressSink;
import tripleo.elijah.util.Ok;
import tripleo.elijah.util.Operation;

import java.util.List;

public class DefaultCompilerController implements CompilerController {
	List<String>   args;
	String[]       args2;
	CompilationBus cb;
	private Compilation c;

	@Override
	public void printUsage() {
		System.out.println("Usage: eljc [--showtree] [-sE|O] <directory or .ez file names>");
	}

	@Override
	public void processOptions() {
		final OptionsProcessor             op  = new ApacheOptionsProcessor();
		final CompilerInstructionsObserver cio = new CompilerInstructionsObserver(c, op, c._cis);
		cb = new CompilationBus(c);

		try {
			args2 = op.process(c, args, cb);
		} catch (final Exception e) {
			c.getErrSink().exception(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Operation<Ok> runner() {
		try {
			c.__cr = new CompilationRunner(c, c._cis, cb, new DefaultProgressSink());
			c.__cr.doFindCIs(args2, cb);

			cb.run_all();

			return Operation.success(Ok.instance());
		} catch (final Exception e) {
			c.getErrSink().exception(e);

			return Operation.failure(e);
		}
	}

	public void _set(final Compilation aCompilation, final List<String> aArgs) {
		c    = aCompilation;
		args = aArgs;
	}
}
