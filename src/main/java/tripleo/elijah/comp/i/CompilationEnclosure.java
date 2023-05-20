package tripleo.elijah.comp.i;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import tripleo.elijah.comp.AccessBus;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.internal.CR_State;

public class CompilationEnclosure {

	private IPipelineAccess pa;
	private       PipelineLogic pipelineLogic;
	private       AccessBus     ab;
	private final Compilation                                 compilation;
	public final  DeferredObject<IPipelineAccess, Void, Void> pipelineAccessPromise = new DeferredObject<>();
	private final DeferredObject<AccessBus, Void, Void>       accessBusPromise      = new DeferredObject<>();

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
}
