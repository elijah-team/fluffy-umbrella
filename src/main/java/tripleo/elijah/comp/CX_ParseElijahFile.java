package tripleo.elijah.comp;

import antlr.*;
import tripleo.elijah.*;
import tripleo.elijah.comp.specs.*;
import tripleo.elijah.lang.*;
import tripleo.elijah.nextgen.query.*;
import tripleo.elijjah.*;

import java.io.*;

public class CX_ParseElijahFile {

	public static Operation<OS_Module> parseAndCache(final ElijahSpec aSpec, final ElijahCache aElijahCache, final String absolutePath, final Compilation compilation) {
		final Operation<OS_Module> calm;
		try {
			calm = parseElijahFile_(aSpec, compilation, aElijahCache, new File(absolutePath), compilation);
		} catch (final IOException aE) {
			return Operation.failure(aE);
		}

		if (calm.mode() == Mode.SUCCESS) {
			aElijahCache.put(aSpec, absolutePath, calm.success());
		}

		return calm;
	}

	private static Operation<OS_Module> parseElijahFile_(final ElijahSpec spec,
	                                                     final Compilation aCompilation,
	                                                     final ElijahCache aElijahCache,
	                                                     final File      file   ,
	                                                     final Compilation c) throws IOException {
		final IO io = aCompilation.getIO();

		// tree add something

		final String    f      = spec.f();
		final boolean   do_out = spec.do_out();
		final OS_Module R;

		try (final InputStream s = io.readFile(file)) {
			final String absolutePath = file.getCanonicalPath();

			final Operation<OS_Module> om = parseElijahFile(f, s, do_out, c, absolutePath);
			if (om.mode() == Mode.FAILURE) {
				final Exception e = om.failure();
				assert e != null;

				tripleo.elijah.util.Stupidity.println_err2(("parser exception: " + e));
				e.printStackTrace(System.err);
				s.close();
				return Operation.failure(e);
			}
			R = om.success();
		}
		return Operation.success(R);
	}

	public static Operation<OS_Module> parseElijahFile(final String f, final InputStream s, final boolean do_out, final Compilation compilation, final String absolutePath) {
		final ElijjahLexer lexer = new ElijjahLexer(s);
		lexer.setFilename(f);
		final ElijjahParser parser = new ElijjahParser(lexer);
		parser.out = new Out(f, compilation, do_out);
		parser.setFilename(f);
		try {
			parser.program();
		} catch (final RecognitionException | TokenStreamException aE) {
			return Operation.failure(aE);
		}
		final OS_Module module = parser.out.module();
		parser.out = null;

		module.setFileName(absolutePath);
		return Operation.success(module);
	}

	private static Operation<OS_Module> parseElijahFile(final ElijahSpec spec, final Compilation compilation) {
		final var absolutePath = new File(spec.f()).getAbsolutePath(); // !!
		return parseElijahFile(spec.f(), spec.s(), spec.do_out(), compilation, absolutePath);
	}
}
