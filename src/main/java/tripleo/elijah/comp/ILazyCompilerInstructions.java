package tripleo.elijah.comp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.util.Operation;

import java.io.File;
import java.util.Objects;

public interface ILazyCompilerInstructions {
	@Contract(value = "_ -> new", pure = true)
	static @NotNull ILazyCompilerInstructions of(final CompilerInstructions aCompilerInstructions) {
		return () -> aCompilerInstructions;
	}

	@Contract(value = "_, _ -> new", pure = true)
	static @NotNull ILazyCompilerInstructions of(final File aFile, final Compilation c) {
		return new ILazyCompilerInstructions() {
			@Override
			public CompilerInstructions get() {
				try {
					final Operation<CompilerInstructions> parsed = CX_ParseEzFile.parseAndCache(aFile, c, c.__cr.ezCache());
					return Objects.requireNonNull(parsed).success();
				} catch (final Exception aE) {
					throw new RuntimeException(aE); // TODO ugh
				}
			}
		};
	}

	CompilerInstructions get();
}
