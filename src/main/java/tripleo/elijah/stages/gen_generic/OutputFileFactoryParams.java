package tripleo.elijah.stages.gen_generic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.world.i.WorldModule;

public class OutputFileFactoryParams {
	private final OS_Module       mod;
	private final ErrSink         errSink;
	private final ElLog.Verbosity verbosity;
	private final PipelineLogic   pipelineLogic;
	private final @NotNull CompilationEnclosure compilationEnclosure;

	@Contract(pure = true)
	public OutputFileFactoryParams(final OS_Module aMod,
	                               final ErrSink aErrSink,
	                               final ElLog.Verbosity aVerbosity,
	                               final PipelineLogic aPipelineLogic, CompilationEnclosure compilationEnclosure) {
		mod           = aMod;
		errSink       = aErrSink;
		verbosity     = aVerbosity;
		pipelineLogic = aPipelineLogic;
		this.compilationEnclosure = compilationEnclosure;
	}

	public OutputFileFactoryParams(final WorldModule aMod, final CompilationEnclosure aCe) {
		throw new NotImplementedException();

	}

	public OS_Module getMod() {
		return mod;
	}

	public String getModFileName() {
		return mod.getFileName();
	}

	public ErrSink getErrSink() {
		return errSink;
	}

	public ElLog.Verbosity getVerbosity() {
		return verbosity;
	}

	public void addLog(final ElLog aLOG) {
		getPipelineLogic().addLog(aLOG);
	}

	public PipelineLogic getPipelineLogic() {
		return pipelineLogic;
	}

	public @NotNull CompilationEnclosure getCompilationEnclosure() {
		return compilationEnclosure;
	}

	public WorldModule getWorldMod() {
		throw new NotImplementedException();

	}
}
