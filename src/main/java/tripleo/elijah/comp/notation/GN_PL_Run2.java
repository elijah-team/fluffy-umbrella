//package tripleo.elijah.comp.notation;
//
//import org.jetbrains.annotations.Contract;
//import org.jetbrains.annotations.NotNull;
//import tripleo.elijah.comp.PipelineLogic;
//import tripleo.elijah.comp.i.CompilationEnclosure;
//import tripleo.elijah.lang.OS_Module;
//import tripleo.elijah.stages.deduce.DeducePhase;
//import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
//import tripleo.elijah.stages.inter.ModuleThing;
//import tripleo.elijah.util.Eventual;
//import tripleo.elijah.util.EventualRegister;
//import tripleo.elijah.world.i.WorldModule;
//import tripleo.elijah.world.impl.DefaultWorldModule;
//
//import java.util.function.Consumer;
//
//public class GN_PL_Run2 implements GN_Notable, @NotNull EventualRegister {
//	private final @NotNull WorldModule mod;
//	private final PipelineLogic         pipelineLogic;
//	private final CompilationEnclosure  ce;
//	private final DefaultClassGenerator dcg;
//	private final          Consumer<WorldModule> worldConsumer;
//
//	@Contract(pure = true)
//	public GN_PL_Run2(final PipelineLogic aPipelineLogic,
//					  final @NotNull WorldModule aMod,
//					  final CompilationEnclosure aCe,
//					  final Consumer<WorldModule> aWorldConsumer) {
//		pipelineLogic = aPipelineLogic;
//		mod           = aMod;
//		ce            = aCe;
//		worldConsumer = aWorldConsumer;
//
//		dcg = new DefaultClassGenerator(pipelineLogic.dp);
//	}
//
//	@Override
//	public void run() {
//		final DefaultWorldModule       worldModule = (DefaultWorldModule) mod;
//		final GenerateFunctionsRequest rq          = new GenerateFunctionsRequest(dcg, worldModule);
//
//		worldModule.setRq(rq); // TODO never used
//
//		final Eventual<DeducePhase.GeneratedClasses> plgc = pipelineLogic.handle(rq);
//		plgc.register(pipelineLogic);
//
//		plgc.then(lgc -> {
//			final ICodeRegistrar cr             = dcg.getCodeRegistrar();
//			final ResolvedNodes  resolved_nodes2 = new ResolvedNodes(cr);
//
//			resolved_nodes2.init(lgc);
//			resolved_nodes2.part2();
//			resolved_nodes2.part3(pipelineLogic, mod, lgc);
//
//			worldConsumer.accept(worldModule);
//		});
//
//		_finish();
//	}
//
//	private void _finish() {
//		pipelineLogic.checkFinishEventuals();
//	}
//
//	@Override
//	public <P> void register(final Eventual<P> e) {
//
//	}
//
//	@Override
//	public void checkFinishEventuals() {
//
//	}
//
//	public record GenerateFunctionsRequest(IClassGenerator classGenerator,
//										   DefaultWorldModule worldModule
//	) {
//		public ModuleThing mt() {
//			return worldModule.thing();
//		}
//
//		public OS_Module mod() {
//			return worldModule.module();
//		}
//	}
//
//	@Contract("_ -> new")
//	@SuppressWarnings("unused")
//	public static @NotNull GN_PL_Run2 getFactoryEnv(GN_Env env1) {
//		var env = (GN_PL_Run2_Env) env1;
//		return new GN_PL_Run2(env.pipelineLogic(), env.mod(), env.ce(), env.worldConsumer());
//	}
//}
