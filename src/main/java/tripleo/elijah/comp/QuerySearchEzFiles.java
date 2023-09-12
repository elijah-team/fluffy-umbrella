package tripleo.elijah.comp;

import org.jetbrains.annotations.*;
import tripleo.elijah.ci.*;
import tripleo.elijah.comp.specs.*;
import tripleo.elijah.nextgen.query.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

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

	final FilenameFilter ez_files_filter = new FilenameFilter() {
		@Override
		public boolean accept(final File file, final String s) {
			final boolean matches2 = Pattern.matches(".+\\.ez$", s);
			return matches2;
		}
	};

	// TODO list of operations, not operation of list
	public Operation2<List<CompilerInstructions>> process(final File directory) {
		final List<CompilerInstructions> R    = new ArrayList<>();
		final String[]                   list = directory.list(ez_files_filter);

		if (list != null) {
			for (final String file_name : list) {
				try {
					final File                            file   = new File(directory, file_name);
					final Operation<CompilerInstructions> oci    = parseEzFile(file, file.toString(), errSink, io, c);
					final CompilerInstructions            ezFile = oci.success();
					if (ezFile != null) {
						R.add(ezFile);
					} else {
						errSink.reportError("9995 ezFile is null " + file);
					}
				} catch (final Exception e) {
					errSink.exception(e);
				}
			}
		}
		return Operation2.success(R);
	}

	@NotNull Operation<CompilerInstructions> parseEzFile(final @NotNull File f, final String file_name, final ErrSink errSink, final IO io, final Compilation c) throws Exception {
		System.out.printf("   %s%n", new File(file_name).getAbsolutePath());

		/*final */
		Operation<CompilerInstructions> result;
		final File                      file = new File(file_name);
		final EzSpec                    spec;

		try (final InputStream s = io.readFile(file)) {
			spec   = new EzSpec(file_name, s, file);
			result = cr.realParseEzFile(spec, cr.ezCache());
		} catch (final IOException aE) {
			result = Operation.failure(aE);
		}

		return result;
	}
}
