package tripleo.elijah.comp;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.jetbrains.annotations.NotNull;

import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.internal.PCon;
import tripleo.elijah.comp.specs.EzCache;
import tripleo.elijah.comp.specs.EzSpec;
import tripleo.elijah.nextgen.query.Mode;

import tripleo.elijjah.EzLexer;
import tripleo.elijjah.EzParser;

import tripleo.wrap.File;

//import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
		parser.pcon = new PCon();
		parser.ci = pcon.new _
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
			var s = aFile.toString();
			final String                          absolutePath = s;//aFile.getAbsolutePath();
			final Operation<CompilerInstructions> cio          = calculate(absolutePath, readFile);

			if (cio.mode() == Mode.SUCCESS) {
				aEzCache.put(spec, absolutePath, cio.success());
			}

			return cio;
		} catch (final IOException aE) {
			return Operation.failure(aE);
		}
	}
}
