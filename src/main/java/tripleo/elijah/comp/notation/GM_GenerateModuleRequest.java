package tripleo.elijah.comp.notation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResultEnv;
import tripleo.elijah.stages.gen_generic.OutputFileFactoryParams;
import tripleo.elijah.world.i.WorldModule;

import java.util.function.Supplier;

public record GM_GenerateModuleRequest(@NotNull GN_GenerateNodesIntoSink generateNodesIntoSink,
									   @NotNull WorldModule mod,
									   @NotNull GN_GenerateNodesIntoSinkEnv env) implements GN_Env {
	@Contract("_ -> new")
	public @NotNull GenerateFiles getGenerateFiles(final Supplier<GenerateResultEnv> fgs) {
		var params = params();
		return env.getGenerateFiles(params, params.getWorldMod(), fgs);
	}

	public @NotNull OutputFileFactoryParams params() {
		return env.getParams(mod, generateNodesIntoSink);
	}
}
