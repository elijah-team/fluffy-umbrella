package tripleo.elijah.comp.i;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.apache.commons.lang3.tuple.Pair;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.AccessBus;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationRunner;
import tripleo.elijah.comp.CompilerInput;
import tripleo.elijah.comp.ICompilationAccess;
import tripleo.elijah.comp.ICompilationBus;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.comp.internal.NotableAction;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.comp.internal.Provenance;
import tripleo.elijah.comp.notation.GN_Env;
import tripleo.elijah.comp.notation.GN_Notable;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.reactive.Reactivable;
import tripleo.elijah.nextgen.reactive.Reactive;
import tripleo.elijah.nextgen.reactive.ReactiveDimension;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.inter.ModuleThing;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.CompletableProcess;
import tripleo.elijah.util.Eventual;
import tripleo.elijah.world.i.WorldModule;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CompilationEnclosure {
	public interface ModuleListener {
		void close();

		void listen(WorldModule module);
	}

	private final @NotNull Map<Provenance, Pair<Class, Class>> installs = new HashMap<>();
//	public final  DeferredObject<IPipelineAccess, Void, Void> pipelineAccessPromise = new DeferredObject<>();
//	private final CB_Output                                   _cbOutput             = new CB_ListBackedOutput();
	private final Compilation                           compilation;
	private final DeferredObject<AccessBus, Void, Void> accessBusPromise   = new DeferredObject<>();
	private final Map<OS_Module, ModuleThing>           moduleThings       = new HashMap<>();
	private final Subject<ReactiveDimension>            dimensionSubject   = ReplaySubject.<ReactiveDimension>create();
	private final Subject<Reactivable>                  reactivableSubject = ReplaySubject.<Reactivable>create();
	private final List<ModuleListener>                  _moduleListeners   = new ArrayList<>();
	Observer<ReactiveDimension> dimensionObserver   = new Observer<ReactiveDimension>() {
		@Override
		public void onComplete() {

		}

		@Override
		public void onError(@NonNull final Throwable e) {

		}

		@Override
		public void onNext(@NonNull final ReactiveDimension aReactiveDimension) {
			// aReactiveDimension.observe();
			throw new IllegalStateException("Error");
		}

		@Override
		public void onSubscribe(@NonNull final Disposable d) {

		}
	};
	Observer<Reactivable>       reactivableObserver = new Observer<Reactivable>() {

		@Override
		public void onComplete() {

		}

		@Override
		public void onError(@NonNull final Throwable e) {

		}

		@Override
		public void onNext(@NonNull final Reactivable aReactivable) {
//			ReplaySubject
			throw new IllegalStateException("Error");
		}

		@Override
		public void onSubscribe(@NonNull final Disposable d) {

		}
	};
	private AccessBus           ab;
	private ICompilationAccess  ca;
	private ICompilationBus     compilationBus;
	private CompilationRunner   compilationRunner;
	//	private CompilerDriver      compilerDriver;
	private List<CompilerInput> inp;
	private IPipelineAccess     pa;
	private PipelineLogic       pipelineLogic;

	private CompilationRunner.CR_State crState;

	Eventual<ICompilationAccess> _p_CompilationAccess = new Eventual<>();

	Eventual<PipelineLogic> _p_PipelineLogic = new Eventual<>();

	Eventual<IPipelineAccess> _p_PipelineAccess = new Eventual<>();

	public CompilationEnclosure(final Compilation aCompilation) {
		compilation = aCompilation;

		waitPipelineAccess(pa0 -> {
			ab = new AccessBus(getCompilation(), pa0);

			accessBusPromise.resolve(ab); // [T188036]

//			ab.addPipelinePlugin(new CR_State.HooliganPipelinePlugin());
//			ab.addPipelinePlugin(new CR_State.EvaPipelinePlugin());
//			ab.addPipelinePlugin(new CR_State.DeducePipelinePlugin());
//			ab.addPipelinePlugin(new CR_State.WritePipelinePlugin());
//			ab.addPipelinePlugin(new CR_State.WriteMakefilePipelinePlugin());
//			ab.addPipelinePlugin(new CR_State.WriteMesonPipelinePlugin());
//			ab.addPipelinePlugin(new CR_State.WriteOutputTreePipelinePlugin());

			pa0._setAccessBus(ab);

			this.pa = pa0;
		});

		compilation.world().addModuleProcess(new CompletableProcess<WorldModule>() {
			@Override
			public void add(final WorldModule item) {
				// TODO Reactive pattern (aka something ala ReplaySubject)
				for (final ModuleListener moduleListener : _moduleListeners) {
					moduleListener.listen(item);
				}
			}

			@Override
			public void complete() {
				// TODO Reactive pattern (aka something ala ReplaySubject)
				for (final ModuleListener moduleListener : _moduleListeners) {
					moduleListener.close();
				}
			}

			@Override
			public void error(final Diagnostic d) {

			}

			@Override
			public void preComplete() {

			}

			@Override
			public void start() {

			}
		});

		waitPipelineLogic(wpl -> pipelineLogic = wpl);

		providePipelineAccess(new IPipelineAccess() {
			@Override
			public void _send_GeneratedClass(final EvaNode aClass) {

			}

			@Override
			public void _setAccessBus(final AccessBus ab) {
//				accessBusPromise.resolve(ab); // README not necessary [T188036]
			}

			@Override
			public void activeClass(final EvaClass aEvaClass) {

			}

			@Override
			public void activeFunction(final BaseEvaFunction aEvaFunction) {

			}

			@Override
			public void activeNamespace(final EvaNamespace aEvaNamespace) {

			}

			@Override
			public void addLog(final ElLog aLOG) {

			}

			@Override
			public AccessBus getAccessBus() {
				return null;
			}

			@Override
			public List<EvaClass> getActiveClasses() {
				return null;
			}

			@Override
			public List<BaseEvaFunction> getActiveFunctions() {
				return null;
			}

			@Override
			public List<EvaNamespace> getActiveNamespaces() {
				return null;
			}

			@Override
			public Compilation getCompilation() {
				return compilation;
			}

			@Override
			public CompilationEnclosure getCompilationEnclosure() {
				return compilation.getCompilationEnclosure();
			}

			@Override
			public List<CompilerInput> getCompilerInput() {
				return null;
			}

			@Override
			public GenerateResultSink getGenerateResultSink() {
				return null;
			}

			@Override
			public DeferredObject<PipelineLogic, Void, Void> getPipelineLogicPromise() {
				return null;
			}

			@Override
			public ProcessRecord getProcessRecord() {
				return null;
			}

			@Override
			public WritePipeline getWitePipeline() {
				return null;
			}

			@Override
			public void install_notate(final Provenance aProvenance, final Class<? extends GN_Notable> aRunClass, final Class<? extends GN_Env> aEnvClass) {
				installs.put(aProvenance, Pair.of(aRunClass, aEnvClass));
			}

			@Override
			public void notate(final Provenance aProvenance, final GN_Env aGNEnv) {
				var y = installs.get(aProvenance);
				//System.err.println("210 "+y);

				Class<?> x = y.getLeft();
				//var z = y.getRight();

				try {
					var inst = x.getMethod("getFactoryEnv", GN_Env.class);

					var notable1 = inst.invoke(null, aGNEnv);

					if (notable1 instanceof @NotNull GN_Notable notable) {
						final NotableAction notableAction = new NotableAction(notable);

						//cb.add(notableAction);

						notableAction._actual_run();

						//System.err.println("227 "+inst);
					}
				} catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			@Override
			public void notate(final Provenance provenance, final GN_Notable aNotable) {

			}

			@Override
			public PipelineLogic pipelineLogic() {
				return null;
			}

			@Override
			public void registerNodeList(final DoneCallback<List<EvaNode>> done) {

			}

			@Override
			public void resolvePipelinePromise(final PipelineLogic aPipelineLogic) {

			}

			@Override
			public void resolveWaitGenC(final OS_Module mod, final GenerateC gc) {

			}

			@Override
			public void setCompilerInput(final List<CompilerInput> aInputs) {

			}

			@Override
			public void setGenerateResultSink(final GenerateResultSink aGenerateResultSink) {

			}

			@Override
			public void setNodeList(final List<EvaNode> aEvaNodeList) {

			}

			@Override
			public void setWritePipeline(final WritePipeline aWritePipeline) {

			}

			@Override
			public void waitGenC(final OS_Module mod, final Consumer<GenerateC> aCb) {

			}
		});
	}

	private void _resolvePipelineAccess(final PipelineLogic aPipelineLogic) {
	}

//	@Contract(pure = true)
//	public CB_Output getCB_Output() {
//		return this._cbOutput;
//	}

	public void addModuleListener(final ModuleListener aModuleListener) {
		_moduleListeners.add(aModuleListener);
	}

	public @NotNull ModuleThing addModuleThing(final OS_Module aMod) {
		var mt = new ModuleThing(aMod);
		moduleThings.put(aMod, mt);
		return mt;
	}

	//@Contract(pure = true) //??
//	public CompilationClosure getCompilationClosure() {
//		return this.getCompilation().getCompilationClosure();
//	}

//	private final List<Triple<AssOutFile, EOT_OutputFile.FileNameProvider, NG_OutputRequest>> outFileAssertions = new ArrayList<>();

	public void addReactive(@NotNull Reactivable r) {
		int y = 2;
		// reactivableObserver.onNext(r);
		reactivableSubject.onNext(r);

		// reactivableObserver.
		dimensionSubject.subscribe(new Observer<ReactiveDimension>() {
			@Override
			public void onComplete() {

			}

			@Override
			public void onError(@NonNull final @NotNull Throwable e) {
				e.printStackTrace();
			}

			@Override
			public void onNext(final ReactiveDimension aReactiveDimension) {
				// r.join(aReactiveDimension);
				r.respondTo(aReactiveDimension);
			}

			@Override
			public void onSubscribe(@NonNull final Disposable d) {

			}
		});
	}

	public void addReactive(@NotNull Reactive r) {
		dimensionSubject.subscribe(new Observer<ReactiveDimension>() {
			@Override
			public void onComplete() {

			}

			@Override
			public void onError(@NonNull final @NotNull Throwable e) {
				e.printStackTrace();
			}

			@Override
			public void onNext(@NonNull ReactiveDimension dim) {
				r.join(dim);
			}

			@Override
			public void onSubscribe(@NonNull final Disposable d) {

			}
		});
	}

	public void addReactiveDimension(final ReactiveDimension aReactiveDimension) {
		dimensionSubject.onNext(aReactiveDimension);

		reactivableSubject.subscribe(new Observer<Reactivable>() {
			@Override
			public void onComplete() {

			}

			@Override
			public void onError(@NonNull final @NotNull Throwable e) {
				e.printStackTrace();
			}

			@Override
			public void onNext(@NonNull final @NotNull Reactivable aReactivable) {
				addReactive(aReactivable);
			}

			@Override
			public void onSubscribe(@NonNull final Disposable d) {

			}
		});

//		aReactiveDimension.setReactiveSink(addReactive);
	}

	public @NotNull Promise<AccessBus, Void, Void> getAccessBusPromise() {
		return accessBusPromise;
	}

//	public void setCompilerDriver(final CompilerDriver aCompilerDriver) {
//		compilerDriver = aCompilerDriver;
//	}

	@Contract(pure = true)
	public Compilation getCompilation() {
		return compilation;
	}

	@Contract(pure = true)
	public @NotNull ICompilationAccess getCompilationAccess() {
		return ca;
	}

	@Contract(pure = true)
	public ICompilationBus getCompilationBus() {
		return compilationBus;
	}

	@Contract(pure = true)
	public CompilationRunner getCompilationRunner() {
		return compilationRunner;
	}

//	public void setPipelineLogic(final PipelineLogic aPipelineLogic) {
//		pipelineLogic = aPipelineLogic;
//
//		getPipelineAccessPromise().then(pa->pa.resolvePipelinePromise(aPipelineLogic));
//	}

	@Contract(pure = true)
	public List<CompilerInput> getCompilerInput() {
		return inp;
	}

	public CompilationRunner.CR_State getCrState(final ICompilationBus aCb) {
		if (crState == null) {
			crState = new CompilationRunner.CR_State();
		}
		return crState;
	}

	public ModuleThing getModuleThing(final OS_Module aMod) {
		if (moduleThings.containsKey(aMod)) {
			return moduleThings.get(aMod);
		}
		return addModuleThing(aMod);
	}

/*
	public void noteAccept(final @NotNull WorldModule aWorldModule) {
		var mod = aWorldModule.module();
		var aMt = aWorldModule.rq().mt();
		// System.err.println(mod);
		// System.err.println(aMt);
	}
*/

	@Contract(pure = true)
	public IPipelineAccess getPipelineAccess() {
		return pa;
	}

	@Contract(pure = true)
	public PipelineLogic getPipelineLogic() {
		return pipelineLogic;
	}

	public void provideCompilationAccess(final ICompilationAccess aCompilationAccess) {
		_p_CompilationAccess.resolve(aCompilationAccess);
	}

	public void providePipelineAccess(final IPipelineAccess aPipelineAccess) {
		_p_PipelineAccess.resolve(aPipelineAccess);
	}

	public void providePipelineLogic(final PipelineLogic aPipelineLogic) {
		if (!_p_PipelineLogic.isResolved())
			_p_PipelineLogic.resolve(aPipelineLogic);
		else {
			System.err.println("370370 PipelineLogic already resolved.");
		}
	}

	public void reactiveJoin(final Reactive aReactive) {
		// throw new IllegalStateException("Error");

		// aReactive.join();
		System.err.println("reactiveJoin " + aReactive.toString());
	}

	public void setCompilationAccess(@NotNull ICompilationAccess aca) {
		ca = aca;
	}

	public void setCompilationBus(final ICompilationBus aCompilationBus) {
		compilationBus = aCompilationBus;
	}

	public void setCompilationRunner(final CompilationRunner aCompilationRunner) {
		compilationRunner = aCompilationRunner;
	}

	public void setCompilerInput(final List<CompilerInput> aInputs) {
		inp = aInputs;
	}

	public ElLog.Verbosity testSilence() {
		final boolean sil = getCompilation().getSilence();
		if (sil) {
			return ElLog.Verbosity.SILENT;
		} else {
			return ElLog.Verbosity.VERBOSE;
		}
	}

	public void waitCompilationAccess(final Consumer<ICompilationAccess> cica) {
		_p_CompilationAccess.then(cica::accept);
	}

	public void waitPipelineAccess(final Consumer<IPipelineAccess> cipa) {
		_p_PipelineAccess.then(cipa::accept);
	}

//	private final @NonNull OFA ofa = new OFA(/*outFileAssertions*/);

//	public void addEntryPoint(final @NotNull Mirror_EntryPoint aMirrorEntryPoint, final IClassGenerator dcg) {
//		aMirrorEntryPoint.generate(dcg);
//	}

//	@Contract(pure = true)
//	public CompilerDriver getCompilationDriver() {
//		return getCompilationBus().getCompilerDriver();
//	}

//	public void AssertOutFile(final @NotNull NG_OutputRequest aOutputRequest) {
//		var fileName = aOutputRequest.fileName();
//		if (fileName instanceof OutputStrategyC.OSC_NFC nfc) {
//			AssertOutFile_Class(nfc, aOutputRequest);
//		} else if (fileName instanceof OutputStrategyC.OSC_NFF nff) {
//			AssertOutFile_Function(nff, aOutputRequest);
//		} else if (fileName instanceof OutputStrategyC.OSC_NFN nfn) {
//			AssertOutFile_Namespace(nfn, aOutputRequest);
//		} else {
//			throw new NotImplementedException();
//		}
//	}

//	private void AssertOutFile_Class(final OutputStrategyC.OSC_NFC aNfc, final NG_OutputRequest aOutputRequest) {
//		outFileAssertions.add(Triple.of(AssOutFile.CLASS, aNfc, aOutputRequest));
//	}
//
//	private void AssertOutFile_Function(final OutputStrategyC.OSC_NFF aNff, final NG_OutputRequest aOutputRequest) {
//		outFileAssertions.add(Triple.of(AssOutFile.FUNCTION, aNff, aOutputRequest));
//	}
//
//	private void AssertOutFile_Namespace(final OutputStrategyC.OSC_NFN aNfn, final NG_OutputRequest aOutputRequest) {
//		outFileAssertions.add(Triple.of(AssOutFile.NAMESPACE, aNfn, aOutputRequest));
//	}

//	public @NonNull OFA OutputFileAsserts() {
//		return ofa;
//	}

//	public void logProgress(final @NotNull CompProgress aCompProgress, final Object x) {
//		switch (aCompProgress) {
//		case __CP_OutputPath_renderNode -> {
//			ER_Node node = (ER_Node) x;
//
//			System.out.printf("** [__CP_OutputPath_renderNode] %s%n", node.getPath());
//		}
//		case __parseElijjahFile_InputRequest -> {
//			InputRequest aInputRequest = (InputRequest) x;
//			File         f             = aInputRequest.file();
//
//			System.out.printf("** [__parseElijjahFile_InputRequest] %s%n", f.getAbsolutePath());
//		}
//		default -> throw new IllegalStateException("Unexpected value: " + aCompProgress);
//		}
//	}

//	public enum AssOutFile {CLASS, NAMESPACE, FUNCTION}

	public void waitPipelineLogic(final Consumer<PipelineLogic> cpl) {
		_p_PipelineLogic.then(cpl::accept);
	}

//	public class OFA implements Iterable<Triple<AssOutFile, EOT_OutputFile.FileNameProvider, NG_OutputRequest>> {
//
//		//public OFA(final List<Triple<AssOutFile, EOT_OutputFile.FileNameProvider, NG_OutputRequest>> aOutFileAssertions) {
//		//_l = aOutFileAssertions;
//		//}
//
//		public boolean contains(String aFileName) {
//			for (Triple<AssOutFile, EOT_OutputFile.FileNameProvider, NG_OutputRequest> outFileAssertion : outFileAssertions) {
//				final String containedFilename = outFileAssertion.getMiddle().getFilename();
//
//				if (containedFilename.equals(aFileName)) {
//					return true;
//				}
//			}
//
//			return false;
//		}
//
//		@Override
//		public Iterator<Triple<AssOutFile, EOT_OutputFile.FileNameProvider, NG_OutputRequest>> iterator() {
//			return outFileAssertions.stream().iterator();
//		}
//	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
