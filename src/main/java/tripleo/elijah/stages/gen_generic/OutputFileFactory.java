package tripleo.elijah.stages.gen_generic;

import tripleo.elijah.util.NotImplementedException;

public final class OutputFileFactory {
	private OutputFileFactory() {
	}

//	@Contract("_, _ -> new")
//	public static @NotNull GenerateFiles create(final @NotNull String lang,
//	                                            final @NotNull OutputFileFactoryParams params) {
//		if (Objects.equals(lang, "c")) {
//			return new GenerateC(params);
//		} else
//			throw new NotImplementedException();
//	}

	public static GenerateFiles create(final String aS, final OutputFileFactoryParams aParams, final GenerateResultEnv aFileGen) {
		throw new NotImplementedException();

	}
}
