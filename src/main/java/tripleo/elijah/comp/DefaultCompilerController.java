package tripleo.elijah.comp;

import tripleo.elijah.comp.internal.CompilationBus;

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
		final CompilerInstructionsObserver cio = new CompilerInstructionsObserver(c, c._cis);
		cb = new CompilationBus(c);

		try {
//			if (args == null) {
//				args = inputs.stream()
//				             .map(ci -> ci.getInp())
//				             .collect(Collectors.toList());
//			}

			if (inputs == null) {
				inputs = args.stream()
				             .map(str -> new CompilerInput(str))
				             .collect(Collectors.toList());
			}

			args2 = op.process(c, inputs, cb);
		} catch (final Exception e) {
			c.getErrSink().exception(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void runner() {
		try {
			c.__cr = new CompilationRunner(c, c._cis, cb);

			for (final String s : args2) {
				for (final CompilerInput input : inputs) {

					if (s.equals(input.getInp())) {
						input.setSourceRoot();
					}
				}
			}

			c.__cr.doFindCIs(inputs, args2, cb);
		} catch (final Exception e) {
			c.getErrSink().exception(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void _setInputs(final Compilation aCompilation, final List<CompilerInput> aInputs) {
		c      = aCompilation;
		inputs = aInputs;
	}

	public void _set(final Compilation aCompilation, final List<String> aArgs) {
		c    = aCompilation;
		args = aArgs;
	}
}
