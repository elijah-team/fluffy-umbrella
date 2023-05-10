package tripleo.elijah.stages.gen_generic;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.logging.ElLog;

public class OutputFileFactoryParams {
	private final OS_Module            mod;
	private final ErrSink              errSink;
	private final ElLog.Verbosity      verbosity;
	private final CompilationEnclosure compilationEnclsure;

	@Contract(pure = true)
	public OutputFileFactoryParams(final OS_Module aMod,
								   final ErrSink aErrSink,
								   final ElLog.Verbosity aVerbosity,
								   final CompilationEnclosure aCompilationEnclsure) {
		mod           = aMod;
		errSink       = aErrSink;
		verbosity     = aVerbosity;
		compilationEnclsure = aCompilationEnclsure;
	}

	public OS_Module getMod() {
		return mod;
	}

	public ErrSink getErrSink() {
		return errSink;
	}

	public ElLog.Verbosity getVerbosity() {
		return verbosity;
	}

	public PipelineLogic getPipelineLogic() {
		return getCompilationEnclosure().getPipelineLogic();
	}

	public CompilationEnclosure getCompilationEnclosure() {
		return compilationEnclsure;
	}
}
