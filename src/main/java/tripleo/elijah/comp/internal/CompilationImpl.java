/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp.internal;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.Out;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationShit;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.IO;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.stages.deduce.FunctionMapHook;
import tripleo.elijah.util.Helpers;
import tripleo.elijjah.ElijjahLexer;
import tripleo.elijjah.ElijjahParser;
import tripleo.elijjah.EzLexer;
import tripleo.elijjah.EzParser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CompilationImpl implements Compilation {

	private final int _compilationNumber;
	private IO io;
	private ErrSink eee;
	public final List<OS_Module> modules = new ArrayList<OS_Module>();
	private final Map<String, OS_Module> fn2m = new HashMap<String, OS_Module>();
	private final Map<String, CompilerInstructions> fn2ci = new HashMap<String, CompilerInstructions>();
	private final Map<String, OS_Package> _packages = new HashMap<String, OS_Package>();
	private int _packageCode = 1;
	public final List<CompilerInstructions> cis = new ArrayList<CompilerInstructions>();

	//
	//
	//
//	private PipelineLogic pipelineLogic; // why not final => set by "plugin" ...

//	CompilationShit cs;
//	Registry reg;

	//
	//
	//

	public CompilationImpl(final ErrSink eee, final IO io) {
		this.eee = eee;
		this.io = io;
		this._compilationNumber = new Random().nextInt(Integer.MAX_VALUE);
	}

	@Override
	public void feedCmdLine(final @NotNull List<String> args) throws Exception {
		String[] argl = new String[args.size()];

		int i = 0;
		for (String arg : args) {
			argl[i++] = arg;
		}

		feedCmdLine(argl);
	}

	@Override
	public void feedCmdLine(final String @NotNull [] args) throws Exception /* finally!! */ {
		final ErrSink errSink = getErrSink();

		boolean do_out = false;

		if (args.length < 1) { // Somehow guaranteed never to be negative
			System.err.println("Usage: eljc [--showtree] [-sE|O] <directory or .ez file names>");
			return;
		}

		// 1. build options extractor
		final Options options = new Options();
		options.addOption("s", true, "stage: E: parse; O: output");
		options.addOption("showtree", false, "show tree");
		options.addOption("out", false, "make debug files");
		options.addOption("silent", false, "suppress DeduceType output to console");
		final CommandLineParser clp = new DefaultParser();
		final CommandLine cmd = clp.parse(options, args);

		// 2. extract options
		if (cmd.hasOption("s")) {
			stage = cmd.getOptionValue('s');
		}
		if (cmd.hasOption("showtree")) {
			showTree = true;
		}
		if (cmd.hasOption("out")) {
			do_out = true;
		}
		if (Compilation.isGitlab_ci() || cmd.hasOption("silent")) {
			silent = true;
		}

		CompilerInstructions ez_file = null;
		final String[] args2 = cmd.getArgs();

		// 3. find ezs
		List<Object> k = find_ezs(args2, errSink)
				.stream()
				.map(x -> add_ci2(x))
				.collect(Collectors.toList());
/*
		for (int i = 0; i < args2.length; i++) {
			final String file_name = args2[i];
			final File f = new File(file_name);
			final boolean matches2 = Pattern.matches(".+\\.ez$", file_name);
			if (matches2)
				add_ci(parseEzFile(f, file_name, eee));
			else {
//						eee.reportError("9996 Not an .ez file "+file_name);
				if (f.isDirectory()) {
					final List<CompilerInstructions> ezs = searchEzFiles(f);
					if (ezs.size() > 1) {
//								eee.reportError("9998 Too many .ez files, using first.");
						eee.reportError("9997 Too many .ez files, be specific.");
//								add_ci(ezs.get(0));
					} else if (ezs.size() == 0) {
						eee.reportError("9999 No .ez files found.");
					} else {
						ez_file = ezs.get(0);
						add_ci(ez_file);
					}
				} else
					eee.reportError("9995 Not a directory "+f.getAbsolutePath());
			}
		}
*/
		// 4. find stdlib (wow, this late?)
		System.err.println("130 GEN_LANG: " + cis.get(0).genLang());
		findStdLib("c"); // TODO find a better place for this

		// 5. "use" (build/integrate/...) cis
		for (final CompilerInstructions ci : cis) {
			use(ci, do_out);
		}

		// 6. build-driver logic
		CompilationShit cs = new CompilationShitImpl(this);
		Registry reg = new RegistryImpl(cs, stage, silent);

		reg.loadPlan();
		reg.runPlan();
	}

	@Override
	public IO getIO() {
		return io;
	}

	@Override
	public void setIO(final IO io) {
		this.io = io;
	}

	//
	//
	//

	public String stage = "O"; // Output

	// TODO return list of discrim. union result type (aka remove errSink direct reporting)
	public List<CompilerInstructions> find_ezs(String[] args2, final ErrSink errSink) throws Exception {
		final List<CompilerInstructions> r = new ArrayList<>();

		for (int i = 0; i < args2.length; i++) {
			final String file_name = args2[i];
			final File f = new File(file_name);
			final boolean matches2 = Pattern.matches(".+\\.ez$", file_name);
			if (matches2) {
				final CompilerInstructions ez_file0 = parseEzFile(f, file_name, errSink);
				r.add(ez_file0);
			} else {
//				eee.reportError("9996 Not an .ez file "+file_name);
				if (f.isDirectory()) {
					final List<CompilerInstructions> ezs = searchEzFiles(f);
					if (ezs.size() > 1) {
//						eee.reportError("9998 Too many .ez files, using first.");
						errSink.reportError("9997 Too many .ez files, be specific.");
//						add_ci(ezs.get(0));
					} else if (ezs.isEmpty()) {
						errSink.reportError("9999 No .ez files found.");
					} else {
						final CompilerInstructions ez_file1 = ezs.get(0);
						r.add(ez_file1);
					}
				} else
					errSink.reportError("9995 Not a directory " + f.getAbsolutePath());
			}
		}

		return r;
	}

	public Object add_ci2(final CompilerInstructions ci) {
		add_ci(ci);
		return null;
	}

	private boolean silent = false;

	boolean silent() {
		return silent;
	}

	private List<CompilerInstructions> searchEzFiles(final File directory) {
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
					final File file = new File(directory, file_name);
					final CompilerInstructions ezFile = parseEzFile(file, file.toString(), eee);
					if (ezFile != null)
						R.add(ezFile);
					else
						eee.reportError("9995 ezFile is null " + file.toString());
				} catch (final Exception e) {
					eee.exception(e);
				}
			}
		}
		return R;
	}

	private void add_ci(final CompilerInstructions ci) {
		cis.add(ci);
	}

	public void use(final CompilerInstructions compilerInstructions, final boolean do_out) throws Exception {
		final File instruction_dir = new File(compilerInstructions.getFilename()).getParentFile();
		for (final LibraryStatementPart lsp : compilerInstructions.lsps) {
			final String dir_name = Helpers.remove_single_quotes_from_string(lsp.getDirName());
			final @NotNull File dir;
			if (dir_name.equals(".."))
				dir = instruction_dir/*.getAbsoluteFile()*/.getParentFile();
			else
				dir = new File(instruction_dir, dir_name);
			use_internal(dir, do_out, lsp);
		}
		final LibraryStatementPart lsp = new LibraryStatementPart();
		lsp.setName(Helpers.makeToken("default")); // TODO: make sure this doesn't conflict
		lsp.setDirName(Helpers.makeToken(String.format("\"%s\"", instruction_dir)));
		lsp.setInstructions(compilerInstructions);
		use_internal(instruction_dir, do_out, lsp);
	}

	private void use_internal(final File dir, final boolean do_out, LibraryStatementPart lsp) throws Exception {
		if (!dir.isDirectory()) {
			eee.reportError("9997 Not a directory " + dir.toString());
			return;
		}
		//
		final FilenameFilter accept_source_files = new FilenameFilter() {
			@Override
			public boolean accept(final File directory, final String file_name) {
				final boolean matches = Pattern.matches(".+\\.elijah$", file_name)
						|| Pattern.matches(".+\\.elijjah$", file_name);
				return matches;
			}
		};
		final File[] files = dir.listFiles(accept_source_files);
		if (files != null) {
			for (final File file : files) {
				parseElijjahFile(file, file.toString(), eee, do_out, lsp);
			}
		}
	}

	private CompilerInstructions parseEzFile(final File f, final String file_name, final ErrSink errSink) throws Exception {
		System.out.println((String.format("   %s", f.getAbsolutePath())));
		if (!f.exists()) {
			errSink.reportError(
					"File doesn't exist " + f.getAbsolutePath());
			return null;
		}

		final CompilerInstructions m = realParseEzFile(file_name, io.readFile(f), f);
		{
			String prelude = m.genLang();
			System.err.println("230 " + prelude);
			if (prelude == null) prelude = "c"; // TODO should be java for eljc
		}
		return m;
	}

	private void parseElijjahFile(@NotNull final File f,
	                              final String file_name,
	                              final ErrSink errSink,
	                              final boolean do_out,
	                              LibraryStatementPart lsp) throws Exception {
		System.out.println((String.format("   %s", f.getAbsolutePath())));
		if (f.exists()) {
			final OS_Module m = realParseElijjahFile(file_name, f, do_out);
			m.setLsp(lsp);
			m.prelude = this.findPrelude("c"); // TODO we dont know which prelude to find yet
		} else {
			errSink.reportError(
					"File doesn't exist " + f.getAbsolutePath()); // TODO getPath?? and use a Diagnostic
		}
	}

	public OS_Module realParseElijjahFile(final String f, final File file, final boolean do_out) throws Exception {
		final String absolutePath = file.getCanonicalFile().toString();
		if (fn2m.containsKey(absolutePath)) { // don't parse twice
			return fn2m.get(absolutePath);
		}
		final InputStream s = io.readFile(file);
		try {
			final OS_Module R = parseFile_(f, s, do_out);
			fn2m.put(absolutePath, R);
			s.close();
			return R;
		} catch (final ANTLRException e) {
			System.err.println(("parser exception: " + e));
			e.printStackTrace(System.err);
			s.close();
			return null;
		}
	}

	public CompilerInstructions realParseEzFile(final String f, final InputStream s, final File file) throws Exception {
		final String absolutePath = file.getCanonicalFile().toString();
		if (fn2ci.containsKey(absolutePath)) { // don't parse twice
			return fn2ci.get(absolutePath);
		}
		try {
			final CompilerInstructions R = parseEzFile_(f, s);
			R.setFilename(file.toString());
			fn2ci.put(absolutePath, R);
			s.close();
			return R;
		} catch (final ANTLRException e) {
			System.err.println(("parser exception: " + e));
			e.printStackTrace(System.err);
			s.close();
			return null;
		}
	}

	private OS_Module parseFile_(final String f, final InputStream s, final boolean do_out) throws RecognitionException, TokenStreamException {
		final ElijjahLexer lexer = new ElijjahLexer(s);
		lexer.setFilename(f);
		final ElijjahParser parser = new ElijjahParser(lexer);
		parser.out = new Out(f, this, do_out);
		parser.setFilename(f);
		parser.program();
		final OS_Module module = parser.out.module();
		parser.out = null;
		return module;
	}

	private CompilerInstructions parseEzFile_(final String f, final InputStream s) throws RecognitionException, TokenStreamException {
		final EzLexer lexer = new EzLexer(s);
		lexer.setFilename(f);
		final EzParser parser = new EzParser(lexer);
		parser.setFilename(f);
		parser.program();
		final CompilerInstructions instructions = parser.ci;
		return instructions;
	}

	boolean showTree = false;

	@Override
	public List<ClassStatement> findClass(final String aClassName) {
		final List<ClassStatement> l = new ArrayList<ClassStatement>();
		for (final OS_Module module : modules) {
			if (module.hasClass(aClassName)) {
				l.add((ClassStatement) module.findClass(aClassName));
			}
		}
		return l;
	}

	@Override
	public int errorCount() {
		return eee.errorCount();
	}

	public OS_Module findPrelude(final String prelude_name) {
		final File local_prelude = new File("lib_elijjah/lib-" + prelude_name + "/Prelude.elijjah");
		if (local_prelude.exists()) {
			try {
				return realParseElijjahFile(local_prelude.getName(), local_prelude, false);
			} catch (final Exception e) {
				eee.exception(e);
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean findStdLib(final String prelude_name) {
		final File local_stdlib = new File("lib_elijjah/lib-" + prelude_name + "/stdlib.ez");
		if (local_stdlib.exists()) {
			try {
				final CompilerInstructions ci = realParseEzFile(local_stdlib.getName(), io.readFile(local_stdlib), local_stdlib);
				add_ci(ci);
				return true;
			} catch (final Exception e) {
				eee.exception(e);
			}
		}
		return false;
	}

	//
	// region MODULE STUFF
	//

	@Override
	public void addModule(final OS_Module module, final String fn) {
		modules.add(module);
		fn2m.put(fn, module);
	}

	public OS_Module fileNameToModule(final String fileName) {
		if (fn2m.containsKey(fileName)) {
			return fn2m.get(fileName);
		}
		return null;
	}

	// endregion

	//
	// region CLASS AND FUNCTION CODES
	//

	private int _classCode = 101;
	private int _functionCode = 1001;

	@Override
	public int nextClassCode() {
		return _classCode++;
	}

	@Override
	public int nextFunctionCode() {
		return _functionCode++;
	}

	// endregion

	//
	// region PACKAGES
	//

	@Override
	public boolean isPackage(final String pkg) {
		return _packages.containsKey(pkg);
	}

	@Override
	public OS_Package getPackage(final Qualident pkg_name) {
		return _packages.get(pkg_name.toString());
	}

	@Override
	public OS_Package makePackage(final Qualident pkg_name) {
		if (!isPackage(pkg_name.toString())) {
			final OS_Package newPackage = new OS_Package(pkg_name, nextPackageCode());
			_packages.put(pkg_name.toString(), newPackage);
			return newPackage;
		} else
			return _packages.get(pkg_name.toString());
	}

	private int nextPackageCode() {
		return _packageCode++;
	}

	// endregion

	@Override
	public int compilationNumber() {
		return _compilationNumber;
	}

	@Override
	public String getCompilationNumberString() {
		return String.format("%08x", _compilationNumber);
	}

	@Override
	public ErrSink getErrSink() {
		return eee;
	}

	@Override
	public void addFunctionMapHook(@NotNull FunctionMapHook aFunctionMapHook) {
/*


		Preconditions.checkNotNull(pipelineLogic != null);
		Preconditions.checkNotNull(pipelineLogic.dp != null);

		pipelineLogic.dp.addFunctionMapHook(aFunctionMapHook);
*/
	}

	private EOT_OutputTree _output_tree = null;

	@Override
	@NotNull
	public EOT_OutputTree getOutputTree() {
		if (_output_tree == null) {
			_output_tree = new EOT_OutputTree();
		}

		assert _output_tree != null;

		return _output_tree;
	}
}

//
//
//
