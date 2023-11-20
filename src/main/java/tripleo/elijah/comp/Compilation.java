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
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.functionality.f202.F202;
import tripleo.elijah.comp.queries.QueryEzFileToModule;
import tripleo.elijah.comp.queries.QueryEzFileToModuleParams;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.FunctionMapHook;
import tripleo.elijah.stages.deduce.fluffy.i.FluffyComp;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.ut.UT_Controller;
import tripleo.elijah.world.impl.DefaultLivingRepo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Compilation {

	public final  List<ElLog>          elLogs = new LinkedList<ElLog>();
	public final  CompilationConfig    cfg    = new CompilationConfig();
	public final  CIS                  _cis   = new CIS();
	private final Pipeline             pipelines;
	private final int                  _compilationNumber;
	private final ErrSink              errSink;
	//
	public final  DefaultLivingRepo    _repo  = new DefaultLivingRepo();
	//
	final         MOD                  mod    = new MOD(this);
	private final IO                   io;
	private final USE                  use    = new USE(this);
	//
	//
	//
	public        PipelineLogic        pipelineLogic;
	public        CompilationRunner    __cr;
	private       CompilerInstructions rootCI;

	public Compilation(final ErrSink aErrSink, final IO aIO) {
		errSink            = aErrSink;
		io                 = aIO;
		_compilationNumber = new Random().nextInt(Integer.MAX_VALUE);
		pipelines          = new Pipeline(aErrSink);
	}

	void hasInstructions(final @NotNull List<CompilerInstructions> cis) throws Exception {
		assert cis.size() > 0;

		rootCI = cis.get(0);

		__cr.start(rootCI, cfg.do_out);
	}

	public void feedCmdLine(final @NotNull List<String> args) {
		feedCmdLine(args, new DefaultCompilerController());
	}

	public String getProjectName() {
		return rootCI.getName();
	}

	public static ElLog.Verbosity gitlabCIVerbosity() {
		final boolean gitlab_ci = isGitlab_ci();
		return gitlab_ci ? ElLog.Verbosity.SILENT : ElLog.Verbosity.VERBOSE;
	}

	public OS_Module realParseElijjahFile(final String f, final @NotNull File file, final boolean do_out) throws Exception {
		return use.realParseElijjahFile(f, file, do_out).success();
	}

	public Operation<CompilerInstructions> parseEzFile(final @NotNull File aFile) {
		try {
			final QueryEzFileToModuleParams       params = new QueryEzFileToModuleParams(aFile.getAbsolutePath(), io.readFile(aFile));
			final Operation<CompilerInstructions> x      = new QueryEzFileToModule(params).calculate();
			return x;
		} catch (final FileNotFoundException aE) {
			return Operation.failure(aE);
		}
	}

	public void pushItem(final CompilerInstructions aci) {
		_cis.onNext(aci);
	}

	//
	//
	//

	public List<ClassStatement> findClass(final String string) {
		final List<ClassStatement> l = new ArrayList<ClassStatement>();
		for (final OS_Module module : mod.modules) {
			if (module.hasClass(string)) {
				l.add((ClassStatement) module.findClass(string));
			}
		}
		return l;
	}

	public static boolean isGitlab_ci() {
		return System.getenv("GITLAB_CI") != null;
	}

	public void use(final @NotNull CompilerInstructions compilerInstructions, final boolean do_out) throws Exception {
		use.use(compilerInstructions, do_out);    // NOTE Rust
	}

	public int errorCount() {
		return errSink.errorCount();
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

	public IO getIO() {
		return io;
	}

//	public void setIO(final IO io) {
//		this.io = io;
//	}

	public void addModule(final OS_Module module, final String fn) {
		mod.addModule(module, fn);
	}

    public OS_Module fileNameToModule(final String fileName) {
	    if (mod.fn2m.containsKey(fileName)) {
		    return mod.fn2m.get(fileName);
	    }
	    return null;
    }

	//
	// region MODULE STUFF
	//

	public ErrSink getErrSink() {
		return errSink;
	}

	public boolean getSilence() {
		return cfg.silent;
	}

	// endregion

	//
	// region CLASS AND FUNCTION CODES
	//

	public Operation2<OS_Module> findPrelude(final String prelude_name) {
		return use.findPrelude(prelude_name);
	}

	public void addFunctionMapHook(final FunctionMapHook aFunctionMapHook) {
		getDeducePhase().addFunctionMapHook(aFunctionMapHook);
	}

	public @NotNull DeducePhase getDeducePhase() {
		// TODO subscribeDeducePhase??
		return pipelineLogic.dp;
	}

	public int nextClassCode() {
		return _repo.nextClassCode();
	}

	public int nextFunctionCode() {
		return _repo.nextFunctionCode();
	}

	// endregion

	//
	// region PACKAGES
	//

	public OS_Package getPackage(final Qualident pkg_name) {
		return _repo.getPackage(pkg_name.toString());
	}

	public OS_Package makePackage(final Qualident pkg_name) {
		return _repo.makePackage(pkg_name);
	}

	// endregion

	public int compilationNumber() {
		return _compilationNumber;
	}

	public String getCompilationNumberString() {
		return String.format("%08x", _compilationNumber);
	}

	@Deprecated
	public int modules_size() {
		return mod.size();
	}

	@NotNull
	public abstract EOT_OutputTree getOutputTree();

	public abstract @NotNull FluffyComp getFluffy();

	public @NotNull List<GeneratedNode> getLGC() {
		return getDeducePhase().generatedClasses.copy();
	}

	public boolean isPackage(final String aPackageName) {
		return _repo.isPackage(aPackageName);
	}

	public void feedCmdLine(final List<String> args, final CompilerController ctl) {
		if (args.size() == 0) {
			ctl.printUsage();
			return; // ab
		}

		if (ctl instanceof DefaultCompilerController) {
			((DefaultCompilerController) ctl)._set(this, args);
		} else if (ctl instanceof UT_Controller) {
			UT_Controller uctl = (UT_Controller) ctl;
			uctl._set(this, args);
		}

		ctl.processOptions();
		ctl.runner();
	}

	static class MOD {
		final         List<OS_Module>        modules = new ArrayList<OS_Module>();
		private final Map<String, OS_Module> fn2m    = new HashMap<String, OS_Module>();
//		private final Compilation            c;

		public MOD(final Compilation aCompilation) {
//			c = aCompilation;
		}

		public void addModule(final OS_Module module, final String fn) {
			modules.add(module);
			fn2m.put(fn, module);
		}

		public int size() {
			return modules.size();
		}

		public List<OS_Module> modules() {
			return modules;
		}
	}

	//
	//
	//
	static class CompilationConfig {
		public    boolean do_out;
		public Stages stage = Stages.O; // Output
		protected boolean silent = false;
		boolean showTree = false;
	}

	public Pipeline getPipelines() {
		return pipelines;
	}

	static class CIS implements Observer<CompilerInstructions> {

		private final Subject<CompilerInstructions> compilerInstructionsSubject = ReplaySubject.create();
		CompilerInstructionsObserver _cio;

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

	public ModuleBuilder moduleBuilder() {
		return new ModuleBuilder(this);
	}

	public static class CompilationAlways {
		public static boolean VOODOO = false;

		@NotNull
		public static String defaultPrelude() {
			return "c";
		}
	}

}

//
//
//
