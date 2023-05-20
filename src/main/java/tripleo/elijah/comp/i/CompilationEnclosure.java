package tripleo.elijah.comp.i;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import tripleo.elijah.comp.AccessBus;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationRunner;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.internal.CR_State;
import tripleo.elijah.comp.internal.CompilationBus;
import tripleo.elijah.comp.internal.CompilerDriver;

public class CompilationEnclosure {

	public final     DeferredObject<IPipelineAccess, Void, Void> pipelineAccessPromise = new DeferredObject<>();
	private final    Compilation                                 compilation;
	private final    DeferredObject<AccessBus, Void, Void>       accessBusPromise      = new DeferredObject<>();
	private          IPipelineAccess                             pa;
	private          PipelineLogic                               pipelineLogic;
	private          AccessBus                                   ab;
	private @NotNull ICompilationAccess                          ca;
	private          CompilationBus                              compilationBus;
	private          CompilerDriver                              compilerDriver;
	private CompilationRunner compilationRunner;

	public CompilationEnclosure(final Compilation aCompilation) {
		compilation = aCompilation;

		getPipelineAccessPromise().then(pa -> {
			ab = new AccessBus(getCompilation(), pa);

			accessBusPromise.resolve(ab);

			ab.addPipelinePlugin(new CR_State.GeneratePipelinePlugin());
			ab.addPipelinePlugin(new CR_State.DeducePipelinePlugin());
			ab.addPipelinePlugin(new CR_State.WritePipelinePlugin());
			ab.addPipelinePlugin(new CR_State.WriteMesonPipelinePlugin());

//		ab.addPipelineLogic(PipelineLogic::new);
////		ab.add(DeducePipeline::new);

			pa._setAccessBus(ab);

			this.pa = pa;
		});
	}

	@Contract(pure = true)
	private Compilation getCompilation() {
		return compilation;
	}

	@Contract(pure = true)
	public Promise<IPipelineAccess, Void, Void> getPipelineAccessPromise() {
		return pipelineAccessPromise;
	}

	public PipelineLogic getPipelineLogic() {
		return pipelineLogic;
	}

	public void setPipelineLogic(final PipelineLogic aPipelineLogic) {
		pipelineLogic = aPipelineLogic;
	}

	public Promise<AccessBus, Void, Void> getAccessBusPromise() {
		return accessBusPromise;
	}

	public void setCompilationAccess(@NotNull ICompilationAccess aca) {
		ca = aca;
	}

	public ICompilationAccess getCompilationAccess() {
		return ca;
	}

	public CompilationBus getCompilationBus() {
		return compilationBus;
	}

	public void setCompilationBus(final CompilationBus aCompilationBus) {
		compilationBus = aCompilationBus;
	}

	public void setCompilerDriver(final CompilerDriver aCompilerDriver) {
		compilerDriver = aCompilerDriver;
	}

	public CompilerDriver getCompilerDriver() {
		return compilerDriver;
	}

	public IPipelineAccess getPipelineAccess() {
		return pa;
	}

	public void setCompilationRunner(final CompilationRunner aCompilationRunner) {
		compilationRunner = aCompilationRunner;
	}

	public CompilationRunner getCompilationRunner() {
		return compilationRunner;
	}

	public CompilerDriver getCompilationDriver() {
		return getCompilationBus().cd;
	}
}
