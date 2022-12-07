package tripleo.elijah.comp;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_generic.GenerateResult;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProcessRecord {
	public final PipelineLogic                              pipelineLogic;
	final        DeducePipeline                             dpl;
	//private final ICompilationAccess                         ca;
	private      DeferredObject<GenerateResult, Void, Void> _pgr;

	public ProcessRecord(final @NotNull ICompilationAccess ca0) {
		//ca            = ca0;

		pipelineLogic = new PipelineLogic(ca0.getAccessBus());
		dpl = new DeducePipeline(ca0.getAccessBus());
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
}
