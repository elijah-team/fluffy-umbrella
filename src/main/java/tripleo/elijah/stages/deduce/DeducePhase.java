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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.diagnostic.Diagnostic;
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
import tripleo.elijah.stages.deduce.declarations.DeferredMember;
import tripleo.elijah.stages.deduce.declarations.DeferredMemberFunction;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_IdentTableEntry;
import tripleo.elijah.stages.deduce.post_bytecode.State;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GenerateFunctions;
import tripleo.elijah.stages.gen_fn.GeneratePhase;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedContainer;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.WlGenerateClass;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 12/24/20 3:59 AM
 */
public class DeducePhase {

	public final    ICodeRegistrar               codeRegistrar;
	public final    GeneratePhase                generatePhase;
	private final   List<FoundElement>           foundElements       = new ArrayList<FoundElement>();
	private final   Map<IdentTableEntry, OnType> idte_type_callbacks = new HashMap<IdentTableEntry, OnType>();
	public @NotNull GeneratedClasses             generatedClasses    = new GeneratedClasses();

	final PipelineLogic pipelineLogic;

	private final @NotNull ElLog LOG;

	private final List<State> registeredStates = new ArrayList<>();
	private final Compilation _compilation;

	public void addFunction(final GeneratedFunction generatedFunction, final FunctionDef fd) {
		functionMap.put(fd, generatedFunction);
	}

	public void registerFound(final FoundElement foundElement) {
		foundElements.add(foundElement);
	}

	public void onType(final IdentTableEntry entry, final OnType callback) {
		idte_type_callbacks.put(entry, callback);
	}

	@NotNull Multimap<OS_Element, ResolvedVariables> resolved_variables = ArrayListMultimap.create();

	public void registerResolvedVariable(final IdentTableEntry identTableEntry, final OS_Element parent, final String varName) {
		resolved_variables.put(parent, new ResolvedVariables(identTableEntry, parent, varName));
	}

	@NotNull Multimap<ClassStatement, OnClass> onclasses = ArrayListMultimap.create();

	public void onClass(final ClassStatement aClassStatement, final OnClass callback) {
		onclasses.put(aClassStatement, callback);
	}

//	Multimap<GeneratedClass, ClassInvocation> generatedClasses1 = ArrayListMultimap.create();
@NotNull Multimap<ClassStatement, ClassInvocation> classInvocationMultimap = ArrayListMultimap.create();

	public DeducePhase(final GeneratePhase aGeneratePhase,
	                   final PipelineLogic aPipelineLogic,
	                   final ElLog.Verbosity verbosity,
	                   final Compilation aCompilation) {
		generatePhase = aGeneratePhase;
		pipelineLogic = aPipelineLogic;
		//
		LOG = new ElLog("(DEDUCE_PHASE)", verbosity, "DeducePhase");
		pipelineLogic.addLog(LOG);
		//
		codeRegistrar = new DefaultCodeRegistrar(aCompilation);
		_compilation  = aCompilation;
		//
		IStateRunnable.ST.register(this);
		DeduceElement3_IdentTableEntry.ST.register(this);
	}

	public boolean equivalentGenericPart(@NotNull final ClassInvocation first, @NotNull final ClassInvocation second) {
		final Map<TypeName, OS_Type> firstGenericPart = first.genericPart;
		final Map<TypeName, OS_Type> secondGenericPart = second.genericPart;
		if (secondGenericPart == null && (firstGenericPart == null || firstGenericPart.size() == 0)) return true;
		//
		int i = secondGenericPart.entrySet().size();
		for (final Map.@NotNull Entry<TypeName, OS_Type> entry : secondGenericPart.entrySet()) {
			final OS_Type entry_type = firstGenericPart.get(entry.getKey());
			assert !(entry_type instanceof OS_UnknownType);
			if (entry_type.equals(entry.getValue()))
				i--;
//				else
//					return aClassInvocation;
		}
		return i == 0;
	}

	final Map<NamespaceStatement, NamespaceInvocation> namespaceInvocationMap = new HashMap<NamespaceStatement, NamespaceInvocation>();

	public NamespaceInvocation registerNamespaceInvocation(final NamespaceStatement aNamespaceStatement) {
		if (namespaceInvocationMap.containsKey(aNamespaceStatement))
			return namespaceInvocationMap.get(aNamespaceStatement);

		@NotNull final NamespaceInvocation nsi = new NamespaceInvocation(aNamespaceStatement);
		namespaceInvocationMap.put(aNamespaceStatement, nsi);
		return nsi;
	}

	@NotNull
	public List<FunctionMapHook> functionMapHooks = new ArrayList<FunctionMapHook>();

	public void addFunctionMapHook(final FunctionMapHook aFunctionMapHook) {
		functionMapHooks.add(aFunctionMapHook);
	}

	@NotNull List<DeferredMember> deferredMembers = new ArrayList<DeferredMember>();

	public void addDeferredMember(final DeferredMember aDeferredMember) {
		deferredMembers.add(aDeferredMember);
	}

	private final ExecutorService              classGenerator          = Executors.newCachedThreadPool();
	private final List<DeferredMemberFunction> deferredMemberFunctions = new ArrayList<>();

//	public List<ElLog> deduceLogs = new ArrayList<ElLog>();

	public void addLog(final ElLog aLog) {
		//deduceLogs.add(aLog);
		pipelineLogic.addLog(aLog);
	}

	// helper function. no generics!
	public @Nullable ClassInvocation registerClassInvocation(final ClassStatement aParent, final String aO) {
		@Nullable ClassInvocation ci = new ClassInvocation(aParent, aO);
		ci = registerClassInvocation(ci);
		return ci;
	}

	public void addDeferredMember(final DeferredMemberFunction aDeferredMemberFunction) {
		deferredMemberFunctions.add(aDeferredMemberFunction);
	}

	public ClassInvocation registerClassInvocation(final ClassStatement aParent) {
		@Nullable ClassInvocation ci = new ClassInvocation(aParent, null);
		ci = registerClassInvocation(ci);
		return ci;
	}

	public @Nullable ClassInvocation registerClassInvocation(@NotNull final ClassInvocation aClassInvocation) {
		boolean put = false;
		@Nullable ClassInvocation Result = null;

		// 1. select which to return
		final ClassStatement c = aClassInvocation.getKlass();
		final Collection<ClassInvocation> cis = classInvocationMultimap.get(c);
		for (@NotNull final ClassInvocation ci : cis) {
			// don't lose information
			if (ci.getConstructorName() != null)
				if (!(ci.getConstructorName().equals(aClassInvocation.getConstructorName())))
					continue;

			final boolean i = equivalentGenericPart(aClassInvocation, ci);
			if (i) {
				Result = ci;
				break;
			}
		}

		if (Result == null) {
			put = true;
			Result = aClassInvocation;
		}

		// 2. Check and see if already done
		final Collection<ClassInvocation> cls = classInvocationMultimap.get(Result.getKlass());
		for (@NotNull final ClassInvocation ci : cls) {
			if (equivalentGenericPart(ci, Result)) {
				return ci;
			}
		}

		if (put) {
			classInvocationMultimap.put(aClassInvocation.getKlass(), aClassInvocation);
		}

		// 3. Generate new GeneratedClass
		final @NotNull WorkList wl = new WorkList();
		final @NotNull OS_Module mod = Result.getKlass().getContext().module();
		wl.addJob(new WlGenerateClass(generatePhase.getGenerateFunctions(mod), Result, generatedClasses, codeRegistrar)); // TODO why add now?
		generatePhase.wm.addJobs(wl);
		generatePhase.wm.drain(); // TODO find a better place to put this

		// 4. Return it
		return Result;
	}

	public @NotNull DeduceTypes2 deduceModule(@NotNull final OS_Module m, final ElLog.Verbosity verbosity) {
		generatePhase.getGenerateFunctions(m)
		             .generateFromEntryPoints(m.entryPoints, this);

		final @NotNull List<GeneratedNode> lgf = new ArrayList<GeneratedNode>();
		for (final GeneratedNode lgci : generatedClasses) {
			if (lgci instanceof GeneratedClass) {
				final List<GeneratedFunction> generatedFunctions = (((GeneratedClass) lgci).functionMap.values())
				  .stream()
				  .map((final GeneratedFunction generatedFunction) -> {
					  generatedFunction.setClass(lgci);
					  return generatedFunction;
				  })
				  .collect(Collectors.toList());
				lgf.addAll(generatedFunctions);
			} else if (lgci instanceof GeneratedNamespace) {
				final List<GeneratedFunction> generatedFunctions = (((GeneratedNamespace) lgci).functionMap.values())
				  .stream()
				  .map((final GeneratedFunction generatedFunction) -> {
					  generatedFunction.setClass(lgci);
					  return generatedFunction;
				  })
				  .collect(Collectors.toList());
				lgf.addAll(generatedFunctions);
			}
		}

		return deduceModule(m, lgf, verbosity);
	}

	private final Multimap<FunctionDef, GeneratedFunction> functionMap = ArrayListMultimap.create();

	public @NotNull DeduceTypes2 deduceModule(@NotNull final OS_Module m, @NotNull final Iterable<GeneratedNode> lgf, final ElLog.Verbosity verbosity) {
		final @NotNull DeduceTypes2 deduceTypes2 = new DeduceTypes2(m, this, verbosity);
		LOG.err("196 DeduceTypes " + deduceTypes2.getFileName());
		{
			final ArrayList<GeneratedNode> p = new ArrayList<GeneratedNode>();
			Iterables.addAll(p, lgf);
			LOG.info("197 lgf.size " + p.size());
		}

		deduceTypes2.deduceFunctions(lgf);

		final List<GeneratedClass> matching_class_list = generatedClasses.filterClassesByModule(m);

//		assert matching_class_list.size() == generatedClasses.size();

		deduceTypes2.deduceClasses(matching_class_list);

		return deduceTypes2;
	}

	public @NotNull Promise<ClassDefinition, Diagnostic, Void> generateClass(final GenerateFunctions gf, final ClassInvocation ci) {
		@Nullable final WlGenerateClass                         gen = new WlGenerateClass(gf, ci, generatedClasses, codeRegistrar);
		final ClassDefinition[]                                 cds = new ClassDefinition[1];
		final DeferredObject<ClassDefinition, Diagnostic, Void> ret = new DeferredObject<>();

		classGenerator.submit(new Runnable() {
			@Override
			public void run() {
				gen.run(null);
				final ClassDefinition cd       = new ClassDefinition(ci);
				final GeneratedClass  genclass = gen.getResult();
				if (genclass != null) {
					cd.setNode(genclass);
					cds[0] = cd;
					ret.resolve(cd);
				} else {
					ret.reject(new CouldntGenerateClass(cd, gf, ci));
				}
			}
		});

		return ret;
	}

	public State register(final State aState) {
		if (!(registeredStates.contains(aState))) {
			registeredStates.add(aState);

			final int id = registeredStates.indexOf(aState);

			aState.setIdentity(id);
			return aState;
		}

		return aState;
	}

	public Compilation _compilation() {
		return _compilation;
	}

	static class ResolvedVariables {
		final IdentTableEntry identTableEntry;
		final OS_Element      parent; // README tripleo.elijah.lang._CommonNC, but that's package-private
		final String          varName;

		public ResolvedVariables(final IdentTableEntry aIdentTableEntry, final OS_Element aParent, final String aVarName) {
			assert aParent instanceof ClassStatement || aParent instanceof NamespaceStatement;

			identTableEntry = aIdentTableEntry;
			parent          = aParent;
			varName         = aVarName;
		}
	}

	/**
	 * Use this when you have already called generateAllTopLevelClasses
	 * @param m the module
	 * @param lgc the result of generateAllTopLevelClasses
	 * @param _unused is unused
	 * @param verbosity
	 */
	public void deduceModule(@NotNull final OS_Module m, @NotNull final Iterable<GeneratedNode> lgc, final boolean _unused, final ElLog.Verbosity verbosity) {
		final @NotNull List<GeneratedNode> lgf = new ArrayList<GeneratedNode>();

		for (@NotNull final GeneratedNode lgci : lgc) {
			if (lgci.module() != m) continue;

			if (lgci instanceof GeneratedClass) {
				final @NotNull Collection<GeneratedFunction> generatedFunctions = ((GeneratedClass) lgci).functionMap.values();
				for (@NotNull final GeneratedFunction generatedFunction : generatedFunctions) {
//					generatedFunction.setClass(lgci); // TODO delete when done
					assert generatedFunction.getGenClass() == lgci;
				}
				lgf.addAll(generatedFunctions);
			} else if (lgci instanceof GeneratedNamespace) {
				final @NotNull Collection<GeneratedFunction> generatedFunctions = ((GeneratedNamespace) lgci).functionMap.values();
				for (@NotNull final GeneratedFunction generatedFunction : generatedFunctions) {
					generatedFunction.setClass(lgci);
				}
				lgf.addAll(generatedFunctions);
			}
		}

		deduceModule(m, lgf, verbosity);
	}

	public void forFunction(final DeduceTypes2 deduceTypes2, @NotNull final FunctionInvocation fi, @NotNull final ForFunction forFunction) {
//		LOG.err("272 forFunction\n\t"+fi.getFunction()+"\n\t"+fi.pte);
		fi.generateDeferred().promise().then(new DoneCallback<BaseGeneratedFunction>() {
			@Override
			public void onDone(@NotNull final BaseGeneratedFunction result) {
				result.onType(new DoneCallback<GenType>() {
					@Override
					public void onDone(final GenType result) {
						forFunction.typeDecided(result);
					}
				});
			}
		});
	}

//	Map<GeneratedFunction, OS_Type> typeDecideds = new HashMap<GeneratedFunction, OS_Type>();

	public void typeDecided(@NotNull final GeneratedFunction gf, final GenType aType) {
		gf.resolveTypeDeferred(aType);
//		typeDecideds.put(gf, aType);
	}

	public void finish(@NotNull final GeneratedClasses lgc22) {
		// TODO all GeneratedFunction nodes have a genClass member
		for (final GeneratedNode generatedNode : lgc22) {
			if (generatedNode instanceof final @NotNull GeneratedClass generatedClass) {
				@NotNull final Collection<GeneratedFunction> functions = generatedClass.functionMap.values();
				for (@NotNull final GeneratedFunction generatedFunction : functions) {
					generatedFunction.setParent(generatedClass);
				}
			} else if (generatedNode instanceof final @NotNull GeneratedNamespace generatedNamespace) {
				@NotNull final Collection<GeneratedFunction> functions = generatedNamespace.functionMap.values();
				for (@NotNull final GeneratedFunction generatedFunction : functions) {
					generatedFunction.setParent(generatedNamespace);
				}
			}
		}
/*
		for (GeneratedNode generatedNode : generatedClasses) {
			if (generatedNode instanceof GeneratedClass) {
				final GeneratedClass generatedClass = (GeneratedClass) generatedNode;
				final ClassStatement cs = generatedClass.getKlass();
				Collection<ClassInvocation> cis = classInvocationMultimap.get(cs);
				for (ClassInvocation ci : cis) {
					if (equivalentGenericPart(generatedClass.ci, ci)) {
						final DeferredObject<GeneratedClass, Void, Void> deferredObject = (DeferredObject<GeneratedClass, Void, Void>) ci.promise();
						deferredObject.then(new DoneCallback<GeneratedClass>() {
							@Override
							public void onDone(GeneratedClass result) {
								assert result == generatedClass;
							}
						});
//						deferredObject.resolve(generatedClass);
					}
				}
			}
		}
*/
		// TODO rewrite with classInvocationMultimap
		for (final ClassStatement classStatement : onclasses.keySet()) {
			for (final GeneratedNode generatedNode : lgc22) {
				if (generatedNode instanceof final @NotNull GeneratedClass generatedClass) {
					if (generatedClass.getKlass() == classStatement) {
						final Collection<OnClass> ks = onclasses.get(classStatement);
						for (@NotNull final OnClass k : ks) {
							k.classFound(generatedClass);
						}
					} else {
						@NotNull final Collection<GeneratedClass> cmv = generatedClass.classMap.values();
						for (@NotNull final GeneratedClass aClass : cmv) {
							if (aClass.getKlass() == classStatement) {
								final Collection<OnClass> ks = onclasses.get(classStatement);
								for (@NotNull final OnClass k : ks) {
									k.classFound(generatedClass);
								}
							}
						}
					}
				}
			}
		}
		for (final Map.@NotNull Entry<IdentTableEntry, OnType> entry : idte_type_callbacks.entrySet()) {
			final IdentTableEntry idte = entry.getKey();
			if (idte.type !=null && // TODO make a stage where this gets set (resolvePotentialTypes)
					idte.type.getAttached() != null)
				entry.getValue().typeDeduced(idte.type.getAttached());
			else
				entry.getValue().noTypeFound();
		}
/*
		for (Map.Entry<GeneratedFunction, OS_Type> entry : typeDecideds.entrySet()) {
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
		for (Map.Entry<FunctionDef, GeneratedFunction> entry : functionMap.entries()) {
			FunctionInvocation fi = new FunctionInvocation(entry.getKey(), null);
			for (Triplet triplet : forFunctions) {
//				Collection<GeneratedFunction> x = functionMap.get(fi);
				triplet.forFunction.finish();
			}
		}
*/
		for (@NotNull final FoundElement foundElement : foundElements) {
			// TODO As we are using this, didntFind will never fail because
			//  we call doFoundElement manually in resolveIdentIA
			//  As the code matures, maybe this will change and the interface
			//  will be improved, namely calling doFoundElement from here as well
			if (foundElement.didntFind()) {
				foundElement.doNoFoundElement();
			}
		}
		for (final GeneratedNode generatedNode : lgc22) {
			if (generatedNode instanceof final @NotNull GeneratedContainer generatedContainer) {
				final Collection<ResolvedVariables> x = resolved_variables.get(generatedContainer.getElement());
				for (@NotNull final ResolvedVariables resolvedVariables : x) {
					final GeneratedContainer.VarTableEntry variable = generatedContainer.getVariable(resolvedVariables.varName);
					assert variable != null;
					final TypeTableEntry type = resolvedVariables.identTableEntry.type;
					if (type != null)
						variable.addPotentialTypes(List_of(type));
					variable.addPotentialTypes(resolvedVariables.identTableEntry.potentialTypes());
				}
			}
		}
		@NotNull final List<GeneratedClass> gcs = new ArrayList<GeneratedClass>();
		boolean all_resolve_var_table_entries = false;
		while (!all_resolve_var_table_entries) {
			if (lgc22.size() == 0) break;
			for (final GeneratedNode generatedNode : lgc22.copy()) {
				if (generatedNode instanceof final @NotNull GeneratedClass generatedClass) {
					all_resolve_var_table_entries = generatedClass.resolve_var_table_entries(this); // TODO use a while loop to get all classes
				}
			}
		}
		for (@NotNull final DeferredMember deferredMember : deferredMembers) {
			if (deferredMember.getParent() instanceof final @NotNull NamespaceStatement parent) {
				final NamespaceInvocation nsi = registerNamespaceInvocation(parent);
				nsi.resolveDeferred()
						.done(new DoneCallback<GeneratedNamespace>() {
							@Override
							public void onDone(@NotNull final GeneratedNamespace result) {
								final GeneratedContainer.@Nullable VarTableEntry v = result.getVariable(deferredMember.getVariableStatement().getName());
								assert v != null;
								// TODO varType, potentialTypes and _resolved: which?
								final OS_Type          varType = v.varType;
								final @NotNull GenType genType = new GenType();
								genType.set(varType);

//								if (deferredMember.getInvocation() instanceof NamespaceInvocation) {
//									((NamespaceInvocation) deferredMember.getInvocation()).resolveDeferred().done(new DoneCallback<GeneratedNamespace>() {
//										@Override
//										public void onDone(GeneratedNamespace result) {
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
							}
						});
			} else if (deferredMember.getParent() instanceof final ClassStatement parent) {
				// TODO do something
				final String name = deferredMember.getVariableStatement().getName();

				// because deferredMember.invocation is null, we must create one here
				final @Nullable ClassInvocation ci = registerClassInvocation(parent, null);
				ci.resolvePromise().then(new DoneCallback<GeneratedClass>() {
					@Override
					public void onDone(final GeneratedClass result) {
						final List<GeneratedContainer.VarTableEntry> vt = result.varTable;
						for (final GeneratedContainer.VarTableEntry gc_vte : vt) {
							if (gc_vte.nameToken.getText().equals(name)) {
								// check connections
								// unify pot. types (prol. shuld be done already -- we don't want to be reporting errors here)
								// call typePromises and externalRefPromisess

								// TODO just getting first element here (without processing of any kind); HACK
								final GenType ty = gc_vte.connectionPairs.get(0).vte.type.genType;
								assert ty.resolved != null;
								gc_vte.varType = ty.resolved; // TODO make sure this is right in all cases
								if (deferredMember.typeResolved().isPending())
									deferredMember.typeResolved().resolve(ty);
								break;
							}
						}
					}
				});
			} else
				throw new NotImplementedException();
		}
		sanityChecks();
		for (final Map.@NotNull Entry<FunctionDef, Collection<GeneratedFunction>> entry : functionMap.asMap().entrySet()) {
			for (@NotNull final FunctionMapHook functionMapHook : functionMapHooks) {
				if (functionMapHook.matches(entry.getKey())) {
					functionMapHook.apply(entry.getValue());
				}
			}
		}
	}

	private void sanityChecks() {
		for (final GeneratedNode generatedNode : generatedClasses) {
			if (generatedNode instanceof final @NotNull GeneratedClass generatedClass) {
				sanityChecks(generatedClass.functionMap.values());
//				sanityChecks(generatedClass.constructors.values()); // TODO reenable
			} else if (generatedNode instanceof final @NotNull GeneratedNamespace generatedNamespace) {
				sanityChecks(generatedNamespace.functionMap.values());
//				sanityChecks(generatedNamespace.constructors.values());
			}
		}
	}

	private void sanityChecks(@NotNull final Collection<GeneratedFunction> aGeneratedFunctions) {
		for (@NotNull final GeneratedFunction generatedFunction : aGeneratedFunctions) {
			for (@NotNull final IdentTableEntry identTableEntry : generatedFunction.idte_list) {
				switch (identTableEntry.getStatus()) {
					case UNKNOWN:
						assert identTableEntry.getResolvedElement() == null;
						LOG.err(String.format("250 UNKNOWN idte %s in %s", identTableEntry, generatedFunction));
						break;
					case KNOWN:
						assert identTableEntry.getResolvedElement() != null;
						if (identTableEntry.type == null) {
							LOG.err(String.format("258 null type in KNOWN idte %s in %s", identTableEntry, generatedFunction));
						}
						break;
					case UNCHECKED:
						LOG.err(String.format("255 UNCHECKED idte %s in %s", identTableEntry, generatedFunction));
						break;
				}
				for (@NotNull final TypeTableEntry pot_tte : identTableEntry.potentialTypes()) {
					if (pot_tte.getAttached() == null) {
						LOG.err(String.format("267 null potential attached in %s in %s in %s", pot_tte, identTableEntry, generatedFunction));
					}
				}
			}
		}
	}

	public static class GeneratedClasses implements Iterable<GeneratedNode> {
		@NotNull
		public List<GeneratedNode> generatedClasses = new ArrayList<GeneratedNode>();

		public void add(final GeneratedNode aClass) {
			generatedClasses.add(aClass);
		}

		@Override
		public Iterator<GeneratedNode> iterator() {
			return generatedClasses.iterator();
		}

		public int size() {
			return generatedClasses.size();
		}

		public List<GeneratedNode> copy() {
			return new ArrayList<GeneratedNode>(generatedClasses);
		}

		public void addAll(final List<GeneratedNode> lgc) {
			// TODO is this method really needed
			generatedClasses.addAll(lgc);
		}

		public List<GeneratedClass> filterClasses(final Predicate<GeneratedClass> pgc) {
			return generatedClasses
					.stream()
					.filter(x -> {
						if (x instanceof GeneratedClass) {
							return pgc.test((GeneratedClass) x);
						} else {
							return false;
						}
					})
					.map(x -> (GeneratedClass) x)
					.collect(Collectors.toList());
		}

		public List<GeneratedClass> filterClassesByModule(final OS_Module aModule) {
			return filterClasses(c -> c.module() == aModule);
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
