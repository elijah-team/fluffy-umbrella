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
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_UnknownType;
import tripleo.elijah.nextgen.ClassDefinition;
import tripleo.elijah.nextgen.diagnostic.CouldntGenerateClass;
import tripleo.elijah.stages.deduce.declarations.DeferredMember;
import tripleo.elijah.stages.deduce.declarations.DeferredMemberFunction;
import tripleo.elijah.util.Maybe;
import tripleo.elijah.stateful.State;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkList;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 12/24/20 3:59 AM
 */
public class DeducePhase {

	public final           GeneratePhase                generatePhase;
	public final @NotNull  GeneratedClasses             generatedClasses    = new GeneratedClasses();
	final Map<NamespaceStatement, NamespaceInvocation> namespaceInvocationMap = new HashMap<NamespaceStatement, NamespaceInvocation>();
	private final          List<FoundElement>           foundElements       = new ArrayList<FoundElement>();
	private final          Map<IdentTableEntry, OnType> idte_type_callbacks = new HashMap<IdentTableEntry, OnType>();
	private final          PipelineLogic                pipelineLogic;
	private final @NotNull ElLog                        LOG;
	private final @NotNull ICompilationAccess           ca;
//	private final Compilation _compilation;
	private final List<State> registeredStates = new ArrayList<>();
	private final ExecutorService classGenerator = Executors.newCachedThreadPool();
	private final Multimap<FunctionDef, EvaFunction> functionMap        = ArrayListMultimap.create();
	@NotNull
	public List<IFunctionMapHook>                    functionMapHooks   = new ArrayList<IFunctionMapHook>();
	@NotNull Multimap<OS_Element, ResolvedVariables> resolved_variables = ArrayListMultimap.create();
	@NotNull Multimap<ClassStatement, OnClass> onclasses = ArrayListMultimap.create();
	//	Multimap<EvaClass, ClassInvocation> generatedClasses1 = ArrayListMultimap.create();
	@NotNull Multimap<ClassStatement, ClassInvocation> classInvocationMultimap = ArrayListMultimap.create();
	@NotNull List<DeferredMember> deferredMembers = new ArrayList<DeferredMember>();
	private List<DeferredMemberFunction> deferredMemberFunctions = new ArrayList<>();

	public DeducePhase(final GeneratePhase aGeneratePhase,
					   final PipelineLogic aPipelineLogic,
					   final ElLog.Verbosity verbosity,
					   final @NotNull ICompilationAccess aca) {
		generatePhase = aGeneratePhase;
		pipelineLogic = aPipelineLogic;
		ca            = aca;
		//
		LOG = new ElLog("(DEDUCE_PHASE)", verbosity, "DeducePhase");
		//
		pipelineLogic.addLog(LOG);
	}

	public void addFunction(EvaFunction generatedFunction, FunctionDef fd) {
		functionMap.put(fd, generatedFunction);
	}

	public void registerFound(FoundElement foundElement) {
		foundElements.add(foundElement);
	}

	public void onType(IdentTableEntry entry, OnType callback) {
		idte_type_callbacks.put(entry, callback);
	}

	public void registerResolvedVariable(IdentTableEntry identTableEntry, OS_Element parent, String varName) {
		resolved_variables.put(parent, new ResolvedVariables(identTableEntry, parent, varName));
	}

	public void onClass(ClassStatement aClassStatement, OnClass callback) {
		onclasses.put(aClassStatement, callback);
	}

	public void addDeferredMember(final DeferredMemberFunction aDeferredMemberFunction) {
		deferredMemberFunctions.add(aDeferredMemberFunction);
	}

	public Promise<ClassDefinition, Diagnostic, Void> generateClass(final GenerateFunctions gf, final ClassInvocation ci) {
		@Nullable WlGenerateClass                               gen   = new WlGenerateClass(gf, ci, generatedClasses);
		ClassDefinition                                         cds[] = new ClassDefinition[1];
		final DeferredObject<ClassDefinition, Diagnostic, Void> ret   = new DeferredObject<>();

		classGenerator.submit(new Runnable() {
			@Override
			public void run() {
				gen.run(null);
				final ClassDefinition cd       = new ClassDefinition(ci);
				final EvaClass        genclass = gen.getResult();
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

	public ClassInvocation registerClassInvocation(final ClassStatement aParent) {
		return registerClassInvocation(new ClassInvocation(aParent, null));
	}

	public @NotNull ClassInvocation registerClassInvocation(@NotNull ClassInvocation aClassInvocation) {
		RegisterClassInvocation rci = new RegisterClassInvocation();
		return rci.registerClassInvocation(aClassInvocation);
	}

	public boolean equivalentGenericPart(@NotNull ClassInvocation first, @NotNull ClassInvocation second) {
		Map<TypeName, OS_Type> firstGenericPart  = first.genericPart;
		Map<TypeName, OS_Type> secondGenericPart = second.genericPart;
		if (secondGenericPart == null && (firstGenericPart == null || firstGenericPart.size() == 0)) return true;
		//
		int i = secondGenericPart.entrySet().size();
		for (Map.@NotNull Entry<TypeName, OS_Type> entry : secondGenericPart.entrySet()) {
			final OS_Type entry_type = firstGenericPart.get(entry.getKey());
			assert !(entry_type instanceof OS_UnknownType);
			if (entry_type.equals(entry.getValue()))
				i--;
//				else
//					return aClassInvocation;
		}
		return i == 0;
	}

	public void addFunctionMapHook(IFunctionMapHook aFunctionMapHook) {
		functionMapHooks.add(aFunctionMapHook);
	}

	public void addDeferredMember(DeferredMember aDeferredMember) {
		deferredMembers.add(aDeferredMember);
	}

	public void addLog(ElLog aLog) {
		//deduceLogs.add(aLog);
		pipelineLogic.addLog(aLog);
	}

//	public List<ElLog> deduceLogs = new ArrayList<ElLog>();

	public @NotNull DeduceTypes2 deduceModule(@NotNull OS_Module m, @NotNull Iterable<EvaNode> lgf, ElLog.Verbosity verbosity) {
		final @NotNull DeduceTypes2 deduceTypes2 = new DeduceTypes2(m, this, verbosity);
//		LOG.err("196 DeduceTypes "+deduceTypes2.getFileName());
		{
			final ArrayList<EvaNode> p = new ArrayList<EvaNode>();
			Iterables.addAll(p, lgf);
			LOG.info("197 lgf.size " + p.size());
		}
		deduceTypes2.deduceFunctions(lgf);
//		deduceTypes2.deduceClasses(generatedClasses.copy().stream()
//				.filter(c -> c.module() == m)
//				.collect(Collectors.toList()));

		for (EvaNode evaNode : generatedClasses.copy()) {
			if (evaNode.module() != m) continue;

			if (evaNode instanceof EvaClass) {
				final EvaClass evaClass = (EvaClass) evaNode;

				evaClass.fixupUserClasses(deduceTypes2, evaClass.getKlass().getContext());
				deduceTypes2.deduceOneClass(evaClass);
			}
		}

		for (EvaNode evaNode : lgf) {
			final BaseEvaFunction  bef;

			if (evaNode instanceof BaseEvaFunction) {
				bef = (BaseEvaFunction) evaNode;
			} else continue;
			for (IFunctionMapHook hook : functionMapHooks) {
				if (hook.matches((FunctionDef) bef.getFD())) {
					hook.apply(List_of((EvaFunction) bef));
				}
			}
		}

		return deduceTypes2;
	}

	public void forFunction(DeduceTypes2 deduceTypes2, @NotNull FunctionInvocation fi, @NotNull ForFunction forFunction) {
//		LOG.err("272 forFunction\n\t"+fi.getFunction()+"\n\t"+fi.pte);
		fi.generateDeferred().promise().then(new DoneCallback<BaseEvaFunction>() {
			@Override
			public void onDone(@NotNull BaseEvaFunction result) {
				result.typePromise().then(new DoneCallback<GenType>() {
					@Override
					public void onDone(GenType result) {
						forFunction.typeDecided(result);
					}
				});
			}
		});
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
			FunctionInvocation fi = new FunctionInvocation(entry.getKey(), null);
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
	}

	public void setGeneratedClassParents() {
		// TODO all EvaFunction nodes have a genClass member
		for (EvaNode evaNode : generatedClasses) {
			if (evaNode instanceof EvaClass) {
				final @NotNull EvaClass          evaClass  = (EvaClass) evaNode;
				@NotNull Collection<EvaFunction> functions = evaClass.functionMap.values();
				for (@NotNull EvaFunction generatedFunction : functions) {
					generatedFunction.setParent(evaClass);
				}
			} else if (evaNode instanceof EvaNamespace) {
				final @NotNull EvaNamespace      generatedNamespace = (EvaNamespace) evaNode;
				@NotNull Collection<EvaFunction> functions          = generatedNamespace.functionMap.values();
				for (@NotNull EvaFunction generatedFunction : functions) {
					generatedFunction.setParent(generatedNamespace);
				}
			}
		}
	}

	public void handleOnClassCallbacks() {
		// TODO rewrite with classInvocationMultimap
		for (ClassStatement classStatement : onclasses.keySet()) {
			for (EvaNode evaNode : generatedClasses) {
				if (evaNode instanceof EvaClass) {
					final @NotNull EvaClass evaClass = (EvaClass) evaNode;
					if (evaClass.getKlass() == classStatement) {
						Collection<OnClass> ks = onclasses.get(classStatement);
						for (@NotNull OnClass k : ks) {
							k.classFound(evaClass);
						}
					} else {
						@NotNull Collection<EvaClass> cmv = evaClass.classMap.values();
						for (@NotNull EvaClass aClass : cmv) {
							if (aClass.getKlass() == classStatement) {
								Collection<OnClass> ks = onclasses.get(classStatement);
								for (@NotNull OnClass k : ks) {
									k.classFound(evaClass);
								}
							}
						}
					}
				}
			}
		}
	}

	public void handleIdteTypeCallbacks() {
		for (Map.@NotNull Entry<IdentTableEntry, OnType> entry : idte_type_callbacks.entrySet()) {
			IdentTableEntry idte = entry.getKey();
			if (idte.type != null && // TODO make a stage where this gets set (resolvePotentialTypes)
					idte.type.getAttached() != null)
				entry.getValue().typeDeduced(idte.type.getAttached());
			else
				entry.getValue().noTypeFound();
		}
	}

	public void handleFoundElements() {
		for (@NotNull FoundElement foundElement : foundElements) {
			// TODO As we are using this, didntFind will never fail because
			//  we call doFoundElement manually in resolveIdentIA
			//  As the code matures, maybe this will change and the interface
			//  will be improved, namely calling doFoundElement from here as well
			if (foundElement.didntFind()) {
				foundElement.doNoFoundElement();
			}
		}
	}

	public void handleResolvedVariables() {
		for (EvaNode evaNode : generatedClasses.copy()) {
			if (evaNode instanceof EvaContainer) {
				final @NotNull EvaContainer   evaContainer = (EvaContainer) evaNode;
				Collection<ResolvedVariables> x            = resolved_variables.get(evaContainer.getElement());
				for (@NotNull DeducePhase.ResolvedVariables resolvedVariables : x) {
					final @NotNull Maybe<EvaContainer.VarTableEntry> variable_m = evaContainer.getVariable(resolvedVariables.varName);

					if (variable_m.isException())
						assert false;

					final @NotNull EvaContainer.VarTableEntry variable = variable_m.o;

					final TypeTableEntry type = resolvedVariables.identTableEntry.type;
					if (type != null)
						variable.addPotentialTypes(List_of(type));
					variable.addPotentialTypes(resolvedVariables.identTableEntry.potentialTypes());
					variable.updatePotentialTypes(evaContainer);
				}
			}
		}
	}

	public void resolveAllVariableTableEntries() {
		@NotNull List<EvaClass> gcs                           = new ArrayList<EvaClass>();
		boolean                 all_resolve_var_table_entries = false;
		while (!all_resolve_var_table_entries) {
			if (generatedClasses.size() == 0) break;
			for (EvaNode evaNode : generatedClasses.copy()) {
				if (evaNode instanceof EvaClass) {
					final @NotNull EvaClass evaClass = (EvaClass) evaNode;
					all_resolve_var_table_entries = evaClass.resolve_var_table_entries(this); // TODO use a while loop to get all classes
				}
			}
		}
	}

	public void handleDeferredMemberFunctions() {
		for (@NotNull final DeferredMemberFunction deferredMemberFunction : deferredMemberFunctions) {
			int              y      = 2;
			final OS_Element parent = deferredMemberFunction.getParent();//.getParent().getParent();

			if (parent instanceof ClassStatement) {
				final IInvocation invocation = deferredMemberFunction.getInvocation();

				final DeferredMemberFunctionParentIsClassStatement dmfpic = new DeferredMemberFunctionParentIsClassStatement(deferredMemberFunction, invocation);
				dmfpic.action();
			} else if (parent instanceof NamespaceStatement) {
//				final ClassStatement classStatement = (ClassStatement) deferredMemberFunction.getParent();
				final NamespaceInvocation namespaceInvocation = (NamespaceInvocation) deferredMemberFunction.getInvocation();
				namespaceInvocation.resolvePromise().
						then(new DoneCallback<EvaNamespace>() {
							@Override
							public void onDone(final EvaNamespace result) {
								final NamespaceInvocation             x = namespaceInvocation;
								final @NotNull DeferredMemberFunction z = deferredMemberFunction;
								int                                   y = 2;
							}
						});
			}
		}

		for (EvaNode evaNode : generatedClasses) {
			if (evaNode instanceof EvaContainerNC) {
				final EvaContainerNC nc = (EvaContainerNC) evaNode;
				nc.noteDependencies(nc.getDependency()); // TODO is this right?

				for (EvaFunction generatedFunction : nc.functionMap.values()) {
					generatedFunction.noteDependencies(nc.getDependency());
				}
				if (nc instanceof EvaClass) {
					final EvaClass evaClass = (EvaClass) nc;

					for (EvaConstructor evaConstructor : evaClass.constructors.values()) {
						evaConstructor.noteDependencies(nc.getDependency());
					}
				}
			}
		}
	}

	public void handleDeferredMembers() {
		for (@NotNull final DeferredMember deferredMember : deferredMembers) {
			if (deferredMember.getParent() instanceof NamespaceStatement) {
				final @NotNull NamespaceStatement parent = (NamespaceStatement) deferredMember.getParent();
				final NamespaceInvocation         nsi    = registerNamespaceInvocation(parent);
				nsi.resolveDeferred()
						.done(new DoneCallback<EvaNamespace>() {
							@Override
							public void onDone(@NotNull EvaNamespace result) {
								@NotNull Maybe<EvaContainer.VarTableEntry> v_m = result.getVariable(deferredMember.getVariableStatement().getName());

								if (v_m.isException())
									assert false;

								EvaContainer.VarTableEntry v = v_m.o;

								// TODO varType, potentialTypes and _resolved: which?
								//final OS_Type varType = v.varType;

								v.resolve_varType_cb((varType) -> {
									final @NotNull GenType genType = new GenType();
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
							}
						});
			} else if (deferredMember.getParent() instanceof ClassStatement) {
				// TODO do something
				final ClassStatement parent = (ClassStatement) deferredMember.getParent();
				final String         name   = deferredMember.getVariableStatement().getName();

				// because deferredMember.invocation is null, we must create one here
				final @Nullable ClassInvocation ci = registerClassInvocation(parent, null);
				ci.resolvePromise().then(new DoneCallback<EvaClass>() {
					@Override
					public void onDone(final EvaClass result) {
						final List<EvaContainer.VarTableEntry> vt = result.varTable;
						for (EvaContainer.VarTableEntry gc_vte : vt) {
							if (gc_vte.nameToken.getText().equals(name)) {
								// check connections
								// unify pot. types (prol. shuld be done already -- we don't want to be reporting errors here)
								// call typePromises and externalRefPromisess

								// TODO just getting first element here (without processing of any kind); HACK
								final List<EvaContainer.VarTableEntry.ConnectionPair> connectionPairs = gc_vte.connectionPairs;
								if (connectionPairs.size() > 0) {
									final GenType ty = connectionPairs.get(0).vte.type.genType;
									assert ty.resolved != null;
									gc_vte.varType = ty.resolved; // TODO make sure this is right in all cases
									if (deferredMember.typeResolved().isPending())
										deferredMember.typeResolved().resolve(ty);
									break;
								} else {
									NotImplementedException.raise();
								}
							}
						}
					}
				});
			} else
				throw new NotImplementedException();
		}
	}

	private void sanityChecks() {
		for (EvaNode evaNode : generatedClasses) {
			if (evaNode instanceof EvaClass) {
				final @NotNull EvaClass evaClass = (EvaClass) evaNode;
				sanityChecks(evaClass.functionMap.values());
//				sanityChecks(generatedClass.constructors.values()); // TODO reenable
			} else if (evaNode instanceof EvaNamespace) {
				final @NotNull EvaNamespace generatedNamespace = (EvaNamespace) evaNode;
				sanityChecks(generatedNamespace.functionMap.values());
//				sanityChecks(generatedNamespace.constructors.values());
			}
		}
	}

	public void handleFunctionMapHooks() {
		for (Map.@NotNull Entry<FunctionDef, Collection<EvaFunction>> entry : functionMap.asMap().entrySet()) {
			for (@NotNull IFunctionMapHook functionMapHook : ca.functionMapHooks()) {
				if (functionMapHook.matches(entry.getKey())) {
					functionMapHook.apply(entry.getValue());
				}
			}
		}
	}

	public NamespaceInvocation registerNamespaceInvocation(NamespaceStatement aNamespaceStatement) {
		if (namespaceInvocationMap.containsKey(aNamespaceStatement))
			return namespaceInvocationMap.get(aNamespaceStatement);

		@NotNull NamespaceInvocation nsi = new NamespaceInvocation(aNamespaceStatement);
		namespaceInvocationMap.put(aNamespaceStatement, nsi);
		return nsi;
	}

	// helper function. no generics!
	public @Nullable ClassInvocation registerClassInvocation(ClassStatement aParent, String aO) {
		@Nullable ClassInvocation ci = new ClassInvocation((ClassStatement) aParent, aO);
		ci = registerClassInvocation(ci);
		return ci;
	}

	private void sanityChecks(@NotNull Collection<EvaFunction> aGeneratedFunctions) {
		for (@NotNull EvaFunction generatedFunction : aGeneratedFunctions) {
			for (@NotNull IdentTableEntry identTableEntry : generatedFunction.idte_list) {
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
				for (@NotNull TypeTableEntry pot_tte : identTableEntry.potentialTypes()) {
					if (pot_tte.getAttached() == null) {
						LOG.err(String.format("267 null potential attached in %s in %s in %s", pot_tte, identTableEntry, generatedFunction));
					}
				}
			}
		}
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
		return ca.getCompilation();
	}

	public DeduceTypes2 deduceModule(final OS_Module aMod) {
		return deduceModule(aMod, this.generatedClasses, Compilation.gitlabCIVerbosity());
	}

	static class ResolvedVariables {
		final IdentTableEntry identTableEntry;
		final OS_Element      parent; // README tripleo.elijah.lang._CommonNC, but that's package-private
		final String          varName;

		public ResolvedVariables(IdentTableEntry aIdentTableEntry, OS_Element aParent, String aVarName) {
			assert aParent instanceof ClassStatement || aParent instanceof NamespaceStatement;

			identTableEntry = aIdentTableEntry;
			parent          = aParent;
			varName         = aVarName;
		}
	}

	static class DeferredMemberFunctionParentIsClassStatement {
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

			if (p instanceof DeduceTypes2.OS_SpecialVariable) {
				final DeduceTypes2.OS_SpecialVariable specialVariable = (DeduceTypes2.OS_SpecialVariable) p;
				onSpecialVariable(specialVariable);
				int y = 2;
			} else if (p instanceof ClassStatement) {
				final Function<EvaNode, Map<FunctionDef, EvaFunction>> x = getFunctionMap(result);

				// once again we need EvaFunction, not FunctionDef
				// we seem to have it below, but there can be multiple
				// specializations of each function

				final EvaFunction gf = x.apply(result).get((FunctionDef) deferredMemberFunction.getFunctionDef());
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

		public void onSpecialVariable(final DeduceTypes2.OS_SpecialVariable aSpecialVariable) {
			final DeduceLocalVariable.MemberInvocation mi = aSpecialVariable.memberInvocation;

			switch (mi.role) {
			case INHERITED:
				final FunctionInvocation functionInvocation = deferredMemberFunction.functionInvocation();
				functionInvocation.generatePromise().
						then(new DoneCallback<BaseEvaFunction>() {
							@Override
							public void onDone(final BaseEvaFunction gf) {
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
								public void onDone(final EvaClass element_generated) {
									// once again we need EvaFunction, not FunctionDef
									// we seem to have it below, but there can be multiple
									// specializations of each function
									final EvaFunction gf = element_generated.functionMap.get((FunctionDef) deferredMemberFunction.getFunctionDef());
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

		@NotNull
		private <T extends EvaNode> Function<EvaNode, Map<FunctionDef, EvaFunction>> getFunctionMap(final T result) {
			final Function<EvaNode, Map<FunctionDef, EvaFunction>> x;
			if (result instanceof EvaNamespace)
				x = new GetFunctionMapNamespace();
			else if (result instanceof EvaClass)
				x = new GetFunctionMapClass();
			else
				throw new NotImplementedException();
			return x;
		}

		static class GetFunctionMapClass implements Function<EvaNode, Map<FunctionDef, EvaFunction>> {
			@Override
			public Map<FunctionDef, EvaFunction> apply(final EvaNode aClass) {
				return ((EvaClass) aClass).functionMap;
			}
		}

		static class GetFunctionMapNamespace implements Function<EvaNode, Map<FunctionDef, EvaFunction>> {
			@Override
			public Map<FunctionDef, EvaFunction> apply(final EvaNode aNamespace) {
				return ((EvaNamespace) aNamespace).functionMap;
			}
		}
	}

	public static class GeneratedClasses implements Iterable<EvaNode> {
		@NotNull List<EvaNode> generatedClasses = new ArrayList<EvaNode>();

		public void add(EvaNode aClass) {
			generatedClasses.add(aClass);
		}

		@Override
		public @NotNull Iterator<EvaNode> iterator() {
			return generatedClasses.iterator();
		}

		public int size() {
			return generatedClasses.size();
		}

		public List<EvaNode> copy() {
			return new ArrayList<EvaNode>(generatedClasses);
		}

		public void addAll(List<EvaNode> lgc) {
			// TODO is this method really needed
			generatedClasses.addAll(lgc);
		}
	}

	class RegisterClassInvocation {
		// TODO this class is a mess

		public @NotNull ClassInvocation registerClassInvocation(@NotNull ClassInvocation aClassInvocation) {
			// 1. select which to return
			ClassStatement              c   = aClassInvocation.getKlass();
			Collection<ClassInvocation> cis = classInvocationMultimap.get(c);
			for (@NotNull ClassInvocation ci : cis) {
				// don't lose information
				if (ci.getConstructorName() != null)
					if (!(ci.getConstructorName().equals(aClassInvocation.getConstructorName())))
						continue;

				boolean i = equivalentGenericPart(aClassInvocation, ci);
				if (i) {
					if (aClassInvocation instanceof DerivedClassInvocation) {
						if (ci instanceof DerivedClassInvocation)
							continue;

						/*if (ci.resolvePromise().isResolved())*/
						{
							ci.resolvePromise().then(new DoneCallback<EvaClass>() {
								@Override
								public void onDone(final EvaClass result) {
									aClassInvocation.resolveDeferred().resolve(result);
								}
							});
							return aClassInvocation;
						}
					} else
						return ci;
//						return part2(ci, false);
				}
			}

			return part2(aClassInvocation, true);
		}

		private ClassInvocation part2(final ClassInvocation aClassInvocation, boolean put) {
			// 2. Check and see if already done
			Collection<ClassInvocation> cls = classInvocationMultimap.get(aClassInvocation.getKlass());
			for (@NotNull ClassInvocation ci : cls) {
				if (equivalentGenericPart(ci, aClassInvocation)) {
					return ci;
				}
			}

			if (put) {
				classInvocationMultimap.put(aClassInvocation.getKlass(), aClassInvocation);
			}

			// 3. Generate new EvaClass
			final @NotNull WorkList  wl  = new WorkList();
			final @NotNull OS_Module mod = aClassInvocation.getKlass().getContext().module();
			wl.addJob(new WlGenerateClass(generatePhase.getGenerateFunctions(mod), aClassInvocation, generatedClasses)); // TODO why add now?
			generatePhase.wm.addJobs(wl);
			generatePhase.wm.drain(); // TODO find a better place to put this

			// 4. Return it
			return aClassInvocation;
		}
	}

}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
