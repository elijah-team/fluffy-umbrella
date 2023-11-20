package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class QuerySearchEzFiles {
	private final Compilation       c;
	private final ErrSink           errSink;
	private final IO                io;
	private final CompilationRunner cr;

	public QuerySearchEzFiles(final Compilation aC, final ErrSink aErrSink, final IO aIo, final CompilationRunner aCr) {
		c       = aC;
		errSink = aErrSink;
		io      = aIo;
		cr      = aCr;
	}

	public Operation2<List<CompilerInstructions>> process(final File directory) {
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
		return cr.realParseEzFile(f, s, file, c);
	}
}
