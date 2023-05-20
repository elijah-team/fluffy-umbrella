package tripleo.elijah.comp.queries;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.CompilationClosure;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static tripleo.elijah.nextgen.query.Mode.FAILURE;
import static tripleo.elijah.nextgen.query.Mode.SUCCESS;

public class QuerySearchEzFiles {
	private final Compilation       c;
	private final ErrSink           errSink;
	private final IO                io;

	public QuerySearchEzFiles(final @NotNull CompilationClosure ccl) {
		c       = ccl.getCompilation();
		errSink = ccl.errSink();
		io      = ccl.io();
	}

	public Operation2<List<CompilerInstructions>> process(final @NotNull File directory) {
		final List<CompilerInstructions> R = new ArrayList<CompilerInstructions>();
		final FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(final File file, final String s) {
				final boolean matches2 = Pattern.matches(".+\\.ez$", s);
				return matches2;
			}
		};
		final String[] list = directory.list(filter);
		if (list != null) {
			for (final String file_name : list) {
				try {
					final File                 file   = new File(directory, file_name);
					final CompilerInstructions ezFile = parseEzFile(file, file.toString(), errSink, io, c);
					if (ezFile != null)
						R.add(ezFile);
					else
						errSink.reportError("9995 ezFile is null " + file);
				} catch (final Exception e) {
					errSink.exception(e);
				}
			}
		}
		return Operation2.success(R);
	}

	@Nullable CompilerInstructions parseEzFile(final @NotNull File f, final String file_name, final ErrSink errSink, final IO io, final Compilation c) throws Exception {
		final Operation<CompilerInstructions> om = parseEzFile1(f, file_name, errSink, io, c);

		final CompilerInstructions m;

		if (om.mode() == Mode.SUCCESS) {
			m = om.success();

/*
	final String prelude;
	final String xprelude = m.genLang();
	tripleo.elijah.util.Stupidity.println_err2("230 " + prelude);
	if (xprelude == null)
		prelude = CompilationAlways.defaultPrelude(); // TODO should be java for eljc
	else
		prelude = null;
*/
		} else {
			m = null;
		}

		return m;
	}

	public @NotNull Operation<CompilerInstructions> parseEzFile1(final @NotNull File f,
																 final String file_name,
																 final ErrSink errSink,
																 final IO io,
																 final Compilation c) {
		System.out.printf("   %s%n", f.getAbsolutePath());
		if (!f.exists()) {
			errSink.reportError(
					"File doesn't exist " + f.getAbsolutePath());
			return null;
		} else {
			final Operation<CompilerInstructions> om = realParseEzFile(file_name/*, io.readFile(f), f*/, io, c);
			return om;
		}
	}

	private Operation<CompilerInstructions> realParseEzFile(final String f, final @NotNull IO io, final Compilation c) {
		final File file = new File(f);

		try {
			return realParseEzFile(f, io.readFile(file), file, c);
		} catch (final FileNotFoundException aE) {
			return Operation.failure(aE);
		}
	}


	public Operation<CompilerInstructions> realParseEzFile(final String f,
														   final InputStream s,
														   final @NotNull File file,
														   final Compilation c) {
		final String absolutePath;
		try {
			absolutePath = file.getCanonicalFile().toString(); // TODO 04/10 hash this and "attach"
			//queryDB.attach(compilerInput, new EzFileIdentity_Sha256($hash)); // ??
		} catch (IOException aE) {
			//throw new RuntimeException(aE);
			return Operation.failure(aE);
		}

		// TODO 04/10
		// Cache<CompilerInput, CompilerInstructions> fn2ci /*EzFileIdentity??*/(MAP/*??*/, resolver is try stmt)
		if (c.fn2ci.containsKey(absolutePath)) { // don't parse twice
			// TODO 04/10
			// ...queryDB.attach(compilerInput, new EzFileIdentity_Sha256($hash)); // ?? fnci
			return Operation.success(c.fn2ci.get(absolutePath));
		}

		try {
			try {
				final Operation<CompilerInstructions> cio = parseEzFile_(f, s);

				if (cio.mode() != SUCCESS) {
					final Exception e = cio.failure();
					assert e != null;

					tripleo.elijah.util.Stupidity.println_err_2(("parser exception: " + e));
					e.printStackTrace(System.err);
					//s.close();
					return cio;
				}

				final CompilerInstructions R = cio.success();
				R.setFilename(file.toString());
				c.fn2ci.put(absolutePath, R);
				return cio;
			} catch (final ANTLRException e) {
				tripleo.elijah.util.Stupidity.println_err_2(("parser exception: " + e));
				e.printStackTrace(System.err);
				return Operation.failure(e);
			}
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException aE) {
					// TODO return inside finally: is this ok??
					return new Operation<>(null, aE, FAILURE);
				}
			}
		}
	}

	private Operation<CompilerInstructions> parseEzFile_(final String f, final InputStream s) throws RecognitionException, TokenStreamException {
		final QueryEzFileToModuleParams qp = new QueryEzFileToModuleParams(f, s);
		return new QueryEzFileToModule(qp).calculate();
	}
}
