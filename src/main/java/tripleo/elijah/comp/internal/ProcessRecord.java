package tripleo.elijah.comp.internal;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.ICompilationAccess;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProcessRecord {
	public final  PipelineLogic      pipelineLogic;
	public final  DeducePipeline                             dpl;
	private final ICompilationAccess                         ca;
	private       DeferredObject<GenerateResult, Void, Void> _pgr;


	public final IPipelineAccess pa = new IPipelineAccess() {
		final DeferredObject<List<EvaNode>, Void, Void> nlp = new DeferredObject<>();

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

		private final DeferredObject<PipelineLogic, Void, Void> ppl = new DeferredObject<>();

		@Override
		public Promise<PipelineLogic, Void, Void> getPipelineLogicPromise() {
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
	};

	public ProcessRecord(final @NotNull ICompilationAccess ca0) {
		ca            = ca0;

		pipelineLogic = new PipelineLogic(ca0);
		dpl           = new DeducePipeline(pa);








		ca.getCompilation()._pa = pa;
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




//	public final AccessBus ab;
//
//	public ProcessRecord(final @NotNull ICompilationAccess ca0) {
//		ab = new AccessBus(ca0.getCompilation());
//
//		ab.addPipelinePlugin(new GeneratePipelinePlugin());
//		ab.addPipelinePlugin(new DeducePipelinePlugin());
//		ab.addPipelinePlugin(new WritePipelinePlugin());
//		ab.addPipelinePlugin(new WriteMesonPipelinePlugin());
//
//		ab.addPipelineLogic(PipelineLogic::new);
////		ab.add(DeducePipeline::new);
//	}
//
//	public void writeLogs(final @NotNull ICompilationAccess ca) {
//		//ab.writeLogs();
//		ca.getStage().writeLogs(ca);
//	}

	public interface PipelinePlugin {
		String name();

		PipelineMember instance(final @NotNull AccessBus ab0);
	}

//	class GeneratePipelinePlugin implements PipelinePlugin {
//
//		@Override
//		public String name() {
//			return "GeneratePipeline";
//		}
//
//		@Override
//		public PipelineMember instance(final @NotNull AccessBus ab0) {
//			return new GeneratePipeline(ab0);
//		}
//	}
//
//	class DeducePipelinePlugin implements PipelinePlugin {
//
//		@Override
//		public String name() {
//			return "DeducePipeline";
//		}
//
//		@Override
//		public PipelineMember instance(final @NotNull AccessBus ab0) {
//			return new DeducePipeline(ab0);
//		}
//	}
//
//	class WritePipelinePlugin implements PipelinePlugin {
//		@Override
//		public String name() {
//			return "WritePipeline";
//		}
//
//		@Override
//		public PipelineMember instance(final @NotNull AccessBus ab0) {
//			return new WritePipeline(ab0);
//		}
//	}
//
//	class WriteMesonPipelinePlugin implements PipelinePlugin {
//		@Override
//		public String name() {
//			return "WriteMesonPipeline";
//		}
//
//		@Override
//		public PipelineMember instance(final @NotNull AccessBus ab0) {
//			return new WriteMesonPipeline(ab0);
//		}
//	}

}
