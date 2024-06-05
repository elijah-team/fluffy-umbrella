/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.Out;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.comp.functionality.f202.F202;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.stages.deduce.FunctionMapHook;
import tripleo.elijah.stages.logging.ElLog;
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

public class Compilation {
	public final  List<OS_Module>                   modules   = new ArrayList<OS_Module>();
	public final  List<CompilerInstructions>        cis       = new ArrayList<CompilerInstructions>();
	private final int                               _compilationNumber;
	private final Map<String, OS_Module>            fn2m      = new HashMap<String, OS_Module>();
	private final Map<String, CompilerInstructions> fn2ci     = new HashMap<String, CompilerInstructions>();
	private final Map<String, OS_Package>           _packages = new HashMap<String, OS_Package>();
	public        PipelineLogic                     pipelineLogic;
	public        String                            stage     = "O"; // Output
	Pipeline pipelines = new Pipeline();
	boolean  showTree  = false;
	private IO                                    io;
	private ErrSink                               errSink;
	private int                                   _packageCode  = 1;
	private DeferredObject<AccessBus, Void, Void> _p_AccessBus  = new DeferredObject();
	private boolean                               silent        = false;
	private int                                   _classCode    = 101;
	private int                                   _functionCode = 1001;
	private Finally                               _finally;

	public Compilation(final ErrSink aErrSink, final IO aIO) {
		if (aErrSink != null) {
			errSink = aErrSink;
		} else {
			errSink = new StdErrSink();
		}
		io                 = aIO;
		_compilationNumber = new Random().nextInt(Integer.MAX_VALUE);
	}

	public static ElLog.Verbosity gitlabCIVerbosity() {
		final boolean gitlab_ci = isGitlab_ci();
		return gitlab_ci ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;
	}

	public static boolean isGitlab_ci() {
		return System.getenv("GITLAB_CI") != null;
	}

	public void feedCmdLine(final List<String> args) throws Exception {
		try {
			if (!args.isEmpty()) {
				final Options options = new Options();
				options.addOption("s", true, "stage: E: parse; O: output");
				options.addOption("showtree", false, "show tree");
				options.addOption("out", false, "make debug files");
				options.addOption("silent", false, "suppress DeduceType output to console");
				final CommandLineParser clp = new DefaultParser();
				final CommandLine       cmd = clp.parse(options, args.toArray(new String[args.size()]));

				if (cmd.hasOption("s")) {
					stage = cmd.getOptionValue('s');
				}
				if (cmd.hasOption("showtree")) {
					showTree = true;
				}
				if (cmd.hasOption("out")) {
				}
				if (isGitlab_ci() || cmd.hasOption("silent")) {
					silent = true;
				}

				CompilerInstructions ez_file = null;
				final String[]       args2   = cmd.getArgs();

				for (int i = 0; i < args2.length; i++) {
					final String  file_name = args2[i];
					final File    f         = new File(file_name);
					final boolean matches2  = Pattern.matches(".+\\.ez$", file_name);
					if (matches2)
						add_ci(parseEzFile(f, file_name, this.errSink));
					else {
//						eee.reportError("9996 Not an .ez file "+file_name);
						if (f.isDirectory()) {
							final List<CompilerInstructions> ezs = searchEzFiles(f);
							if (ezs.size() > 1) {
//								eee.reportError("9998 Too many .ez files, using first.");
								this.errSink.reportError(9997,  "Too many .ez files, be specific.");
//								add_ci(ezs.get(0));
							} else if (ezs.size() == 0) {
								this.errSink.reportError("9999 No .ez files found.");
							} else {
								ez_file = ezs.get(0);
								add_ci(ez_file);
							}
						} else
							this.errSink.reportError("9995 Not a directory " + f.getAbsolutePath());
					}
				}

				System.err.println("130 GEN_LANG: " + cis.get(0).genLang());
				findStdLib("c"); // TODO find a better place for this

				for (final CompilerInstructions ci : cis) {
					use(ci, false);
				}

				final AccessBus ab1 = new AccessBus(this);
				_p_AccessBus.resolve(ab1);

				if (stage.equals("E")) {
					// do nothing. job over
				} else {
					_p_AccessBus.then(ab -> {
						pipelineLogic = new PipelineLogic(ab);

						_doRun(ab, errSink, pipelineLogic);
					});
				}
			} else {
				System.err.println("Usage: eljc [--showtree] [-sE|O] <directory or .ez file names>");
			}
		} catch (final Exception e) {
			errSink.exception(e);
			throw e;
		}
	}

	private void _doRun(final AccessBus ab, final ErrSink errSink, final PipelineLogic pipelineLogic) {
		ab.addPipelineLogic((bus) -> pipelineLogic);

		final DeducePipeline dpl = new DeducePipeline(ab);
		pipelines.add(dpl);
		final GeneratePipeline gpl = new GeneratePipeline(ab);
		pipelines.add(gpl);
		final WritePipeline wpl = new WritePipeline(ab);
		pipelines.add(wpl);

		try {
			pipelines.run();

			ab.subscribe_PipelineLogic((PipelineLogic pipelineLogic1) -> {
				if (pipelineLogic1 != pipelineLogic) {
					throw new AssertionError("Questioning the meaning of this statement a year and a half later");
				}
				writeLogs(silent, pipelineLogic1.elLogs);
			});
		} catch (Exception e) {
			errSink.exception(e);
		}

		ab.checkFinishEventuals__Fake();
	}

	public IO getIO() {
		return io;
	}

	public void setIO(final IO io) {
		this.io = io;
	}

	private void writeLogs(boolean aSilent, List<ElLog> aLogs) {
		Multimap<String, ElLog> logMap = ArrayListMultimap.create();
		if (true || aSilent) {
			for (ElLog deduceLog : aLogs) {
				logMap.put(deduceLog.getFileName(), deduceLog);
			}
			for (Map.Entry<String, Collection<ElLog>> stringCollectionEntry : logMap.asMap().entrySet()) {
				final F202 f202 = new F202(getErrSink(), this);
				f202.processLogs(stringCollectionEntry.getValue());
			}
		}
	}

	private @NotNull List<CompilerInstructions> searchEzFiles(final @NotNull File directory) {
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
					final CompilerInstructions ezFile = parseEzFile(file, file.toString(), errSink);
					if (ezFile != null)
						R.add(ezFile);
					else
						errSink.reportError("9995 ezFile is null " + file.toString());
				} catch (final Exception e) {
					errSink.exception(e);
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
			File         dir;// = new File(dir_name);
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
			errSink.reportError("9997 Not a directory " + dir.toString());
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
				parseElijjahFile(file, file.toString(), errSink, do_out, lsp);
			}
		}
	}

	private CompilerInstructions parseEzFile(final File f, final String file_name, final ErrSink errSink) throws Exception {
		final String absolutePath = f.getAbsolutePath();

		System.out.println((String.format("   %s", absolutePath)));
		if (!f.exists()) {
			errSink.reportError(
					"File doesn't exist " + absolutePath);
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

	public List<ClassStatement> findClass(final String aClassName) {
		final List<ClassStatement> l = new ArrayList<ClassStatement>();
		for (final OS_Module module : modules) {
			if (module.hasClass(aClassName)) {
				l.add((ClassStatement) module.findClass(aClassName));
			}
		}
		return l;
	}

	public int errorCount() {
		return errSink.errorCount();
	}

	//
	// region MODULE STUFF
	//

	public OS_Module findPrelude(final String prelude_name) {
		final File local_prelude = new File("lib_elijjah/lib-" + prelude_name + "/Prelude.elijjah");
		if (local_prelude.exists()) {
			try {
				return realParseElijjahFile(local_prelude.getName(), local_prelude, false);
			} catch (final Exception e) {
				errSink.exception(e);
				return null;
			}
		}
		return null;
	}

	public boolean findStdLib(final String prelude_name) {
		final File local_stdlib = new File("lib_elijjah/lib-" + prelude_name + "/stdlib.ez");
		if (local_stdlib.exists()) {
			try {
				final CompilerInstructions ci = realParseEzFile(local_stdlib.getName(), io.readFile(local_stdlib), local_stdlib);
				add_ci(ci);
				return true;
			} catch (final Exception e) {
				errSink.exception(e);
			}
		}
		return false;
	}

	// endregion

	//
	// region CLASS AND FUNCTION CODES
	//

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

	public int nextClassCode() {
		return _classCode++;
	}

	public int nextFunctionCode() {
		return _functionCode++;
	}

	// endregion

	//
	// region PACKAGES
	//

	public boolean isPackage(final String pkg) {
		return _packages.containsKey(pkg);
	}

	public OS_Package getPackage(final Qualident pkg_name) {
		return _packages.get(pkg_name.toString());
	}

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

	public int compilationNumber() {
		return _compilationNumber;
	}

	public String getCompilationNumberString() {
		return String.format("%08x", _compilationNumber);
	}

	public ErrSink getErrSink() {
		return errSink;
	}

	public void addFunctionMapHook(FunctionMapHook aFunctionMapHook) {
		pipelineLogic.dp.addFunctionMapHook(aFunctionMapHook);
	}

	public boolean getSilence() {
		return silent;
	}

	public void spi(final Object aObject) {
		if (aObject instanceof AccessBus.AB_LgcListener) {
			AccessBus.AB_LgcListener abl = (AccessBus.AB_LgcListener) aObject;
			_p_AccessBus.then(ab1 -> ab1.subscribe_lgc(abl));
		}
		if (aObject instanceof AccessBus.AB_ModuleListListener) {
			AccessBus.AB_ModuleListListener abm = (AccessBus.AB_ModuleListListener) aObject;
			_p_AccessBus.then(ab1 -> ab1.subscribe_moduleList(abm));
		}
		if (aObject instanceof AccessBus.AB_PipelineLogicListener) {
			AccessBus.AB_PipelineLogicListener abpl = (AccessBus.AB_PipelineLogicListener) aObject;
			_p_AccessBus.then(ab1 -> ab1.subscribe_PipelineLogic(abpl));
		}
		if (aObject instanceof AccessBus.AB_GenerateResultListener) {
			AccessBus.AB_GenerateResultListener abg = (AccessBus.AB_GenerateResultListener) aObject;
			_p_AccessBus.then(ab1 -> ab1.subscribe_GenerateResult(abg));
		}
	}

	public PipelineAdder getPipelineAdder() {
		final Compilation _c = this;
		return new PipelineAdder() {
			@Override
			public void addPipeline(final PipelineMember aPipeline) {
				_c.pipelines.add(aPipeline);
			}
		};
	}

	public Finally reports() {
		if (_finally == null) {
//			_finally = new PartialEscapeClosure.Final()
			_finally = new Finally();
		}
		return _finally;
	}

	public interface PipelineAdder {
		void addPipeline(PipelineMember aPipeline);
	}

	/**
	 * Created 8/21/21 10:09 PM
	 */
	public class Pipeline {
		List<PipelineMember> pls = new ArrayList<>();

		public void add(PipelineMember aPipelineMember) {
			pls.add(aPipelineMember);
		}


		public void run() throws Exception {
			for (PipelineMember pl : pls) {
				pl.run();
			}
		}
	}

	public class Finally {
		public int moduleCount() {
			return modules.size();
		}
	}

}

//
//
//
