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
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.CompilerInstructionsImpl;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.ci.LibraryStatementPartImpl;
import tripleo.elijah.comp.diagnostic.ExceptionDiagnostic;
import tripleo.elijah.comp.diagnostic.FileNotFoundDiagnostic;
import tripleo.elijah.comp.i.*;
import tripleo.elijah.comp.internal.*;
import tripleo.elijah.comp.queries.QuerySourceFileToModule;
import tripleo.elijah.comp.queries.QuerySourceFileToModuleParams;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.stages.deduce.IFunctionMapHook;
import tripleo.elijah.stages.deduce.fluffy.i.FluffyComp;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.world.i.LivingRepo;
import tripleo.elijah.world.impl.DefaultLivingRepo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static tripleo.elijah.nextgen.query.Mode.SUCCESS;

public abstract class Compilation {

	public final  List<OS_Module>                   modules   = new ArrayList<OS_Module>();
	public final  CIS                               _cis      = new CIS();
	public final  USE                               use       = new USE(this);
	final         ErrSink                           errSink;
	final         Map<String, CompilerInstructions> fn2ci     = new HashMap<String, CompilerInstructions>();
	private final Pipeline                          pipelines = new Pipeline();
	private final int                               _compilationNumber;
	private final Map<String, OS_Package>           _packages = new HashMap<String, OS_Package>();
	//public        Stages                            stage     = Stages.O; // Output
	//public        boolean                           silent    = false;
	//public boolean do_out = false;
	//public        boolean                           showTree  = false;
	//
	//
	public        LivingRepo                        _repo     = new DefaultLivingRepo();
	public  CompilationRunner __cr;
	private IPipelineAccess   _pa;
	public  CompilationBus    cb;
	public CompilationConfig cfg = new CompilationConfig();
	CompilerInstructions rootCI;
	private IO             io;
	//
	//
	private int               _packageCode  = 1;
	private int               _classCode    = 101;
	private int               _functionCode = 1001;
	private CompilationEnclosure compilationEnclosure = new CompilationEnclosure(this);

	public Compilation(final ErrSink errSink, final IO io) {
		this.errSink            = errSink;
		this.io                 = io;
		this._compilationNumber = new Random().nextInt(Integer.MAX_VALUE);
	}

	public static ElLog.Verbosity gitlabCIVerbosity() {
		final boolean gitlab_ci = isGitlab_ci();
		return gitlab_ci ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;
	}

	public static boolean isGitlab_ci() {
		return System.getenv("GITLAB_CI") != null;
	}

	public String getProjectName() {
		return rootCI.getName();
	}

	public IO getIO() {
		return io;
	}

	public void setIO(final IO io) {
		this.io = io;
	}

	public abstract @NotNull EOT_OutputTree getOutputTree();

	public abstract @NotNull FluffyComp getFluffy();

	public abstract void fakeFlow(final List<CompilerInput> aInputs, final CompilationFlow aFlow);

	public CompilationClosure getCompilationClosure() {
		return new CompilationClosure() {

			@Override
			public Compilation getCompilation() {
				return Compilation.this;
			}

			@Override
			public ErrSink errSink() {
				return errSink;
			}

			@Override
			public IO io() {
				return io;
			}
		};
	}

	public IPipelineAccess pa() {
		//assert _pa != null;

		if (_pa == null) {
			__cr.crState.ca();
		}

		return _pa;
	}

	public Pipeline getPipelines() {
		return pipelines;
	}

	public void feedCmdLine(final @NotNull List<String> args) throws Exception {
		final PicoContainer      pico       = MainModule.newContainer();
		final CompilerController controller = new DefaultCompilerController();

		if (args.size() == 0) {
			controller.printUsage();
			//System.err.println("Usage: eljc [--showtree] [-sE|O] <directory or .ez file names>");
			return;
		}

		final List<CompilerInput> inputs = args.stream()
				.map(s -> {
					final CompilerInput input = new CompilerInput(s);
					if (s.equals(input.getInp())) {
						input.setSourceRoot();
					}
					return input;
				})
				.collect(Collectors.toList());


		controller._setInputs(this, inputs);
		controller.processOptions();
		controller.runner();
	}

	public void subscribeCI(final Observer<CompilerInstructions> aCio) {
		_cis.subscribe(aCio);
	}

	void hasInstructions(final @NotNull List<CompilerInstructions> cis,
						 final boolean do_out,
						 final @NotNull OptionsProcessor op,
						 final IPipelineAccess pa) throws Exception {
		//assert cis.size() == 1;

		assert cis.size() > 0;

		rootCI = cis.get(0);

		__cr.start(rootCI, do_out, pa);
	}

	public void pushItem(CompilerInstructions aci) {
		_cis.onNext(aci);
	}

	public void addPipeline(PipelineMember aPl) {
		pipelines.add(aPl);
	}

	public void use(final @NotNull CompilerInstructionsImpl compilerInstructions, final boolean do_out) throws Exception {
		use.use(compilerInstructions, do_out);    // NOTE Rust
	}

	@Deprecated
	public int instructionCount() {
		return 4; // TODO shim !!!cis.size();
	}

	public ModuleBuilder moduleBuilder() {
		return new ModuleBuilder(this);
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

	public Operation2<OS_Module> findPrelude(final String prelude_name) {
		return use.findPrelude(prelude_name);
	}

	public void addModule(final OS_Module module, final String fn) {
		modules.add(module);
		use.addModule(module, fn);
	}

	public OS_Package getPackage(final Qualident pkg_name) {
		return _repo.getPackage(pkg_name.toString());
	}

	public OS_Package makePackage(final Qualident pkg_name) {
		return _repo.makePackage(pkg_name);
	}

	public CompilationEnclosure getCompilationEnclosure() {
		return compilationEnclosure;
	}

	public void setCompilationEnclosure(final CompilationEnclosure aCompilationEnclosure) {
		compilationEnclosure = aCompilationEnclosure;
	}

	public IPipelineAccess get_pa() {
		return _pa;
	}

	public void set_pa(IPipelineAccess a_pa) {
		_pa = a_pa;

		compilationEnclosure.pipelineAccessPromise.resolve(_pa);
	}

	public static class CompilationConfig {
		public boolean do_out;
		public Stages  stage    = Stages.O; // Output
		public boolean silent   = false;
		public boolean showTree = false;
	}

	//
	// region MODULE STUFF
	//

	// endregion

	//
	// region PACKAGES
	//

	public boolean isPackage(final String pkg) {
		return _packages.containsKey(pkg);
	}

	private int nextPackageCode() {
		return _packageCode++;
	}

	public int nextClassCode() {
		return _classCode++;
	}

	public int nextFunctionCode() {
		return _functionCode++;
	}

	// endregion

	//
	// region CLASS AND FUNCTION CODES
	//

	public int compilationNumber() {
		return _compilationNumber;
	}

	public String getCompilationNumberString() {
		return String.format("%08x", _compilationNumber);
	}

	//
	// endregion
	//

	//
	// region COMPILATION-SHIT
	//

	public ErrSink getErrSink() {
		return errSink;
	}

	// TODO remove this 04/20
	public void addFunctionMapHook(final IFunctionMapHook aFunctionMapHook) {
		getCompilationEnclosure().getPipelineLogic().dp.addFunctionMapHook(aFunctionMapHook);
	}

	// endregion

	public void eachModule(final Consumer<OS_Module> object) {
		for (OS_Module mod : modules) {
			object.accept(mod);
		}
	}

	static class MainModule {

		public static @NotNull PicoContainer newContainer() {
			final MutablePicoContainer pico = new DefaultPicoContainer();

			pico.addComponent(PicoContainer.class, pico);
			pico.addComponent(OptionsProcessor.class, new ApacheOptionsProcessor());

			//pico.addComponent(CompilerInstructionsObserver.class); // TODO not yet

			//pico.addComponent(InfoWindowProvider.class);
			//pico.addComponent(ShowInfoWindowAction.class);
			//pico.addComponent(ShowInfoWindowButton.class);

			return pico;
		}
	}

	public static class USE {
		private static final FilenameFilter accept_source_files = new FilenameFilter() {
			@Override
			public boolean accept(final File directory, final String file_name) {
				final boolean matches = Pattern.matches(".+\\.elijah$", file_name)
						|| Pattern.matches(".+\\.elijjah$", file_name);
				return matches;
			}
		};
		private final Compilation c;
		private final ErrSink     errSink;
		private final Map<String, OS_Module> fn2m = new HashMap<String, OS_Module>();

		@Contract(pure = true)
		public USE(final Compilation aCompilation) {
			c       = aCompilation;
			errSink = c.getErrSink();
		}

		public void use(final @NotNull CompilerInstructionsImpl compilerInstructions, final boolean do_out) throws Exception {
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
			final LibraryStatementPart lsp = new LibraryStatementPartImpl();
			lsp.setName(Helpers.makeToken("default")); // TODO: make sure this doesn't conflict
			lsp.setDirName(Helpers.makeToken(String.format("\"%s\"", instruction_dir)));
			lsp.setInstructions(compilerInstructions);
			use_internal(instruction_dir, do_out, lsp);
		}

		private void use_internal(final @NotNull File dir, final boolean do_out, LibraryStatementPart lsp) throws Exception {
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

				assert om.mode() == SUCCESS;

				return Operation2.success(om.success());
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

				if (om.mode() == SUCCESS) {
					// TODO we dont know which prelude to find yet
					final Operation2<OS_Module> pl = findPrelude(CompilationAlways.defaultPrelude());

					// NOTE Go. infectious. tedious. also slightly lazy
					assert pl.mode() == SUCCESS;

					final OS_Module mm = om.success();

					if (mm.getLsp() == null) {
						//assert mm.prelude  == null;
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
			} catch (Exception aE) {
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
				if (om.mode() != SUCCESS) {
					final Exception e = om.failure();
					assert e != null;

					System.err.println(("parser exception: " + e));
					e.printStackTrace(System.err);
					s.close();
					return Operation.failure(e);
				}
				final OS_Module R = om.success();
				fn2m.put(absolutePath, R);
				s.close();
				return Operation.success(R);
			} catch (final ANTLRException e) {
				System.err.println(("parser exception: " + e));
				e.printStackTrace(System.err);
				s.close();
				return Operation.failure(e);
			}
		}

		public void addModule(final OS_Module aModule, final String aFn) {
			fn2m.put(aFn, aModule);
		}
	}

	public static class CompilationAlways {
		@NotNull
		public static String defaultPrelude() {
			return "c";
		}

		public class Tokens {
			public static final DriverToken COMPILATION_RUNNER_START       = DriverToken.makeToken("COMPILATION_RUNNER_START");
			public static final DriverToken COMPILATION_RUNNER_FIND_STDLIB = DriverToken.makeToken("COMPILATION_RUNNER_FIND_STDLIB");
		}
	}

	public class CIS implements Observer<CompilerInstructions> {

		private final Subject<CompilerInstructions> compilerInstructionsSubject = ReplaySubject.<CompilerInstructions>create();
		public IProgressSink                ps;
		public CompilerInstructionsObserver _cio;

		@Override
		public void onSubscribe(@NonNull final Disposable d) {
			compilerInstructionsSubject.onSubscribe(d);
		}

		@Override
		public void onNext(@NonNull final CompilerInstructions aCompilerInstructions) {
			compilerInstructionsSubject.onNext(aCompilerInstructions);
		}

		@Override
		public void onError(@NonNull final Throwable e) {
			compilerInstructionsSubject.onError(e);
		}

		@Override
		public void onComplete() {
			throw new IllegalStateException();
			//compilerInstructionsSubject.onComplete();
		}

		public void almostComplete() {
			_cio.almostComplete();
		}

		public void subscribe(final Observer<CompilerInstructions> aCio) {
			compilerInstructionsSubject.subscribe(aCio);
		}
	}

	public void feedInputs(final @NotNull List<CompilerInput> inputs, final CompilerController ctl) {
		if (inputs.size() == 0) {
			ctl.printUsage();
			return;
		}

		if (ctl instanceof DefaultCompilerController) {
			ctl._setInputs(this, inputs);
		//} else if (ctl instanceof UT_Controller uctl) {
		//	uctl._setInputs(this, inputs);
		}

		ctl.processOptions();
		ctl.runner();
	}

}

//
//
//
