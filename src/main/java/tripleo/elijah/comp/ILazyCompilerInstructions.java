package tripleo.elijah.comp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;

import java.io.File;
import java.util.Objects;

public interface ILazyCompilerInstructions {
	@Contract(value = "_ -> new", pure = true)
	static @NotNull ILazyCompilerInstructions of(final CompilerInstructions aCompilerInstructions) {
		return new ILazyCompilerInstructions() {
			@Override
			public CompilerInstructions get() {
				return aCompilerInstructions;
			}
		};
	}

	@Contract(value = "_, _ -> new", pure = true)
	static @NotNull ILazyCompilerInstructions of(final File aFile, final Compilation c) {
		return new ILazyCompilerInstructions() {
			@Override
			public CompilerInstructions get() {
				try {
					final @NotNull Operation<CompilerInstructions> parsed = c.parseEzFile(aFile);
					return Objects.requireNonNull(parsed).success();
				} catch (Exception aE) {
					throw new RuntimeException(aE); // TODO ugh
				}
			}
		};
	}

	CompilerInstructions get();
}
