/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import static tripleo.elijah.util.Helpers.List_of;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ICompilationAccess;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.lang.types.OS_UnknownType;
import tripleo.elijah.nextgen.ClassDefinition;
import tripleo.elijah.nextgen.diagnostic.CouldntGenerateClass;
import tripleo.elijah.nextgen.reactive.ReactiveDimension;
import tripleo.elijah.stages.deduce.declarations.DeferredMember;
import tripleo.elijah.stages.deduce.declarations.DeferredMemberFunction;
import tripleo.elijah.stages.deduce.nextgen.DR_Ident;
import tripleo.elijah.stages.deduce.nextgen.DR_Item;
import tripleo.elijah.stages.deduce.nextgen.DR_ProcCall;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_IdentTableEntry;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaConstructor;
import tripleo.elijah.stages.gen_fn.EvaContainer;
import tripleo.elijah.stages.gen_fn.EvaContainerNC;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_fn.GeneratePhase;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.WlGenerateClass;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.stages.post_deduce.DefaultCodeRegistrar;
import tripleo.elijah.stateful.State;
import tripleo.elijah.stateful._RegistrationTarget;
import tripleo.elijah.testing.comp.IFunctionMapHook;
import tripleo.elijah.util.Maybe;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;
import tripleo.elijah.world.i.WorldModule;

/**
 * Created 12/24/20 3:59 AM
 */
public class DeducePhase extends _RegistrationTarget implements ReactiveDimension {
	public interface Country {
		void sendClasses(Consumer<List<EvaNode>> ces);
	}

	class Country1 implements Country {
		@Override
		public void sendClasses(final @NotNull Consumer<List<EvaNode>> ces) {
			ces.accept(generatedClasses.copy());
		}
	}

	public class DeducePhaseInjector {
		public List<DE3_Active> new_ArrayList__DE3_Active() {
			return new ArrayList<>();
		}

		public List<DeferredMemberFunction> new_ArrayList__DeferredaMemberFunction() {
			return new ArrayList<>();
		}

		public List<DeferredMember> new_ArrayList__DeferredMember() {
			return new ArrayList<>();
		}

		public List<EvaClass> new_ArrayList__EvaClass() {
			return new ArrayList<>();
		}

		public List<EvaNode> new_ArrayList__EvaNode() {
			return new ArrayList<>();
		}

		public List<EvaNode> new_ArrayList__EvaNode(final List<EvaNode> aGeneratedClasses) {
			return new ArrayList<>(aGeneratedClasses);
		}

		public List<FoundElement> new_ArrayList__FoundElement() {
			return new ArrayList<>();
		}

		public List<IFunctionMapHook> new_ArrayList__IFunctionMapHook() {
			return new ArrayList<>();
		}

		public List<State> new_ArrayList__State() {
			return new ArrayList<>();
		}

		public ClassDefinition new_ClassDefinition(final ClassInvocation aCi) {
			return new ClassDefinition(aCi);
		}

		public @NotNull ClassInvocation new_ClassInvocation(final ClassStatement aParent, final String aConstructorName, final @NotNull Supplier<DeduceTypes2> aDeduceTypes2Supplier) {
			return new ClassInvocation(aParent, aConstructorName, aDeduceTypes2Supplier);
		}

		public Diagnostic new_CouldntGenerateClass(final ClassDefinition aCd, final GenerateFunctions aGf, final ClassInvocation aCi) {
			return new CouldntGenerateClass(aCd, aGf, aCi);
		}

		public Country1 new_Country1(final DeducePhase aDeducePhase) {
			return aDeducePhase.new Country1();
		}

		public DeduceTypes2 new_DeduceTypes2(final OS_Module aM, final DeducePhase aDeducePhase, final ElLog.Verbosity aVerbosity) {
			return new DeduceTypes2(aM, aDeducePhase, aVerbosity);
		}

		public ICodeRegistrar new_DefaultCodeRegistrar(final Compilation aCompilation) {
			return new DefaultCodeRegistrar(aCompilation);
		}

		public DeferredMemberFunctionParentIsClassStatement new_DeferredMemberFunctionParentIsClassStatement(final DeferredMemberFunction aDeferredMemberFunction, final IInvocation aInvocation, final DeducePhase aDeducePhase) {
			return aDeducePhase.new DeferredMemberFunctionParentIsClassStatement(aDeferredMemberFunction, aInvocation);
		}

		public DRS new_DRS() {
			return new DRS();
		}

		public ElLog new_ElLog(final String aS, final ElLog.Verbosity aVerbosity, final String aDeducePhase) {
			return new ElLog(aS, aVerbosity, aDeducePhase);
		}

		public FunctionInvocation new_FunctionInvocation(final BaseFunctionDef aF, final ProcTableEntry aO, final IInvocation aCi, final GeneratePhase aGeneratePhase) {
			return new FunctionInvocation(aF, aO, aCi, aGeneratePhase);
		}

		public GeneratedClasses new_GeneratedClasses(final DeducePhase aDeducePhase) {
			return aDeducePhase.new GeneratedClasses();
		}

		public GenType new_GenTypeImpl() {
			return new GenType();
		}

		public Function<EvaNode, Map<FunctionDef, EvaFunction>> new_GetFunctionMapClass() {
			return new DeferredMemberFunctionParentIsClassStatement.GetFunctionMapClass();
		}

		public Function<EvaNode, Map<FunctionDef, EvaFunction>> new_GetFunctionMapNamespace() {
			return new DeferredMemberFunctionParentIsClassStatement.GetFunctionMapNamespace();
		}

		public Map<IdentTableEntry, OnType> new_HashMap__IdentTableEntry() {
			return new HashMap<>();
		}

		public Map<NamespaceStatement, NamespaceInvocation> new_HashMap__NamespaceInvocationMap() {
			return new HashMap<NamespaceStatement, NamespaceInvocation>();
		}

		public NamespaceInvocation new_NamespaceInvocation(final NamespaceStatement aParent) {
			return new NamespaceInvocation(aParent);
		}

		public RegisterClassInvocation new_RegisterClassInvocation(final DeducePhase aDeducePhase) {
			return aDeducePhase.new RegisterClassInvocation();
		}

		public ResolvedVariables new_ResolvedVariables(final IdentTableEntry aIdentTableEntry, final OS_Element aParent, final String aVarName) {
			return new ResolvedVariables(aIdentTableEntry, aParent, aVarName);
		}

		public WAITS new_WAITS() {
			return new WAITS();
		}

		public WlGenerateClass new_WlGenerateClass(final GenerateFunctions aGenerateFunctions, final ClassInvocation aClassInvocation, final GeneratedClasses aGeneratedClasses, final ICodeRegistrar aCodeRegistrar) {
			return new WlGenerateClass(aGenerateFunctions, aClassInvocation, aGeneratedClasses, aCodeRegistrar);
		}

		public WorkJob new_WlGenerateClass(final GenerateFunctions aGenerateFunctions, final ClassInvocation aClassInvocation, final GeneratedClasses aGeneratedClasses, final ICodeRegistrar aCodeRegistrar, final RegisterClassInvocation_env aEnv) {
			return new WlGenerateClass(aGenerateFunctions, aClassInvocation, aGeneratedClasses, aCodeRegistrar, aEnv);
		}

		public WorkList new_WorkList() {
			return new WorkList();
		}

		public WorkManager new_WorkManager() {
			return new WorkManager();
		}
	}
	enum DeducePhaseProvenance {
		DeduceTypes_create // 196
	}
	/*static*/ class DeferredMemberFunctionParentIsClassStatement {
		static class GetFunctionMapClass implements Function<EvaNode, Map<FunctionDef, EvaFunction>> {
			@Override
			public Map<FunctionDef, EvaFunction> apply(final @NotNull EvaNode aClass) {
				return ((EvaClass) aClass).functionMap;
			}
		}
		static class GetFunctionMapNamespace implements Function<EvaNode, Map<FunctionDef, EvaFunction>> {
			@Override
			public Map<FunctionDef, EvaFunction> apply(final @NotNull EvaNode aNamespace) {
				return ((EvaNamespace) aNamespace).functionMap;
			}
		}
		private final DeferredMemberFunction deferredMemberFunction;

		private final IInvocation            invocation;

		private final OS_Element             parent;

		public DeferredMemberFunctionParentIsClassStatement(final DeferredMemberFunction aDeferredMemberFunction, final IInvocation aInvocation) {
			deferredMemberFunction = aDeferredMemberFunction;
			invocation             = aInvocation;
			parent                 = deferredMemberFunction.getParent();//.getParent().getParent();
		}

		void action() {
			if (invocation instanceof ClassInvocation)
				((ClassInvocation) invocation).resolvePromise().then(new DoneCallback<EvaClass>() {
					@Override
					public void onDone(final EvaClass result) {
						defaultAction(result);
					}
				});
			else if (invocation instanceof NamespaceInvocation)
				((NamespaceInvocation) invocation).resolvePromise().then(new DoneCallback<EvaNamespace>() {
					@Override
					public void onDone(final EvaNamespace result) {
						defaultAction(result);
					}
				});
		}

		<T extends EvaNode> void defaultAction(final T result) {
			final OS_Element p = deferredMemberFunction.getParent();

			if (p instanceof final DeduceTypes2.@NotNull OS_SpecialVariable specialVariable) {
				onSpecialVariable(specialVariable);
				final int y = 2;
			} else if (p instanceof ClassStatement) {
				final Function<EvaNode, Map<FunctionDef, EvaFunction>> x = getFunctionMap(result);

				// once again we need EvaFunction, not FunctionDef
				// we seem to have it below, but there can be multiple
				// specializations of each function

				final EvaFunction gf = x.apply(result).get(deferredMemberFunction.getFunctionDef());
				if (gf != null) {
					deferredMemberFunction.externalRefDeferred().resolve(gf);
					gf.typePromise().then(new DoneCallback<GenType>() {
						@Override
						public void onDone(final GenType result) {
							deferredMemberFunction.typeResolved().resolve(result);
						}
					});
				}
			} else
				throw new IllegalStateException("unknown parent");
		}

		@NotNull
		private <T extends EvaNode> Function<EvaNode, Map<FunctionDef, EvaFunction>> getFunctionMap(final T result) {
			final Function<EvaNode, Map<FunctionDef, EvaFunction>> x;
			if (result instanceof EvaNamespace)
				x = _inj().new_GetFunctionMapNamespace();
			else if (result instanceof EvaClass)
				x = _inj().new_GetFunctionMapClass();
			else
				throw new NotImplementedException();
			return x;
		}

		public void onSpecialVariable(final DeduceTypes2.@NotNull OS_SpecialVariable aSpecialVariable) {
			final DeduceLocalVariable.MemberInvocation mi = aSpecialVariable.memberInvocation;

			switch (mi.role) {
			case INHERITED:
				final FunctionInvocation functionInvocation = deferredMemberFunction.functionInvocation();
				functionInvocation.generatePromise().
						then(new DoneCallback<BaseEvaFunction>() {
							@Override
							public void onDone(final @NotNull BaseEvaFunction gf) {
								deferredMemberFunction.externalRefDeferred().resolve(gf);
								gf.typePromise().
										then(new DoneCallback<GenType>() {
											@Override
											public void onDone(final GenType result) {
												deferredMemberFunction.typeResolved().resolve(result);
											}
										});
							}
						});
				break;
			case DIRECT:
				if (invocation instanceof NamespaceInvocation)
					assert false;
				else {
					final ClassInvocation classInvocation = (ClassInvocation) invocation;
					classInvocation.resolvePromise().
							then(new DoneCallback<EvaClass>() {
								@Override
								public void onDone(final @NotNull EvaClass element_generated) {
									// once again we need EvaFunction, not FunctionDef
									// we seem to have it below, but there can be multiple
									// specializations of each function
									final EvaFunction gf = element_generated.functionMap.get(deferredMemberFunction.getFunctionDef());
									deferredMemberFunction.externalRefDeferred().resolve(gf);
									gf.typePromise().
											then(new DoneCallback<GenType>() {
												@Override
												public void onDone(final GenType result) {
													deferredMemberFunction.typeResolved().resolve(result);
												}
											});
								}
							});
				}
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + mi.role);
			}
		}
	}
	static class DRS {

		private final List<Pair<BaseEvaFunction, DR_Item>> drs = new ArrayList<>();

		public void add(final Pair<BaseEvaFunction, DR_Item> aDrp) {
			drs.add(aDrp);
		}
		public Iterable<Pair<BaseEvaFunction, DR_Item>> iterator() {
			return drs;
		}

	}
	public class GeneratedClasses implements Iterable<EvaNode> {
		@NotNull List<EvaNode> generatedClasses = _inj().new_ArrayList__EvaNode();
		private  int           generation;

		public void add(final EvaNode aClass) {
			pa._send_GeneratedClass(aClass);

			generatedClasses.add(aClass);
		}

		public void addAll(@NotNull final List<EvaNode> lgc) {
			// TODO is this method really needed
			generatedClasses.addAll(lgc);
		}

		public @NotNull List<EvaNode> copy() {
			++generation;
			return new ArrayList<>(generatedClasses);
		}

		@Override
		public @NotNull Iterator<EvaNode> iterator() {
			return generatedClasses.iterator();
		}

		public int size() {
			return generatedClasses.size();
		}

		@Override
		public String toString() {
			return "GeneratedClasses{size=%d, generation=%d}".formatted(generatedClasses.size(), generation);
		}
	}
	class RegisterClassInvocation {
		// TODO this class is a mess

		private @NotNull ClassInvocation getClassInvocation(final @NotNull ClassInvocation aClassInvocation, OS_Module mod, final WorkList wl, final @NotNull RegisterClassInvocation_env aEnv) {
			if (mod == null)
				mod = aClassInvocation.getKlass().getContext().module();

			if (false) {
				final var prom = generateClass(generatePhase.getGenerateFunctions(mod), aClassInvocation, generatePhase.getWm());

				//return prom;
				return null;
			} else {
				final DeferredObject<ClassDefinition, Diagnostic, Void> prom = new DeferredObject<>();

				final GenerateFunctions generateFunctions = generatePhase.getGenerateFunctions(mod);
				wl.addJob(_inj().new_WlGenerateClass(generateFunctions, aClassInvocation, generatedClasses, codeRegistrar, aEnv)); // TODO why add now?
				generatePhase.getWm().addJobs(wl);
				generatePhase.getWm().drain(); // TODO find a better place to put this

				prom.resolve(new ClassDefinition(aClassInvocation));

				//return prom;
				return aClassInvocation;
			}
		}

		private @NotNull ClassInvocation part2(final @NotNull ClassInvocation aClassInvocation, final boolean put, final @NotNull RegisterClassInvocation_env aEnv) {
			// 2. Check and see if already done
			final Collection<ClassInvocation> cls = classInvocationMultimap.get(aClassInvocation.getKlass());
			for (@NotNull final ClassInvocation ci : cls) {
				if (equivalentGenericPart(ci, aClassInvocation)) {
					return ci;
				}
			}

			if (put) {
				classInvocationMultimap.put(aClassInvocation.getKlass(), aClassInvocation);
			}

			// 3. Generate new EvaClass
			final @NotNull WorkList  wl  = _inj().new_WorkList();

			final var x = getClassInvocation(aClassInvocation, null, wl, aEnv);

			// 4. Return it
			//final ClassDefinition[] yy = new ClassDefinition[1];
			//x.then(y -> yy[0] =y);
			//return yy[0];
			return x;
		}

		public @NotNull ClassInvocation registerClassInvocation(@NotNull final ClassInvocation aClassInvocation) {
			return registerClassInvocation(new RegisterClassInvocation_env(aClassInvocation, null, null));
		}

		public ClassInvocation registerClassInvocation(final @NotNull RegisterClassInvocation_env env) {
			final var aClassInvocation = env.ci();

			// 1. select which to return
			final ClassStatement              c   = aClassInvocation.getKlass();
			final Collection<ClassInvocation> cis = classInvocationMultimap.get(c);

			for (@NotNull final ClassInvocation ci : cis) {
				// don't lose information
				if (ci.getConstructorName() != null)
					if (!(ci.getConstructorName().equals(aClassInvocation.getConstructorName())))
						continue;

				final boolean i = equivalentGenericPart(aClassInvocation, ci);
				if (i) {
					if (aClassInvocation instanceof DerivedClassInvocation) {
						if (ci instanceof DerivedClassInvocation)
							continue;

						/*if (ci.resolvePromise().isResolved())*/
						{
							ci.resolvePromise().then((final @NotNull EvaClass result) -> {
								aClassInvocation.resolveDeferred().resolve(result);
							});
							return aClassInvocation;
						}
					} else
						return ci;
				}
			}

			return part2(aClassInvocation, true, env);
		}
	}
	static class ResolvedVariables {
		final          IdentTableEntry identTableEntry;
		final @NotNull OS_Element      parent; // README tripleo.elijah.lang._CommonNC, but that's package-private
		final          String          varName;

		public ResolvedVariables(final IdentTableEntry aIdentTableEntry, final OS_Element aParent, final String aVarName) {
			assert aParent instanceof ClassStatement || aParent instanceof NamespaceStatement;

			identTableEntry = aIdentTableEntry;
			parent          = aParent;
			varName         = aVarName;
		}
	}
	static class WAITS {

		private final Set<DeduceTypes2> waits = new HashSet<>();

		public void add(final DeduceTypes2 aDeduceTypes2) {
			waits.add(aDeduceTypes2);
		}
		public Iterable<DeduceTypes2> iterator() {
			return waits;
		}

	}
	private final String PHASE = "DeducePhase";
	private @NotNull
	final DeducePhaseInjector __inj = new DeducePhaseInjector();
	public final @NotNull GeneratedClasses generatedClasses;
	private @NotNull ICodeRegistrar     codeRegistrar;
	public @NotNull  GeneratePhase      generatePhase;
	private @NotNull ICompilationAccess ca;

	private final         Map<NamespaceStatement, NamespaceInvocation> namespaceInvocationMap = _inj().new_HashMap__NamespaceInvocationMap();
	private final          ExecutorService                              classGenerator         = Executors.newCachedThreadPool();
	private final          Country1                                     country                 = _inj().new_Country1(this);
	private final          List<DeferredMemberFunction>                 deferredMemberFunctions = _inj().new_ArrayList__DeferredaMemberFunction();
	private final          List<FoundElement>                           foundElements       = _inj().new_ArrayList__FoundElement();
	private final          Multimap<BaseFunctionDef, EvaFunction> functionMap         = ArrayListMultimap.create();
	private final    Map<IdentTableEntry, OnType> idte_type_callbacks = _inj().new_HashMap__IdentTableEntry();
	private @NotNull ElLog                        LOG;

	private @NotNull PipelineLogic                pipelineLogic;

	private final          List<DE3_Active> _actives                = _inj().new_ArrayList__DE3_Active();

	public final @NotNull          List<IFunctionMapHook>                    functionMapHooks        = _inj().new_ArrayList__IFunctionMapHook();

	private final @NotNull Multimap<ClassStatement, ClassInvocation> classInvocationMultimap = ArrayListMultimap.create();

	private final @NotNull List<DeferredMember>                      deferredMembers         = _inj().new_ArrayList__DeferredMember();

	private final @NotNull Multimap<ClassStatement, OnClass>       onclasses          = ArrayListMultimap.create();

	private final @NotNull Multimap<OS_Element, ResolvedVariables> resolved_variables = ArrayListMultimap.create();

	private final @NotNull DRS                                     drs                = _inj().new_DRS();

	private final @NotNull WAITS           waits                   = _inj().new_WAITS();

	public                 IPipelineAccess pa;
	final Multimap<OS_Module, Consumer<DeduceTypes2>> iWantModules = ArrayListMultimap.create();
	public DeducePhase(final @NotNull CompilationEnclosure ace) {
		this(ace, ace.getPipelineLogic());
	}

	public DeducePhase(final @NotNull CompilationEnclosure ce,
	                   final /*@NotNull*/ PipelineLogic aPipelineLogic) {
		ce.waitPipelineLogic(wpl -> {
			pipelineLogic = wpl;
			generatePhase = pipelineLogic.generatePhase;

			LOG = _inj().new_ElLog("(DEDUCE_PHASE)", pipelineLogic.getVerbosity(), PHASE);
			pipelineLogic.addLog(LOG);
		});

		ce.waitCompilationAccess(wca -> {
			ca            = wca;
			codeRegistrar = _inj().new_DefaultCodeRegistrar(ca.getCompilation());
		});

		ce.waitPipelineAccess(wpa -> {
			pa = wpa;
		});

		ce.addReactiveDimension(this);

		// create more
		generatedClasses = _inj().new_GeneratedClasses(this);

		//
		DeduceElement3_IdentTableEntry.ST.register(this);
	}

	public Compilation _compilation() {
		return ca.getCompilation();
	}

	public @NotNull Multimap<BaseFunctionDef, EvaFunction> _functionMap() {
		return functionMap;
	}

	public @NotNull DeducePhaseInjector _inj() {
		return this.__inj;
	}

	public void addActives(@NotNull final List<DE3_Active> activesList) {
		_actives.addAll(activesList);
	}

	public void addDeferredMember(final @NotNull DeferredMember aDeferredMember) {
		deferredMembers.add(aDeferredMember);
	}

	public void addDeferredMember(final DeferredMemberFunction aDeferredMemberFunction) {
		deferredMemberFunctions.add(aDeferredMemberFunction);
	}

	private void addDr(final Pair<BaseEvaFunction, DR_Item> drp) {
		drs.add(drp);
	}

	public void addDrs(final BaseEvaFunction aGeneratedFunction, final List<? extends DR_Item> aDrs) {
		for (final DR_Item dr : aDrs) {
			addDr(Pair.of(aGeneratedFunction, dr));
		}
	}

//	public List<ElLog> deduceLogs = new ArrayList<ElLog>();

	public void addFunction(final EvaFunction generatedFunction, @NotNull final BaseFunctionDef fd) {
		functionMap.put(fd, generatedFunction);
	}

	public void addFunctionMapHook(final IFunctionMapHook aFunctionMapHook) {
		functionMapHooks.add(aFunctionMapHook);
	}

	public void addLog(final ElLog aLog) {
		//deduceLogs.add(aLog);
		pipelineLogic.addLog(aLog);
	}

	public @NotNull ICompilationAccess ca() {
		return ca;
	}

	public @NotNull Country country() {
		return country;
	}

	public @NotNull DeduceTypes2 deduceModule(final @NotNull WorldModule aMod) {
		return deduceModule(aMod, this.generatedClasses, Compilation.gitlabCIVerbosity());
	}

	public @NotNull DeduceTypes2 deduceModule(@NotNull final WorldModule wm, @NotNull final Iterable<EvaNode> lgf, final ElLog.Verbosity verbosity) {
		final var mod = wm.module();

		final @NotNull DeduceTypes2 deduceTypes2 = _inj().new_DeduceTypes2(mod, this, verbosity);

		logProgress(DeducePhaseProvenance.DeduceTypes_create, List.of(deduceTypes2, lgf));

		deduceTypes2.deduceFunctions(lgf);
//		deduceTypes2.deduceClasses(generatedClasses.copy().stream()
//				.filter(c -> c.module() == m)
//				.collect(Collectors.toList()));

		for (final EvaNode evaNode : generatedClasses.copy()) {
			if (evaNode.module() != mod) continue;

			if (evaNode instanceof final @NotNull EvaClass evaClass) {

				evaClass.fixupUserClasses(deduceTypes2, evaClass.getKlass().getContext());
				deduceTypes2.deduceOneClass(evaClass);
			}
		}

		for (final EvaNode evaNode : lgf) {
			final BaseEvaFunction bef;

			if (evaNode instanceof BaseEvaFunction) {
				bef = (BaseEvaFunction) evaNode;
			} else continue;
			for (final IFunctionMapHook hook : functionMapHooks) {
				if (hook.matches(bef.getFD())) {
					hook.apply(List_of((EvaFunction) bef));
				}
			}
		}

		return deduceTypes2;
	}

	public void doneWait(final DeduceTypes2 aDeduceTypes2, final BaseEvaFunction aGeneratedFunction) {
		NotImplementedException.raise();
	}

	public boolean equivalentGenericPart(@NotNull final ClassInvocation first, @NotNull final ClassInvocation second) {
		final ClassInvocation.CI_GenericPart secondGenericPart1 = second.genericPart();
		final ClassInvocation.CI_GenericPart firstGenericPart1  = first.genericPart();

		if (second.getKlass() == first.getKlass() /*&& secondGenericPart1 == null*/) return true;

		final Map<TypeName, OS_Type> secondGenericPart = secondGenericPart1.getMap();
		final Map<TypeName, OS_Type> firstGenericPart  = firstGenericPart1.getMap();

		int i = secondGenericPart.entrySet().size();
		for (final Map.@NotNull Entry<TypeName, OS_Type> entry : secondGenericPart.entrySet()) {
			final OS_Type entry_type = firstGenericPart.get(entry.getKey());
			//assert !(entry_type instanceof OS_UnknownType);

			if (entry_type instanceof OS_UnknownType) continue;

			if (entry_type.equals(entry.getValue()))
				i--;
//				else
//					return aClassInvocation;
		}
		return i == 0;
	}

	public void finish() {
		setGeneratedClassParents();
		/*
		for (GeneratedNode generatedNode : generatedClasses) {
			if (generatedNode instanceof EvaClass) {
				final EvaClass generatedClass = (EvaClass) generatedNode;
				final ClassStatement cs = generatedClass.getKlass();
				Collection<ClassInvocation> cis = classInvocationMultimap.get(cs);
				for (ClassInvocation ci : cis) {
					if (equivalentGenericPart(generatedClass.ci, ci)) {
						final DeferredObject<EvaClass, Void, Void> deferredObject = (DeferredObject<EvaClass, Void, Void>) ci.promise();
						deferredObject.then(new DoneCallback<EvaClass>() {
							@Override
							public void onDone(EvaClass result) {
								assert result == generatedClass;
							}
						});
//						deferredObject.resolve(generatedClass);
					}
				}
			}
		}
*/
		handleOnClassCallbacks();
		handleIdteTypeCallbacks();
/*
		for (Map.Entry<EvaFunction, OS_Type> entry : typeDecideds.entrySet()) {
			for (Triplet triplet : forFunctions) {
				if (triplet.gf.getGenerated() == entry.getKey()) {
					synchronized (triplet.deduceTypes2) {
						triplet.forFunction.typeDecided(entry.getValue());
					}
				}
			}
		}
*/
/*
		for (Map.Entry<FunctionDef, EvaFunction> entry : functionMap.entries()) {
			FunctionInvocation fi = _inj().new_FunctionInvocation(entry.getKey(), null);
			for (Triplet triplet : forFunctions) {
//				Collection<EvaFunction> x = functionMap.get(fi);
				triplet.forFunction.finish();
			}
		}
*/
		handleFoundElements();
		handleResolvedVariables();
		resolveAllVariableTableEntries();
		handleDeferredMemberFunctions();
		handleDeferredMembers();
		sanityChecks();
		handleFunctionMapHooks();

		for (final DE3_Active de3_Active : _actives) {
			pa.getCompilationEnclosure().addReactive(de3_Active);
		}

		for (final Pair<BaseEvaFunction, DR_Item> pair : drs.iterator()) {
			final BaseEvaFunction ef = pair.getLeft();
			final DR_Item         dr = pair.getRight();

			//System.err.println("611a " + ef);
			//System.err.println("611b " + dr);

			if (dr instanceof final DR_ProcCall drpc) {
				final var fi = drpc.getFunctionInvocation();
				if (fi != null) {
					final BaseEvaFunction[] ef1 = new BaseEvaFunction[1];
					fi.generatePromise().then(x -> ef1[0] = x);

					if (ef1[0] == null) {
						//throw new AssertionError();
						System.err.println("****************************** no function generated");
					} else {
						pa.activeFunction(ef1[0]);
					}
				}
			} else if (dr instanceof final DR_Ident drid) {
				//System.err.println(String.format("***** 623623 -- %s %b", drid.name(), drid.isResolved()));

				for (final DR_Ident.Understanding understanding : drid.u) {
					//System.err.println(String.format("**** 623626 -- %s", understanding.asString()));
				}
			}
		}

		for (final DeduceTypes2 wait : waits.iterator()) {
			for (final Map.Entry<OS_Module, Collection<Consumer<DeduceTypes2>>> entry : iWantModules.asMap().entrySet()) {
				if (entry.getKey() == wait.module) {
					for (final Consumer<DeduceTypes2> deduceTypes2Consumer : entry.getValue()) {

						// README I like this, but by the time we get here, everything is already done...
						//  and I mean the callback to DT_External2::actualise
						//  - everything being resolution and setStatus, etc

						deduceTypes2Consumer.accept(wait);
					}
				}
			}
		}
	}

	public void finish(final GeneratedClasses aGeneratedClasses) {
		finish();
	}

	public void finish_default() {
		finish();
	}

	public void forFunction(final DeduceTypes2 deduceTypes2, @NotNull final FunctionInvocation fi, @NotNull final ForFunction forFunction) {
//		LOG.err("272 forFunction\n\t"+fi.getFunction()+"\n\t"+fi.pte);
		fi.generateDeferred().promise()
				.then(result -> result.typePromise()
						.then(forFunction::typeDecided));
	}

	public Promise<ClassDefinition, Diagnostic, Void> generateClass(final GenerateFunctions gf, final @NotNull ClassInvocation ci) {
		final WorkManager wm = _inj().new_WorkManager();
		// par { return promise ; wm.drain() ; }
		final Promise<ClassDefinition, Diagnostic, Void> x = generateClass(gf, ci, wm);
		wm.drain();
		return x;
	}

	public @NotNull Promise<ClassDefinition, Diagnostic, Void> generateClass(final GenerateFunctions gf, final @NotNull ClassInvocation ci, final WorkManager wm) {
		final DeferredObject<ClassDefinition, Diagnostic, Void> ret = new DeferredObject<>();

		classGenerator.submit(new Runnable() {
			@Override
			public void run() {
				final WlGenerateClass gen = _inj().new_WlGenerateClass(gf, ci, generatedClasses, codeRegistrar);
				gen.run(wm);

				final ClassDefinition cd       = _inj().new_ClassDefinition(ci);
				final EvaClass        genclass = gen.getResult();
				if (genclass != null) {
					cd.setNode(genclass);
					ret.resolve(cd);
				} else {
					ret.reject(_inj().new_CouldntGenerateClass(cd, gf, ci));
				}
			}
		});

		return ret;
	}

	public ICodeRegistrar getCodeRegistrar() {
		return codeRegistrar;
	}

	public void handleDeferredMemberFunctions() {
		for (@NotNull final DeferredMemberFunction deferredMemberFunction : deferredMemberFunctions) {
			final int        y      = 2;
			final OS_Element parent = deferredMemberFunction.getParent();//.getParent().getParent();

			if (parent instanceof ClassStatement) {
				final IInvocation invocation = deferredMemberFunction.getInvocation();

				final DeferredMemberFunctionParentIsClassStatement dmfpic = _inj().new_DeferredMemberFunctionParentIsClassStatement(deferredMemberFunction, invocation, this);
				dmfpic.action();
			} else if (parent instanceof NamespaceStatement) {
//				final ClassStatement classStatement = (ClassStatement) deferredMemberFunction.getParent();
				final IInvocation invocation = deferredMemberFunction.getInvocation();

				final NamespaceInvocation namespaceInvocation;
				if (invocation instanceof ClassInvocation) {
					namespaceInvocation = _inj().new_NamespaceInvocation((NamespaceStatement) parent);
				} else {
					namespaceInvocation = (NamespaceInvocation) invocation;
				}

				namespaceInvocation.resolvePromise().
						then((final @NotNull EvaNamespace result) -> {
							final NamespaceInvocation             x  = namespaceInvocation;
							final @NotNull DeferredMemberFunction z  = deferredMemberFunction;
							final int                             yy = 2;
						});
			}
		}

		for (final EvaNode evaNode : generatedClasses) {
			if (evaNode instanceof final @NotNull EvaContainerNC nc) {
				nc.noteDependencies(nc.getDependency()); // TODO is this right?

				for (final EvaFunction generatedFunction : nc.functionMap.values()) {
					generatedFunction.noteDependencies(nc.getDependency());
				}
				if (nc instanceof final @NotNull EvaClass evaClass) {

					for (final EvaConstructor evaConstructor : evaClass.constructors.values()) {
						evaConstructor.noteDependencies(nc.getDependency());
					}
				}
			}
		}
	}

	public void handleDeferredMembers() {
		for (@NotNull final DeferredMember deferredMember : deferredMembers) {
			if (deferredMember.getParent().isNamespaceStatement()) {
				final @NotNull NamespaceStatement parent = (NamespaceStatement) deferredMember.getParent().element();
				final NamespaceInvocation         nsi    = registerNamespaceInvocation(parent);
				nsi.resolveDeferred()
						.done(result -> {
							@NotNull final Maybe<EvaContainer.VarTableEntry> v_m = result.getVariable(deferredMember.getVariableStatement().getName());

							assert !v_m.isException();

							final EvaContainer.VarTableEntry v = v_m.o;

							// TODO varType, potentialTypes and _resolved: which?
							//final OS_Type varType = v.varType;

							assert v != null;
							v.resolve_varType_cb((varType) -> {
								final @NotNull GenType genType = _inj().new_GenTypeImpl();
								genType.set(varType);

//								if (deferredMember.getInvocation() instanceof NamespaceInvocation) {
//									((NamespaceInvocation) deferredMember.getInvocation()).resolveDeferred().done(new DoneCallback<EvaNamespace>() {
//										@Override
//										public void onDone(EvaNamespace result) {
//											result;
//										}
//									});
//								}

								deferredMember.externalRefDeferred().resolve(result);
/*
							if (genType.resolved == null) {
								// HACK need to resolve, but this shouldn't be here
								try {
									@NotNull OS_Type rt = DeduceTypes2.resolve_type(null, varType, varType.getTypeName().getContext());
									genType.set(rt);
								} catch (ResolveError aResolveError) {
									aResolveError.printStackTrace();
								}
							}
							deferredMember.typeResolved().resolve(genType);
*/
							});
						});
			} else if (deferredMember.getParent().element() instanceof ClassStatement) {
				// TODO do something
				final ClassStatement parent = (ClassStatement) deferredMember.getParent().element();
				final String         name   = deferredMember.getVariableStatement().getName();

				// because deferredMember.invocation is null, we must create one here
				final @Nullable ClassInvocation ci = registerClassInvocation(parent, null, new NULL_DeduceTypes2());
				assert ci != null;
				ci.resolvePromise().then(result -> {
					final List<EvaContainer.VarTableEntry> vt = result.varTable;
					for (final EvaContainer.VarTableEntry gc_vte : vt) {
						if (gc_vte.nameToken.getText().equals(name)) {
							// check connections
							// unify pot. types (prol. shuld be done already -- we don't want to be reporting errors here)
							// call typePromises and externalRefPromisess

							// TODO just getting first element here (without processing of any kind); HACK
							final List<EvaContainer.VarTableEntry.ConnectionPair> connectionPairs = gc_vte.connectionPairs;
							if (connectionPairs.size() > 0) {
								final GenType ty = connectionPairs.get(0).vte.getType().genType;
								assert ty.getResolved() != null;
								gc_vte.varType = ty.getResolved(); // TODO make sure this is right in all cases
								if (deferredMember.typeResolved().isPending())
									deferredMember.typeResolved().resolve(ty);
								break;
							} else {
								NotImplementedException.raise();
							}
						}
					}
				});
			} else
				throw new NotImplementedException();
		}
	}

	public void handleFoundElements() {
		for (@NotNull final FoundElement foundElement : foundElements) {
			// TODO As we are using this, didntFind will never fail because
			//  we call doFoundElement manually in resolveIdentIA
			//  As the code matures, maybe this will change and the interface
			//  will be improved, namely calling doFoundElement from here as well
			if (foundElement.didntFind()) {
				foundElement.doNoFoundElement();
			}
		}
	}

	public void handleFunctionMapHooks() {
		for (final Map.@NotNull Entry<BaseFunctionDef, Collection<EvaFunction>> entry : functionMap.asMap().entrySet()) {
			for (final IFunctionMapHook functionMapHook : ca.functionMapHooks()) {
				if (functionMapHook.matches(entry.getKey())) {
					functionMapHook.apply(entry.getValue());
				}
			}
		}
	}

	public void handleIdteTypeCallbacks() {
		for (final Map.@NotNull Entry<IdentTableEntry, OnType> entry : idte_type_callbacks.entrySet()) {
			final IdentTableEntry idte = entry.getKey();
			if (idte.getType() != null && // TODO make a stage where this gets set (resolvePotentialTypes)
					idte.getType().getAttached() != null)
				entry.getValue().typeDeduced(idte.getType().getAttached());
			else
				entry.getValue().noTypeFound();
		}
	}

	public void handleOnClassCallbacks() {
		// TODO rewrite with classInvocationMultimap
		for (final ClassStatement classStatement : onclasses.keySet()) {
			for (final EvaNode evaNode : generatedClasses) {
				if (evaNode instanceof final @NotNull EvaClass evaClass) {
					if (evaClass.getKlass() == classStatement) {
						final Collection<OnClass> ks = onclasses.get(classStatement);
						for (@NotNull final OnClass k : ks) {
							k.classFound(evaClass);
						}
					} else {
						@NotNull final Collection<EvaClass> cmv = evaClass.classMap.values();
						for (@NotNull final EvaClass aClass : cmv) {
							if (aClass.getKlass() == classStatement) {
								final Collection<OnClass> ks = onclasses.get(classStatement);
								for (@NotNull final OnClass k : ks) {
									k.classFound(evaClass);
								}
							}
						}
					}
				}
			}
		}
	}

	public void handleResolvedVariables() {
		for (final EvaNode evaNode : generatedClasses.copy()) {
			if (evaNode instanceof final @NotNull EvaContainer evaContainer) {
				final Collection<ResolvedVariables> x = resolved_variables.get(evaContainer.getElement());
				for (@NotNull final DeducePhase.ResolvedVariables resolvedVariables : x) {
					final @NotNull Maybe<EvaContainer.VarTableEntry> variable_m = evaContainer.getVariable(resolvedVariables.varName);

					assert !variable_m.isException();

					final @NotNull EvaContainer.VarTableEntry variable = variable_m.o;

					final TypeTableEntry type = resolvedVariables.identTableEntry.getType();
					if (type != null)
						variable.addPotentialTypes(List_of(type));
					variable.addPotentialTypes(resolvedVariables.identTableEntry.potentialTypes());
					variable.updatePotentialTypes(evaContainer);
				}
			}
		}
	}

	private void logProgress(final @NotNull DeducePhaseProvenance aProvenance, final Object o) {
		switch (aProvenance) {
		case DeduceTypes_create -> {
			List<? extends Object> l = (List<? extends Object>) o;
			DeduceTypes2 deduceTypes2 = (DeduceTypes2) l.get(0);
			List<EvaNode> lgf = ((GeneratedClasses) l.get(1)).generatedClasses;
			LOG.info("196 DeduceTypes " + deduceTypes2.getFileName());
			{
				final List<EvaNode> p = _inj().new_ArrayList__EvaNode();
				Iterables.addAll(p, lgf);
				LOG.info("197 lgf.size " + p.size());
			}
		}
		default -> throw new IllegalStateException("Unexpected value: " + aProvenance);
		}
	}

	public void modulePromise(final OS_Module aModule, final Consumer<DeduceTypes2> con) {
		iWantModules.put(aModule, con);
	}

	public @NotNull FunctionInvocation newFunctionInvocation(final BaseFunctionDef f, final @Nullable ProcTableEntry aO, final @NotNull IInvocation ci) {
		return _inj().new_FunctionInvocation(f, aO, ci, this.generatePhase);
	}

	public void onClass(final ClassStatement aClassStatement, final OnClass callback) {
		onclasses.put(aClassStatement, callback);
	}

	public void onType(final IdentTableEntry entry, final OnType callback) {
		idte_type_callbacks.put(entry, callback);
	}

	public @NotNull ClassInvocation registerClassInvocation(@NotNull final ClassInvocation aClassInvocation) {
		final RegisterClassInvocation rci = _inj().new_RegisterClassInvocation(this);
		return rci.registerClassInvocation(aClassInvocation);
	}

	public @NotNull ClassInvocation registerClassInvocation(final @NotNull ClassStatement aParent) {
		//return registerClassInvocation(_inj().new_ClassInvocation(aParent, null, aDeduceTypes2));
		return registerClassInvocation(_inj().new_ClassInvocation(aParent, null, new NULL_DeduceTypes2())); // !! 08/28
	}

	// helper function. no generics!
	public @Nullable ClassInvocation registerClassInvocation(@NotNull final ClassStatement aParent,
	                                                         final String aConstructorName,
	                                                         final @NotNull Supplier<DeduceTypes2> aDeduceTypes2) {
		//@Nullable ClassInvocation ci = _inj().new_ClassInvocation(aParent, aConstructorName, aDeduceTypes2);
		@Nullable ClassInvocation ci = _inj().new_ClassInvocation(aParent, aConstructorName, aDeduceTypes2); // !! 08/28
		ci = registerClassInvocation(ci);
		return ci;
	}

	public ClassInvocation registerClassInvocation(final RegisterClassInvocation_env env) {
		final var rci = env.phase().new RegisterClassInvocation();
		return rci.registerClassInvocation(env);
	}

	public void registerFound(final FoundElement foundElement) {
		foundElements.add(foundElement);
	}

	public NamespaceInvocation registerNamespaceInvocation(final NamespaceStatement aNamespaceStatement) {
		if (namespaceInvocationMap.containsKey(aNamespaceStatement))
			return namespaceInvocationMap.get(aNamespaceStatement);

		@NotNull final NamespaceInvocation nsi = _inj().new_NamespaceInvocation(aNamespaceStatement);
		namespaceInvocationMap.put(aNamespaceStatement, nsi);
		return nsi;
	}

	public void registerResolvedVariable(final IdentTableEntry identTableEntry, final OS_Element parent, final String varName) {
		resolved_variables.put(parent, _inj().new_ResolvedVariables(identTableEntry, parent, varName));
	}

	public void resolveAllVariableTableEntries() {
		@NotNull final List<EvaClass> gcs                           = _inj().new_ArrayList__EvaClass();
		boolean                       all_resolve_var_table_entries = false;
		while (!all_resolve_var_table_entries) {
			if (generatedClasses.size() == 0) break;
			for (final EvaNode evaNode : generatedClasses.copy()) {
				if (evaNode instanceof final @NotNull EvaClass evaClass) {
					all_resolve_var_table_entries = evaClass.resolve_var_table_entries(this); // TODO use a while loop to get all classes
				}
			}
		}
	}

	private void sanityChecks() {
		for (final EvaNode evaNode : generatedClasses) {
			if (evaNode instanceof final @NotNull EvaClass evaClass) {
				sanityChecks(evaClass.functionMap.values());
//				sanityChecks(generatedClass.constructors.values()); // TODO reenable
			} else if (evaNode instanceof final @NotNull EvaNamespace generatedNamespace) {
				sanityChecks(generatedNamespace.functionMap.values());
//				sanityChecks(generatedNamespace.constructors.values());
			}
		}
	}

	private void sanityChecks(@NotNull final Collection<EvaFunction> aGeneratedFunctions) {
		for (@NotNull final EvaFunction generatedFunction : aGeneratedFunctions) {
			for (@NotNull final IdentTableEntry identTableEntry : generatedFunction.idte_list) {
				switch (identTableEntry.getStatus()) {
				case UNKNOWN:
					assert !identTableEntry.hasResolvedElement();
					LOG.err(String.format("250 UNKNOWN idte %s in %s", identTableEntry, generatedFunction));
					break;
				case KNOWN:
					assert identTableEntry.hasResolvedElement();
					if (identTableEntry.getType() == null) {
						LOG.err(String.format("258 null type in KNOWN idte %s in %s", identTableEntry, generatedFunction));
					}
					break;
				case UNCHECKED: {


					LOG.err(String.format("255 UNCHECKED idte %s in %s", identTableEntry, generatedFunction));
					break;
				}
				}
				for (@NotNull final TypeTableEntry pot_tte : identTableEntry.potentialTypes()) {
					if (pot_tte.getAttached() == null) {
						LOG.err(String.format("267 null potential attached in %s in %s in %s", pot_tte, identTableEntry, generatedFunction));
					}
				}
			}
		}
	}

	public void setGeneratedClassParents() {
		// TODO all EvaFunction nodes have a genClass member
		for (final EvaNode evaNode : generatedClasses) {
			if (evaNode instanceof final @NotNull EvaClass evaClass) {
				@NotNull final Collection<EvaFunction> functions = evaClass.functionMap.values();
				for (@NotNull final EvaFunction generatedFunction : functions) {
					generatedFunction.setParent(evaClass);
				}
			} else if (evaNode instanceof final @NotNull EvaNamespace generatedNamespace) {
				@NotNull final Collection<EvaFunction> functions = generatedNamespace.functionMap.values();
				for (@NotNull final EvaFunction generatedFunction : functions) {
					generatedFunction.setParent(generatedNamespace);
				}
			}
		}
	}

	public void waitOn(final DeduceTypes2 aDeduceTypes2) {
		waits.add(aDeduceTypes2);
	}

}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
