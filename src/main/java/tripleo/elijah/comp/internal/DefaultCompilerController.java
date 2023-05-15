package tripleo.elijah.comp.internal;

import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.OptionsProcessor;

import java.util.List;
import java.util.stream.Collectors;

public class DefaultCompilerController implements CompilerController {
	List<String>   args;
	String[]       args2;
	CompilationBus cb;
	private Compilation         c;
	private List<CompilerInput> inputs;

	@Override
	public void printUsage() {
		tripleo.elijah.util.Stupidity.println_out_2("Usage: eljc [--showtree] [-sE|O] <directory or .ez file names>");
	}

	@Override
	public void processOptions() {
		final OptionsProcessor             op  = new ApacheOptionsProcessor();
		final CompilerInstructionsObserver cio = new CompilerInstructionsObserver(c, op);
		cb = new CompilationBus(c.getCompilationEnclosure());

		c.getCompilationEnclosure().setCompilationBus(cb);

		c._cis._cio = cio;

		try {
			args2 = op.process(c, inputs, cb);
		} catch (final Exception e) {
			c.getErrSink().exception(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void runner() {
		try {
			c.subscribeCI(c._cis._cio);

			if (c.cb == null) {
				c.cb = new CompilationBus(c);
			}

			assert c.getCompilationEnclosure().getCompilationAccess() == null;
			final DefaultCompilationAccess ca = new DefaultCompilationAccess(c);
			c.getCompilationEnclosure().setCompilationAccess(ca);

			c.__cr = new CompilationRunner(c.getCompilationEnclosure().getCompilationAccess());

			hook(c.__cr);

			c.__cr.doFindCIs(inputs, args2, cb);
		} catch (final Exception e) {
			c.getErrSink().exception(e);
			throw new RuntimeException(e);
		}
	}

	public void hook(final CompilationRunner aCr) {

	}

	@Override
	public void _setInputs(final Compilation aCompilation, final List<CompilerInput> aInputs) {
		c      = aCompilation;
		inputs = aInputs;
	}

	public void _setInputs(final List<CompilerInput> aInputs) {
		inputs = aInputs;
	}
}
