/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.comp.functionality.f202.F202;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleInput;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.FunctionMapHook;
import tripleo.elijah.stages.deduce.fluffy.i.FluffyComp;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.ut.UT_Controller;
import tripleo.elijah.util.CompletableProcess;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.ObservableCompletableProcess;
import tripleo.elijah.util.Operation2;
import tripleo.elijah.world.i.LivingRepo;
import tripleo.elijah.world.i.WorldModule;
import tripleo.elijah.world.impl.DefaultLivingRepo;
import tripleo.elijah.world.impl.DefaultWorldModule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Compilation {

	public static class CIS {
		private final ObservableCompletableProcess<CompilerInstructions> ocp = new ObservableCompletableProcess<>();
		private       CompilerInstructionsObserver                       _cio;

		public void almostComplete() {
			ocp.almostComplete();
		}

//		@Override
		public void onComplete() {
			ocp.onComplete();
		}

//		@Override
		public void onError(@NonNull final Throwable e) {
			ocp.onError(e);
		}

//		@Override
		public void onNext(@NonNull final CompilerInstructions aCompilerInstructions) {
			ocp.onNext(aCompilerInstructions);
		}

		public void onSubscribe(@NonNull final Disposable d) {
			ocp.onSubscribe(d);
		}

		public void set_cio(final CompilerInstructionsObserver aCompilerInstructionsObserver) {
			_cio = aCompilerInstructionsObserver;
		}

		public void subscribe(final CompletableProcess<CompilerInstructions> aCompletableProcess) {
			ocp.subscribe(aCompletableProcess);
		}

		public void subscribe(final Observer<CompilerInstructions> aCio) {
			ocp.subscribe(aCio);
		}
	}
	public static class CompilationAlways {
		public static @NotNull String defaultPrelude() {
			return "c";
		}
	}
	//
	//
	//
	static class CompilationConfig {
		public    boolean do_out;
		public    Stages  stage  = Stages.O; // Output
		protected boolean silent = false;
		boolean showTree = false;
	}
	class InstructionDoer implements CompletableProcess<CompilerInstructions> {
		CompilerInstructions root;

		@Override
		public void add(final CompilerInstructions item) {
			if (root == null) {
				root = item;
				try {
					rootCI = root;

					__cr.start(rootCI, cfg.do_out);
				} catch (Exception aE) {
					throw new RuntimeException(aE);
				}
			} else {
				System.err.println("second: "+ item.getFilename());

				var do_out = false;
				var compilation = __cr.c();

				try {
					if (false)
						compilation.use(item, do_out);
				} catch (Exception aE) {
					throw new RuntimeException(aE);
				}
			}
		}

		@Override
		public void complete() {
			System.err.println("InstructionDoer::complete");
		}

		@Override
		public void error(final Diagnostic d) {
			System.err.println("InstructionDoer::error");
		}

		@Override
		public void preComplete() {
			System.err.println("InstructionDoer::preComplete");
		}

		@Override
		public void start() {
			System.err.println("InstructionDoer::start");
		}
	}
	static class MOD {
		final         List<OS_Module>        modules = new ArrayList<OS_Module>();
		private final Map<String, OS_Module> fn2m    = new HashMap<String, OS_Module>();
		private final Compilation            c;

		public MOD(final Compilation aCompilation) {
			c = aCompilation;
		}

		public void addModule(final OS_Module module, final String fn) {
			modules.add(module);
			fn2m.put(fn, module);

			System.err.println("338 "+module.getFileName());
			c.reports().addInput(module::getFileName, Finally.Out2.ELIJAH);
		}

		public List<OS_Module> modules() {
			return modules;
		}

		public int size() {
			return modules.size();
		}
	}
	public static ElLog.Verbosity gitlabCIVerbosity() {
		final boolean gitlab_ci = isGitlab_ci();
		return gitlab_ci ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;
	}
	public static boolean isGitlab_ci() {
		return System.getenv("GITLAB_CI") != null;
	}
	public final  List<ElLog>          elLogs = new LinkedList<ElLog>();
	public final CompilationConfig cfg   = new CompilationConfig();
	public final CIS               _cis  = new CIS();
	//
	public final DefaultLivingRepo _repo = new DefaultLivingRepo();
	//
	final         MOD                  mod    = new MOD(this);
	private final Pipeline             pipelines;
	private final int                  _compilationNumber;

	private final ErrSink              errSink;
	private final IO                   io;
	private final USE                  use    = new USE(this);

	//
	//
	//
	public        PipelineLogic        pipelineLogic;

	public        CompilationRunner    __cr;


	private       CompilerInstructions rootCI;

	private final   CompFactory                       _con    = new CompFactory() {
		@Override
		public @NotNull InputRequest createInputRequest(final File aFile, final boolean aDo_out, final @Nullable LibraryStatementPart aLsp) {
			return new InputRequest(aFile, aDo_out, aLsp);
		}

		@Override
		public @NotNull EIT_ModuleInput createModuleInput(final OS_Module aModule) {
			return new EIT_ModuleInput(aModule, Compilation.this);
		}

		@Override
		public @NotNull Qualident createQualident(final @NotNull List<String> sl) {
			var R = new Qualident();
			for (String s : sl) {
				R.append(Helpers.string_to_ident(s));
			}
			return R;
		}

		@Override
		public @NotNull WorldModule createWorldModule(final OS_Module m) {
			CompilationEnclosure ce = getCompilationEnclosure();
			final WorldModule    R  = new DefaultWorldModule(m, ce);
			return R;
		}
	};

	private final Finally      _f;

	private final Finally.Flow _flow;

	private       LivingRepo _world;

	private final CompilationEnclosure ce = new CompilationEnclosure(this);

	final InstructionDoer id = new InstructionDoer();

	public Compilation(final @NotNull ErrSink aErrSink, final IO aIO) {
		errSink            = aErrSink;
		io                 = aIO;
		_compilationNumber = new Random().nextInt(Integer.MAX_VALUE);
		pipelines          = new Pipeline(aErrSink);

		_f                 = new Finally();
		_flow              = _f.flow();
	}

	public void addFunctionMapHook(final FunctionMapHook aFunctionMapHook) {
		getDeducePhase().addFunctionMapHook(aFunctionMapHook);
	}

	//
	//
	//

	public void addModule(final OS_Module module, final String fn) {
		mod.addModule(module, fn);
	}

	public int compilationNumber() {
		return _compilationNumber;
	}

	public int errorCount() {
		return errSink.errorCount();
	}

	public void feedCmdLine(final @NotNull List<String> args) {
		feedCmdLine(args, new DefaultCompilerController());
	}

	public void feedCmdLine(final List<String> args, final CompilerController ctl) {
		if (args.size() == 0) {
			ctl.printUsage();
			return; // ab
		}

		if (ctl instanceof DefaultCompilerController) {
			((DefaultCompilerController) ctl)._set(this, args);
		} else if (ctl instanceof final UT_Controller uctl) {
			uctl._set(this, args);
		}

		ctl.processOptions();
		ctl.runner();
	}

//	public void setIO(final IO io) {
//		this.io = io;
//	}

	public OS_Module fileNameToModule(final String fileName) {
		if (mod.fn2m.containsKey(fileName)) {
			return mod.fn2m.get(fileName);
		}
		return null;
	}

	public List<ClassStatement> findClass(final String string) {
		final List<ClassStatement> l = new ArrayList<ClassStatement>();
		for (final OS_Module module : mod.modules) {
			if (module.hasClass(string)) {
				l.add((ClassStatement) module.findClass(string));
			}
		}
		return l;
	}

	//
	// region MODULE STUFF
	//

	public Operation2<OS_Module> findPrelude(final String prelude_name) {
		return use.findPrelude(prelude_name);
	}

	public Finally.Flow flow() {
		return _flow;
	}

	// endregion

	//
	// region CLASS AND FUNCTION CODES
	//

	public CompilationEnclosure getCompilationEnclosure() {
		return ce;
	}

	public String getCompilationNumberString() {
		return String.format("%08x", _compilationNumber);
	}

	public @NotNull DeducePhase getDeducePhase() {
		// TODO subscribeDeducePhase??
		return pipelineLogic.dp;
	}

	public ErrSink getErrSink() {
		return errSink;
	}

	public abstract @NotNull FluffyComp getFluffy();

	// endregion

	//
	// region PACKAGES
	//

	public IO getIO() {
		return io;
	}

	public @NotNull List<EvaNode> getLGC() {
		return getDeducePhase().generatedClasses.copy();
	}

	// endregion

	public abstract @NotNull EOT_OutputTree getOutputTree();

	public OS_Package getPackage(final Qualident pkg_name) {
		return _repo.getPackage(pkg_name.toString());
	}

	public Pipeline getPipelines() {
		return pipelines;
	}

	public String getProjectName() {
		return rootCI.getName();
	}

	public boolean getSilence() {
		return cfg.silent;
	}

	public boolean isPackage(final String aPackageName) {
		return _repo.isPackage(aPackageName);
	}

	public OS_Package makePackage(final Qualident pkg_name) {
		return _repo.makePackage(pkg_name);
	}

	public ModuleBuilder moduleBuilder() {
		return new ModuleBuilder(this);
	}

	@Deprecated
	public int modules_size() {
		return mod.size();
	}

	public int nextClassCode() {
		return _repo.nextClassCode();
	}

	public int nextFunctionCode() {
		return _repo.nextFunctionCode();
	}

	public void pushItem(final CompilerInstructions aci) {
		_cis.onNext(aci);
	}

	public OS_Module realParseElijjahFile(final String f, final @NotNull File file, final boolean do_out) throws Exception {
		return use.realParseElijjahFile(f, file, do_out).success();
	}

	public Finally reports() {
		return _f;
	}

	public void use(final @NotNull CompilerInstructions compilerInstructions, final boolean do_out) throws Exception {
		use.use(compilerInstructions, do_out);    // NOTE Rust
	}

	public LivingRepo world() {
		if (_world == null)
			_world = new DefaultLivingRepo();
		return _world;
	}

	void writeLogs(final boolean aSilent, final @NotNull List<ElLog> aLogs) {
		final Multimap<String, ElLog> logMap = ArrayListMultimap.create();
		if (true) {
			for (final ElLog deduceLog : aLogs) {
				logMap.put(deduceLog.getFileName(), deduceLog);
			}
			for (final Map.Entry<String, Collection<ElLog>> stringCollectionEntry : logMap.asMap().entrySet()) {
				final F202 f202 = new F202(getErrSink(), this);
				f202.processLogs(stringCollectionEntry.getValue());
			}
		}
	}
}

//
//
//
