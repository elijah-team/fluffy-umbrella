package tripleo.elijah.comp;

import antlr.*;
import org.jetbrains.annotations.*;
import tripleo.elijah.ci.*;
import tripleo.elijah.comp.specs.*;
import tripleo.elijah.nextgen.query.*;
import tripleo.elijjah.*;

import java.io.*;

class CX_ParseEzFile {
	public static Operation<CompilerInstructions> parseAndCache(final EzSpec aSpec, final EzCache aEzCache, final String absolutePath) {
		final Operation<CompilerInstructions> cio = parseEzFile_(aSpec);

		if (cio.mode() == Mode.SUCCESS) {
			aEzCache.put(aSpec, absolutePath, cio.success());
		}

		return cio;
	}

	public static Operation<CompilerInstructions> parseEzFile_(final EzSpec spec) {
		return calculate(spec.f(), spec.s());
	}

	private static Operation<CompilerInstructions> calculate(final String aAbsolutePath, final InputStream aReadFile) {
		final EzLexer lexer = new EzLexer(aReadFile);
		lexer.setFilename(aAbsolutePath);
		final EzParser parser = new EzParser(lexer);
		parser.setFilename(aAbsolutePath);
		try {
			parser.program();
		} catch (final RecognitionException | TokenStreamException aE) {
			return Operation.failure(aE);
		}
		final CompilerInstructions instructions = parser.ci;
		instructions.setFilename(aAbsolutePath);
		return Operation.success(instructions);
	}

	public static Operation<CompilerInstructions> parseEzFile(final @NotNull File aFile, final Compilation aCompilation) {
		try (final InputStream readFile = aCompilation.getIO().readFile(aFile)) {
			final Operation<CompilerInstructions> cio = calculate(aFile.getAbsolutePath(), readFile);
			return cio;
		} catch (final IOException aE) {
			return Operation.failure(aE);
		}
	}

	public static Operation<CompilerInstructions> parseAndCache(final @NotNull File aFile, final Compilation aCompilation, final EzCache aEzCache) {
		try (final InputStream readFile = aCompilation.getIO().readFile(aFile)) {
			final EzSpec                          spec         = new EzSpec(aFile.getName(), readFile, aFile);
			final String                          absolutePath = aFile.getAbsolutePath();
			final Operation<CompilerInstructions> cio          = calculate(aFile.getAbsolutePath(), readFile);

			if (cio.mode() == Mode.SUCCESS) {
				aEzCache.put(spec, absolutePath, cio.success());
			}

			return cio;
		} catch (final IOException aE) {
			return Operation.failure(aE);
		}
	}
}
