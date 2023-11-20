package tripleo.elijah.comp;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.CompilerInstructionsImpl;
import tripleo.elijah.ci.GenerateStatement;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.ci.LibraryStatementPartImpl;
import tripleo.elijah.comp.diagnostic.ExceptionDiagnostic;
import tripleo.elijah.comp.diagnostic.FileNotFoundDiagnostic;
import tripleo.elijah.comp.queries.QuerySourceFileToModule;
import tripleo.elijah.comp.queries.QuerySourceFileToModuleParams;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.StringExpression;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.util.Helpers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

class USE {
	private static final FilenameFilter         accept_source_files = (directory, file_name) -> {
		final boolean matches = Pattern.matches(".+\\.elijah$", file_name)
		  || Pattern.matches(".+\\.elijjah$", file_name);
		return matches;
	};
	private final        Compilation            c;
	private final        ErrSink                errSink;
	private final        Map<String, OS_Module> fn2m                = new HashMap<String, OS_Module>();

	@Contract(pure = true)
	public USE(final Compilation aCompilation) {
		c       = aCompilation;
		errSink = c.getErrSink();
	}

	public void use(final @NotNull CompilerInstructions compilerInstructions, final boolean do_out) throws Exception {
		// TODO

		if (compilerInstructions.getFilename() == null) return;


		final File instruction_dir = new File(compilerInstructions.getFilename()).getParentFile();
		for (final LibraryStatementPart lsp : compilerInstructions.getLibraryStatementParts()) {
			final String dir_name = Helpers.remove_single_quotes_from_string(lsp.getDirName());
			final File   dir;// = new File(dir_name);
			if (dir_name.equals(".."))
				dir = instruction_dir/*.getAbsoluteFile()*/.getParentFile();
			else
				dir = new File(instruction_dir, dir_name);
			use_internal(dir, do_out, lsp);
		}
		final LibraryStatementPart lsp = new LibraryStatementPartImpl();
		lsp.setName(Helpers.makeToken("default")); // TODO: make sure this doesn't conflict
		lsp.setDirName(Helpers.makeToken(String.format("\"%s\"", instruction_dir)));
		lsp.setInstructions(compilerInstructions);
		use_internal(instruction_dir, do_out, lsp);
	}

	private void use_internal(final @NotNull File dir, final boolean do_out, final LibraryStatementPart lsp) throws Exception {
		if (!dir.isDirectory()) {
			errSink.reportError("9997 Not a directory " + dir);
			return;
		}
		//
		final File[] files = dir.listFiles(accept_source_files);
		if (files != null) {
			for (final File file : files) {
				parseElijjahFile(file, file.toString(), do_out, lsp);
			}
		}
	}

	public Operation2<OS_Module> findPrelude(final String prelude_name) {
		final File local_prelude = new File("lib_elijjah/lib-" + prelude_name + "/Prelude.elijjah");

		if (!(local_prelude.exists())) {
			return Operation2.failure(new FileNotFoundDiagnostic(local_prelude));
		}

		try {
			final Operation2<OS_Module> om = realParseElijjahFile2(local_prelude.getName(), local_prelude, false);

			assert om.mode() == Mode.SUCCESS;

			final CompilerInstructions instructions = new CompilerInstructionsImpl();
			instructions.setName("prelude");
			final GenerateStatement generateStatement = new GenerateStatement();
			final StringExpression  expression        = new StringExpression(Helpers.makeToken("\"c\"")); // TODO
			generateStatement.addDirective(Helpers.makeToken("gen"), expression);
			instructions.add(generateStatement);
			final LibraryStatementPart lsp = new LibraryStatementPartImpl();
			lsp.setInstructions(instructions);
//				lsp.setDirName();
			final OS_Module module = om.success();
			module.setLsp(lsp);

			return Operation2.success(module);
		} catch (final Exception e) {
			errSink.exception(e);
			return Operation2.failure(new ExceptionDiagnostic(e));
		}
	}

	private Operation2<OS_Module> parseElijjahFile(final @NotNull File f,
	                                               final @NotNull String file_name,
	                                               final boolean do_out,
	                                               final @NotNull LibraryStatementPart lsp) {
		System.out.printf("   %s%n", f.getAbsolutePath());

		if (f.exists()) {
			final Operation2<OS_Module> om = realParseElijjahFile2(file_name, f, do_out);

			if (om.mode() == Mode.SUCCESS) {
				final OS_Module mm = om.success();

				//assert mm.getLsp() == null;
				//assert mm.prelude == null;

				if (mm.getLsp() == null) {
					// TODO we dont know which prelude to find yet
					final Operation2<OS_Module> pl = findPrelude(Compilation.CompilationAlways.defaultPrelude());

					// NOTE Go. infectious. tedious. also slightly lazy
					assert pl.mode() == Mode.SUCCESS;

					mm.setLsp(lsp);
					mm.prelude = pl.success();
				}

				return Operation2.success(mm);
			} else {
				final Diagnostic e = new UnknownExceptionDiagnostic(om);
				return Operation2.failure(e);
			}
		} else {
			final Diagnostic e = new FileNotFoundDiagnostic(f);

			return Operation2.failure(e);
		}
	}

	public Operation2<OS_Module> realParseElijjahFile2(final String f, final @NotNull File file, final boolean do_out) {
		final Operation<OS_Module> om;

		try {
			om = realParseElijjahFile(f, file, do_out);
		} catch (final Exception aE) {
			return Operation2.failure(new ExceptionDiagnostic(aE));
		}

		switch (om.mode()) {
		case SUCCESS:
			return Operation2.success(om.success());
		case FAILURE:
			final Exception e = om.failure();
			errSink.exception(e);
			return Operation2.failure(new ExceptionDiagnostic(e));
		default:
			throw new IllegalStateException("Unexpected value: " + om.mode());
		}
	}

	private Operation<OS_Module> parseFile_(final String f, final InputStream s, final boolean do_out) throws RecognitionException, TokenStreamException {
		final QuerySourceFileToModuleParams qp = new QuerySourceFileToModuleParams(s, f, do_out);
		return new QuerySourceFileToModule(qp, c).calculate();
	}

	public Operation<OS_Module> realParseElijjahFile(final String f, final @NotNull File file, final boolean do_out) throws Exception {
		final String absolutePath = file.getCanonicalFile().toString();
		if (fn2m.containsKey(absolutePath)) { // don't parse twice
			final OS_Module m = fn2m.get(absolutePath);
			return Operation.success(m);
		}

		final IO io = c.getIO();

		// tree add something

		final InputStream s = io.readFile(file);
		try {
			final Operation<OS_Module> om = parseFile_(f, s, do_out);
			if (om.mode() != Mode.SUCCESS) {
				final Exception e = om.failure();
				assert e != null;

				tripleo.elijah.util.Stupidity.println_err2(("parser exception: " + e));
				e.printStackTrace(System.err);
				s.close();
				return Operation.failure(e);
			}
			final OS_Module R = om.success();
			fn2m.put(absolutePath, R);
			s.close();
			return Operation.success(R);
		} catch (final ANTLRException e) {
			tripleo.elijah.util.Stupidity.println_err2(("parser exception: " + e));
			e.printStackTrace(System.err);
			s.close();
			return Operation.failure(e);
		}
	}
}
