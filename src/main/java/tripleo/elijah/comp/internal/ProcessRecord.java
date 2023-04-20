package tripleo.elijah.comp.internal;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;

import tripleo.elijah.comp.i.CompilationClosure;
import tripleo.vendor.mal.stepA_mal;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.comp.notation.GN_Notable;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProcessRecord {
	public final  PipelineLogic                              pipelineLogic;
	public final  DeducePipeline                             dpl;
	private final ICompilationAccess                         ca;
	public final  AccessBus                                  ab;
	private       DeferredObject<GenerateResult, Void, Void> _pgr;
	public final  IPipelineAccess                            pa = new ProcessRecord_PipelineAccess();
	final         stepA_mal.MalEnv2                          env;

	public ProcessRecord(final @NotNull ICompilationAccess ca0) {
		ca            = ca0;
		ca.getCompilation()._pa = pa;

		pipelineLogic = new PipelineLogic(ca0);
		dpl           = new DeducePipeline(pa);

		ab            = new AccessBus(ca.getCompilation(), pa);
		ab.addPipelinePlugin(new GeneratePipelinePlugin());
		ab.addPipelinePlugin(new DeducePipelinePlugin());
		ab.addPipelinePlugin(new WritePipelinePlugin());
		ab.addPipelinePlugin(new WriteMesonPipelinePlugin());

//		ab.addPipelineLogic(PipelineLogic::new);
////		ab.add(DeducePipeline::new);

		pa._setAccessBus(ab);
		
		env = new stepA_mal.MalEnv2(null); // TODO what does null mean?
	}

	public void writeLogs(final ICompilationAccess aCa) {
		final ICompilationAccess ca = aCa;

		ca.getCompilation().stage.writeLogs(ca);
	}

	public Promise<GenerateResult, Void, Void> generateResultPromise() {
		if (_pgr == null) {
			_pgr = new DeferredObject<>();
		}
		return _pgr;
	}

	public void setGenerateResult(final GenerateResult gr) {
		_pgr.resolve(gr);
	}

	public void consumeGenerateResult(final @NotNull Consumer<Supplier<GenerateResult>> csgr) {
		csgr.accept(() -> {
			final GenerateResult[] xx = new GenerateResult[1];
			generateResultPromise().then((x) -> xx[0] = x);
			return xx[0];
		});
	}




//
//	public void writeLogs(final @NotNull ICompilationAccess ca) {
//		//ab.writeLogs();
//		ca.getStage().writeLogs(ca);
//	}

	public interface PipelinePlugin {
		String name();

		PipelineMember instance(final @NotNull AccessBus ab0);
	}

	class GeneratePipelinePlugin implements PipelinePlugin {

		@Override
		public String name() {
			return "GeneratePipeline";
		}

		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new GeneratePipeline(ab0);
		}
	}

	class DeducePipelinePlugin implements PipelinePlugin {

		@Override
		public String name() {
			return "DeducePipeline";
		}

		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new DeducePipeline(ab0.getPipelineAccess());
		}
	}

	class WritePipelinePlugin implements PipelinePlugin {
		@Override
		public String name() {
			return "WritePipeline";
		}

		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new WritePipeline(ab0.getPipelineAccess());
		}
	}

	class WriteMesonPipelinePlugin implements PipelinePlugin {
		@Override
		public String name() {
			return "WriteMesonPipeline";
		}

		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new WriteMesonPipeline(ab0);
		}
	}

	class ProcessRecord_PipelineAccess implements IPipelineAccess {
		private       List<CompilerInput>                       inp;
		private       WritePipeline                             _wpl;
		private       AccessBus                                 _ab;
		private final DeferredObject<List<EvaNode>, Void, Void> nlp = new DeferredObject<>();
		private final DeferredObject<PipelineLogic, Void, Void> ppl = new DeferredObject<>();

		@Override
		public Compilation getCompilation() {
			return ca.getCompilation();
		}

		@Override
		public DeducePipeline getDeducePipeline() {
			return dpl;
		}

		@Override
		public PipelineLogic pipelineLogic() {
			return pipelineLogic;
		}

		@Override
		public ProcessRecord getProcessRecord() {
			return ProcessRecord.this;
		}

		@Override
		public DeferredObject<PipelineLogic, Void, Void> getPipelineLogicPromise() {
			return ppl;
		}

		@Override
		public void setNodeList(final List<EvaNode> aEvaNodeList) {
			nlp/*;)*/.resolve(aEvaNodeList);
		}

		@Override
		public void registerNodeList(final DoneCallback<List<EvaNode>> done) {
			nlp.then(done);
		}

		@Override
		public void _setAccessBus(final AccessBus ab) {
			_ab = ab;
		}

		@Override
		public AccessBus getAccessBus() {
			return _ab;
		}

		@Override
		public WritePipeline getWitePipeline() {
			return _wpl;
		}

		@Override
		public void setWritePipeline(final WritePipeline aWritePipeline) {
			_wpl = aWritePipeline;
		}

		@Override
		public void notate(final int provenance, final GN_Notable aNotable) {
			int y=2;
			aNotable.run();
		}

		@Override
		public List<CompilerInput> getCompilerInput() {
			return inp;
		}

		@Override
		public void setCompilerInput(final List<CompilerInput> aInputs) {
			inp = aInputs;
		}

		@Override
		public CompilationClosure getCompilationClosure() {
			return getCompilation().getCompilationClosure();
		}
	}
}
