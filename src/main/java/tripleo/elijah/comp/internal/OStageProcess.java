package tripleo.elijah.comp.internal;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
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
	private       stepA_mal.MalEnv2  env;
	//private final ProcessRecord pr;
	private final ICompilationAccess ca;
	private       AccessBus          ab;

	public OStageProcess(final ICompilationAccess aCa, final @NotNull ProcessRecord aPr) {
		ca = aCa;

		ca.getCompilation().getCompilationEnclosure().getAccessBusPromise()
				.then(iab-> {
					ab = aPr.ab();

					env = ab.env();

					Preconditions.checkNotNull(ab);
					env.set(new types.MalSymbol("add-pipeline"), new _AddPipeline__MAL(ab));
				});

	}

	@Override
	public void run(final @NotNull Compilation aCompilation) {
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
				final CR_State.PipelinePlugin pipelinePlugin = ab.getPipelinePlugin(pipelineName);
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

