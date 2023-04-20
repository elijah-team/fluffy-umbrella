package tripleo.elijah.comp.internal;

import com.google.common.base.Preconditions;
import tripleo.vendor.mal.stepA_mal;
import tripleo.vendor.mal.types;
import org.jdeferred2.impl.DeferredObject;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.i.RuntimeProcess;

import java.util.logging.Level;
import java.util.logging.Logger;

import static tripleo.elijah.util.Helpers.List_of;

public class OStageProcess implements RuntimeProcess {
	final         stepA_mal.MalEnv2      env;
	private final ProcessRecord pr;
	private final ICompilationAccess     ca;

	public OStageProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
		ca = aCa;
		pr = aPr;

		env = pr.env();

		Preconditions.checkNotNull(pr.ab());
		pr.env().set(new types.MalSymbol("add-pipeline"), new _AddPipeline__MAL(pr.ab()));

	}

	@Override
	public void run(final Compilation aCompilation) {
		Pipeline ps = aCompilation.getPipelines();

		try {
			ps.run();
		} catch (Exception ex) {
			Logger.getLogger(OStageProcess.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void postProcess() {
	}

	@Override
	public void prepare() throws Exception {
		Preconditions.checkNotNull(pr);
//		Preconditions.checkNotNull(pr.ab.gr);

		final AccessBus ab = pr.ab();

		env.re("(def! GeneratePipeline 'native)");
		env.re("(add-pipeline 'DeducePipeline)"); // FIXME note moved from ...

		env.re("(add-pipeline 'GeneratePipeline)");
		env.re("(add-pipeline 'WritePipeline)");
		env.re("(add-pipeline 'WriteMesonPipeline)");

		ab.subscribePipelineLogic(pl -> {
			final Compilation comp = ca.getCompilation();

			comp.eachModule(pl::addModule);
		});
	}

	//	@Override
	public void __00__prepare() {
		Preconditions.checkNotNull(pr);
		Preconditions.checkNotNull(pr.dpl());

		Preconditions.checkNotNull(pr.pipelineLogic());
		Preconditions.checkNotNull(pr.pipelineLogic().gr);

		final DeferredObject<PipelineLogic, Void, Void> ppl = (DeferredObject<PipelineLogic, Void, Void>) pr.pa().getPipelineLogicPromise();
		ppl.resolve(pr.pipelineLogic());

		final Compilation comp = ca.getCompilation();

		final DeducePipeline     dpl  = pr.dpl(); //new DeducePipeline      (ca);
		final GeneratePipeline   gpl  = new GeneratePipeline(pr.pa());
		final WritePipeline      wpl  = new WritePipeline(pr.pa());
		final WriteMesonPipeline wmpl = new WriteMesonPipeline(comp, pr, ppl, wpl);

		List_of(dpl, gpl, wpl, wmpl)
				.forEach(ca::addPipeline);

		pr.setGenerateResult(pr.pipelineLogic().gr);

		// NOTE Java needs help!
		//Helpers.<Consumer<Supplier<GenerateResult>>>List_of(wpl.consumer(), wmpl.consumer())
		List_of(wpl.consumer(), wmpl.consumer())
				.forEach(pr::consumeGenerateResult);
	}

	//@Override
	public void __00__run() {
		final AccessBus ab = pr.ab();

		ab.subscribePipelineLogic((pl) -> {
			final Compilation comp = ca.getCompilation();
			final Pipeline    ps   = comp.getPipelines();

			try {
				ps.run();

				ca.writeLogs();
			} catch (final Exception ex) {
//				Logger.getLogger(OStageProcess.class.getName()).log(Level.SEVERE, "Error during Piplines#run from OStageProcess", ex);
				comp.getErrSink().exception(ex);
			}
		});
	}

	private static class _AddPipeline__MAL extends types.MalFunction {
		private final AccessBus ab;

		public _AddPipeline__MAL(final AccessBus aAb) {
			ab = aAb;
		}

		@Override
		public types.MalVal apply(final types.MalList args) throws types.MalThrowable {
			final types.MalVal a0 = args.nth(0);

			if (a0 instanceof types.MalSymbol) {
				final types.MalSymbol pipelineSymbol = (types.MalSymbol) a0;
				// 0. accessors
				final String pipelineName = pipelineSymbol.getName();

				// 1. observe side effect
				final PipelinePlugin pipelinePlugin = ab.getPipelinePlugin(pipelineName);
				if (pipelinePlugin == null)
					return types.False;

				// 2. produce effect
				ab.add(pipelinePlugin::instance);
				return types.True;
			} else {
				// TODO exception? errSink??
				return types.False;
			}
		}
	}
}

