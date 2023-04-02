package tripleo.elijah.comp.internal;

//import com.google.common.base.Preconditions;
//import mal.stepA_mal;
//import mal.types;
//import tripleo.elijah.comp.AccessBus;
//import tripleo.elijah.comp.Compilation;
//import tripleo.elijah.comp.ICompilationAccess;
//import tripleo.elijah.comp.Pipeline;
//import tripleo.elijah.comp.i.RuntimeProcess;

//public class OStageProcess implements RuntimeProcess {
//	private final ProcessRecord      pr;
//	private final ICompilationAccess ca;
//	final         stepA_mal.MalEnv2  env;
//
//	public OStageProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
//		ca = aCa;
//		pr = aPr;
//
//		env = new stepA_mal.MalEnv2(null); // TODO what does null mean?
//
//		Preconditions.checkNotNull(pr.ab);
//		env.set(new types.MalSymbol("add-pipeline"), new _AddPipeline__MAL(pr.ab));
//	}
//
//	@Override
//	public void run() {
//		final AccessBus ab = pr.ab;
//
//		ab.subscribePipelineLogic((pl) -> {
//			final Compilation comp = ca.getCompilation();
//			final Pipeline    ps   = comp.getPipelines();
//
//			try {
//				ps.run();
//
//				ab.writeLogs();
//			} catch (final Exception ex) {
////				Logger.getLogger(OStageProcess.class.getName()).log(Level.SEVERE, "Error during Piplines#run from OStageProcess", ex);
//				comp.getErrSink().exception(ex);
//			}
//		});
//	}
//
//	@Override
//	public void postProcess() {
//	}
//
//	@Override
//	public void prepare() throws Exception {
//		Preconditions.checkNotNull(pr);
//		Preconditions.checkNotNull(pr.ab.gr);
//
//		final AccessBus ab = pr.ab;
//
////		env.re("(def! GeneratePipeline 'native)");
//		env.re("(add-pipeline 'DeducePipeline)"); // FIXME note moved from ...
//
////		ab.add(GeneratePipeline::new);
////		ab.add(WritePipeline::new);
////		ab.add(WriteMesonPipeline::new);
//		env.re("(add-pipeline 'GeneratePipeline)");
//		env.re("(add-pipeline 'WritePipeline)");
//		env.re("(add-pipeline 'WriteMesonPipeline)");
//
//		ab.subscribePipelineLogic(pl -> {
//			final Compilation comp = ca.getCompilation();
//
//			comp.eachModule(pl::addModule);
//		});
//	}
//
//	private static class _AddPipeline__MAL extends types.MalFunction {
//		private final AccessBus ab;
//
//		public _AddPipeline__MAL(final AccessBus aAb) {
//			ab = aAb;
//		}
//
//		public types.MalVal apply(final types.MalList args) throws types.MalThrowable {
//			final types.MalVal a0 = args.nth(0);
//
//			if (a0 instanceof final types.MalSymbol pipelineSymbol) {
//				// 0. accessors
//				final String pipelineName = pipelineSymbol.getName();
//
//				// 1. observe side effect
//				final ProcessRecord.PipelinePlugin pipelinePlugin = ab.getPipelinePlugin(pipelineName);
//				if (pipelinePlugin == null)
//					return types.False;
//
//				// 2. produce effect
//				ab.add(pipelinePlugin::instance);
//				return types.True;
//			} else {
//				// TODO exception? errSink??
//				return types.False;
//			}
//		}
//	}
//}
