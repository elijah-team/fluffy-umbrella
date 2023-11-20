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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.contexts.ClassContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.lang2.SpecialFunctions;
import tripleo.elijah.lang2.SpecialVariables;
import tripleo.elijah.stages.deduce.declarations.DeferredMember;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.*;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created 9/15/20 12:51 PM
 */
public class DeduceTypes2 {
	private static final String PHASE = "DeduceTypes2";
	private final @NotNull OS_Module module;
	final @NotNull DeducePhase phase;
	final ErrSink errSink;
	final @NotNull ElLog LOG;
	@NotNull WorkManager wm = new WorkManager();

	public DeduceTypes2(@NotNull final OS_Module module, @NotNull final DeducePhase phase) {
		this(module, phase, ElLog.Verbosity.VERBOSE);
	}

	public DeduceTypes2(@NotNull final OS_Module module, @NotNull final DeducePhase phase, final ElLog.Verbosity verbosity) {
		this.module = module;
		this.phase = phase;
		this.errSink = module.getCompilation().getErrSink();
		this.LOG = new ElLog(module.getFileName(), verbosity, PHASE);
		//
		phase.addLog(LOG);
	}

	public void deduceFunctions(final @NotNull Iterable<GeneratedNode> lgf) {
		for (final GeneratedNode generatedNode : lgf) {
			if (generatedNode instanceof GeneratedFunction) {
				@NotNull final GeneratedFunction generatedFunction = (GeneratedFunction) generatedNode;
				deduceOneFunction(generatedFunction, phase);
			}
		}

/*
		lgf.stream()
				.filter(generatedNode -> generatedNode instanceof GeneratedFunction)
				.map(x -> (GeneratedFunction) x)
				.forEach(generatedFunction -> deduceOneFunction(generatedFunction, phase));
*/

		@NotNull List<GeneratedNode> generatedClasses = (phase.generatedClasses.copy());
		// TODO consider using reactive here
		int size;
		do {
			size = df_helper(generatedClasses, new dfhi_functions());
			generatedClasses = phase.generatedClasses.copy();
		} while (size > 0);
		do {
			size = df_helper(generatedClasses, new dfhi_constructors());
			generatedClasses = phase.generatedClasses.copy();
		} while (size > 0);
	}

	/**
	 * Deduce functions or constructors contained in classes list
	 *
	 * @param aGeneratedClasses assumed to be a list of {@link GeneratedContainerNC}
	 * @param dfhi specifies what to select for:<br>
	 *             {@link dfhi_functions} will select all functions from {@code functionMap}, and <br>
	 *             {@link dfhi_constructors} will select all constructors from {@code constructors}.
	 * @param <T> generic parameter taken from {@code dfhi}
	 * @return the number of deduced functions or constructors, or 0
	 */
	<T> int df_helper(@NotNull final List<GeneratedNode> aGeneratedClasses, @NotNull final df_helper_i<T> dfhi) {
		int size = 0;
		for (final GeneratedNode generatedNode : aGeneratedClasses) {
			@NotNull final GeneratedContainerNC generatedContainerNC = (GeneratedContainerNC) generatedNode;
			final @Nullable df_helper<T> dfh = dfhi.get(generatedContainerNC);
			if (dfh == null) continue;
			@NotNull final Collection<T> lgf2 = dfh.collection();
			for (final T generatedConstructor : lgf2) {
				if (dfh.deduce(generatedConstructor))
					size++;
			}
		}
		return size;
	}

	/*public void deduceClasses(final @NotNull List<GeneratedNode> lgc) {
		for (GeneratedNode generatedNode : lgc) {
			if (!(generatedNode instanceof GeneratedClass)) continue;

			final GeneratedClass generatedClass = (GeneratedClass) generatedNode;
			for (GeneratedContainer.VarTableEntry entry : generatedClass.varTable) {
				final OS_Type vt = entry.varType;
				GenType genType = makeGenTypeFromOSType(vt, generatedClass.ci.genericPart);
				if (genType != null)
					entry.resolve(genType.node);
				int y=2;
			}
		}
	}*/

	public void deduceClasses(@NotNull final List<GeneratedClass> matching_class_list) {
		for (final GeneratedClass generatedClass : matching_class_list) {
			for (final GeneratedContainer.VarTableEntry entry : generatedClass.varTable) {
				final OS_Type vt = entry.varType;
				final GenType genType = makeGenTypeFromOSType(vt, generatedClass.ci.genericPart);
				if (genType != null)
					entry.resolve(genType.node);

				NotImplementedException.raise();
			}
		}
	}

	public DeducePhase _phase() {
		return phase;
	}

	public ErrSink _errSink() {
		return errSink;
	}

	interface IElementProcessor {
		void elementIsNull();

		void hasElement(OS_Element el);
	}

	static class ProcessElement {
		static void processElement(final OS_Element el, final IElementProcessor ep) {
			if (el == null)
				ep.elementIsNull();
			else
				ep.hasElement(el);
		}
	}

	private GenType makeGenTypeFromOSType(final OS_Type aType, final @Nullable Map<TypeName, OS_Type> aGenericPart) {
		final GenType gt = new GenType();
		gt.typeName = aType;
		if (aType.getType() == OS_Type.Type.USER) {
			final TypeName tn1 = aType.getTypeName();
			if (tn1.isNull()) return null; // TODO Unknown, needs to resolve somewhere

			assert tn1 instanceof NormalTypeName;
			final NormalTypeName tn = (NormalTypeName) tn1;
			final LookupResultList lrl = tn.getContext().lookup(tn.getName());
			final @Nullable OS_Element el = lrl.chooseBest(null);

			ProcessElement.processElement(el, new IElementProcessor() {
				@Override
				public void elementIsNull() {
					final int y=2;
				}

				@Override
				public void hasElement(final OS_Element el) {
					final @Nullable OS_Element best = preprocess(el);
					if (best == null) return;

					if (best instanceof ClassStatement) {
						final ClassStatement classStatement = (ClassStatement) best;
						gt.resolved = new OS_Type(classStatement);
					} else if (best instanceof ClassContext.OS_TypeNameElement) {
						final ClassContext.OS_TypeNameElement typeNameElement = (ClassContext.OS_TypeNameElement) best;
						assert aGenericPart != null;
						final OS_Type x = aGenericPart.get(typeNameElement.getTypeName());
						switch (x.getType()) {
						case USER_CLASS:
							final OS_Element best2 = x.getClassOf(); // always a ClassStatement

							// TODO test next 4 lines are copies of above
							if (best2 instanceof ClassStatement) {
								final ClassStatement classStatement = (ClassStatement) best2;
								gt.resolved = new OS_Type(classStatement);
							}
							break;
						case USER:
							final NormalTypeName tn2 = (NormalTypeName) x.getTypeName();
							final LookupResultList lrl2 = tn.getContext().lookup(tn2.getName());
							final @Nullable OS_Element el2 = lrl2.chooseBest(null);

							// TODO test next 4 lines are copies of above
							if (el2 instanceof ClassStatement) {
								final ClassStatement classStatement = (ClassStatement) el2;
								gt.resolved = new OS_Type(classStatement);
							} else
								throw new NotImplementedException();
							break;
						}
					} else {
						LOG.err("143 "+el);
						throw new NotImplementedException();
					}

					gotResolved(gt);
				}

				private void gotResolved(final GenType gt) {
					if (gt.resolved.getClassOf().getGenericPart().size() != 0) {
						//throw new AssertionError();
						LOG.info("149 non-generic type "+tn1);
					}
					genCI(gt, null); // TODO aGenericPart
					assert gt.ci != null;
					if (gt.ci instanceof NamespaceInvocation) {
						final NamespaceInvocation nsi = (NamespaceInvocation) gt.ci;
						nsi.resolveDeferred().then(new DoneCallback<GeneratedNamespace>() {
							@Override
							public void onDone(final GeneratedNamespace result) {
								gt.node = result;
							}
						});
					} else if (gt.ci instanceof ClassInvocation) {
						final ClassInvocation ci = (ClassInvocation) gt.ci;
						ci.resolvePromise().then(new DoneCallback<GeneratedClass>() {
							@Override
							public void onDone(final GeneratedClass result) {
								gt.node = result;
							}
						});
					} else
						throw new NotImplementedException();
				}

				private OS_Element preprocess(final OS_Element el) {
					@Nullable OS_Element best = el;
					try {
						while (best instanceof AliasStatement) {
							best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, DeduceTypes2.this);
						}
						assert best != null;
						return best;
					} catch (final ResolveError aResolveError) {
						LOG.err("152 Can't resolve Alias statement "+best);
						errSink.reportDiagnostic(aResolveError);
						return null;
					}
				}
			});
		} else
			throw new AssertionError("Not a USER Type");
		return gt;
	}

	interface df_helper_i<T> {
		@Nullable df_helper<T> get(GeneratedContainerNC generatedClass);
	}

	class dfhi_constructors implements df_helper_i<GeneratedConstructor> {
		@Override
		public @Nullable df_helper_Constructors get(final GeneratedContainerNC aGeneratedContainerNC) {
			if (aGeneratedContainerNC instanceof GeneratedClass) // TODO namespace constructors
				return new df_helper_Constructors((GeneratedClass) aGeneratedContainerNC);
			else
				return null;
		}
	}

	class dfhi_functions implements df_helper_i<GeneratedFunction> {
		@Override
		public @NotNull df_helper_Functions get(final GeneratedContainerNC aGeneratedContainerNC) {
			return new df_helper_Functions(aGeneratedContainerNC);
		}
	}

	interface df_helper<T> {
		@NotNull Collection<T> collection();

		boolean deduce(T generatedConstructor);
	}

	class df_helper_Constructors implements df_helper<GeneratedConstructor> {
		private final GeneratedClass generatedClass;

		public df_helper_Constructors(final GeneratedClass aGeneratedClass) {
			generatedClass = aGeneratedClass;
		}

		@Override
		public @NotNull Collection<GeneratedConstructor> collection() {
			return generatedClass.constructors.values();
		}

		@Override
		public boolean deduce(@NotNull final GeneratedConstructor generatedConstructor) {
			return deduceOneConstructor(generatedConstructor, phase);
		}
	}

	class df_helper_Functions implements df_helper<GeneratedFunction> {
		private final GeneratedContainerNC generatedContainerNC;

		public df_helper_Functions(final GeneratedContainerNC aGeneratedContainerNC) {
			generatedContainerNC = aGeneratedContainerNC;
		}

		@Override
		public @NotNull Collection<GeneratedFunction> collection() {
			return generatedContainerNC.functionMap.values();
		}

		@Override
		public boolean deduce(@NotNull final GeneratedFunction aGeneratedFunction) {
			return deduceOneFunction(aGeneratedFunction, phase);
		}
	}
	
	public boolean deduceOneFunction(@NotNull final GeneratedFunction aGeneratedFunction, @NotNull final DeducePhase aDeducePhase) {
		if (aGeneratedFunction.deducedAlready) return false;
		deduce_generated_function(aGeneratedFunction);
		aGeneratedFunction.deducedAlready = true;
		for (@NotNull final IdentTableEntry identTableEntry : aGeneratedFunction.idte_list) {
			if (identTableEntry.getResolvedElement() instanceof VariableStatement) {
				final @NotNull VariableStatement vs = (VariableStatement) identTableEntry.getResolvedElement();
				final OS_Element el = vs.getParent().getParent();
				final OS_Element el2 = aGeneratedFunction.getFD().getParent();
				if (el != el2) {
					if (el instanceof ClassStatement || el instanceof NamespaceStatement)
						// NOTE there is no concept of gf here
						aDeducePhase.registerResolvedVariable(identTableEntry, el, vs.getName());
				}
			}
		}
		{
			final @NotNull GeneratedFunction gf = aGeneratedFunction;

			@Nullable InstructionArgument result_index = gf.vte_lookup("Result");
			if (result_index == null) {
				// if there is no Result, there should be Value
				result_index = gf.vte_lookup("Value");
				// but Value might be passed in. If it is, discard value
				if (result_index != null) {
					@NotNull final VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
					if (vte.vtt != VariableTableType.RESULT) {
						result_index = null;
					}
				}
			}
			if (result_index != null) {
				@NotNull final VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
				if (vte.resolvedType() == null) {
					final GenType b = vte.genType;
					final OS_Type a = vte.type.getAttached();
					if (a != null) {
						// see resolve_function_return_type
						switch (a.getType()) {
							case USER_CLASS:
								dof_uc(vte, a);
								break;
							case USER:
								vte.genType.typeName = a;
								try {
									@NotNull final GenType rt = resolve_type(a, a.getTypeName().getContext());
									if (rt.resolved != null && rt.resolved.getType() == OS_Type.Type.USER_CLASS) {
										if (rt.resolved.getClassOf().getGenericPart().size() > 0)
											vte.genType.nonGenericTypeName = a.getTypeName(); // TODO might be wrong
										dof_uc(vte, rt.resolved);
									}
								} catch (final ResolveError aResolveError) {
									errSink.reportDiagnostic(aResolveError);
								}
								break;
							default:
								// TODO do nothing for now
								final int y3 = 2;
								break;
						}
					} /*else
							throw new NotImplementedException();*/
				}
			}
		}
		aDeducePhase.addFunction(aGeneratedFunction, (FunctionDef) aGeneratedFunction.getFD());
		return true;
	}

	public boolean deduceOneConstructor(@NotNull final GeneratedConstructor aGeneratedConstructor, @NotNull final DeducePhase aDeducePhase) {
		if (aGeneratedConstructor.deducedAlready) return false;
		deduce_generated_function_base(aGeneratedConstructor, aGeneratedConstructor.getFD());
		aGeneratedConstructor.deducedAlready = true;
		for (@NotNull final IdentTableEntry identTableEntry : aGeneratedConstructor.idte_list) {
			if (identTableEntry.getResolvedElement() instanceof VariableStatement) {
				final @NotNull VariableStatement vs = (VariableStatement) identTableEntry.getResolvedElement();
				final OS_Element el = vs.getParent().getParent();
				final OS_Element el2 = aGeneratedConstructor.getFD().getParent();
				if (el != el2) {
					if (el instanceof ClassStatement || el instanceof NamespaceStatement)
						// NOTE there is no concept of gf here
						aDeducePhase.registerResolvedVariable(identTableEntry, el, vs.getName());
				}
			}
		}
		{
			final @NotNull GeneratedConstructor gf = aGeneratedConstructor;

			@Nullable InstructionArgument result_index = gf.vte_lookup("Result");
			if (result_index == null) {
				// if there is no Result, there should be Value
				result_index = gf.vte_lookup("Value");
				// but Value might be passed in. If it is, discard value
				if (result_index != null) {
					@NotNull final VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
					if (vte.vtt != VariableTableType.RESULT) {
						result_index = null;
					}
				}
			}
			if (result_index != null) {
				@NotNull final VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
				if (vte.resolvedType() == null) {
					final GenType b = vte.genType;
					final OS_Type a = vte.type.getAttached();
					if (a != null) {
						// see resolve_function_return_type
						switch (a.getType()) {
							case USER_CLASS:
								dof_uc(vte, a);
								break;
							case USER:
								b.typeName = a;
								try {
									@NotNull final GenType rt = resolve_type(a, a.getTypeName().getContext());
									if (rt.resolved != null && rt.resolved.getType() == OS_Type.Type.USER_CLASS) {
										if (rt.resolved.getClassOf().getGenericPart().size() > 0)
											b.nonGenericTypeName = a.getTypeName(); // TODO might be wrong
										dof_uc(vte, rt.resolved);
									}
								} catch (final ResolveError aResolveError) {
									errSink.reportDiagnostic(aResolveError);
								}
								break;
							default:
								// TODO do nothing for now
								final int y3 = 2;
								break;
						}
					} /*else
							throw new NotImplementedException();*/
				}
			}
		}
//		aDeducePhase.addFunction(aGeneratedConstructor, (FunctionDef) aGeneratedConstructor.getFD()); // TODO do we need this?
		return true;
	}

	private void dof_uc(@NotNull final VariableTableEntry aVte, @NotNull final OS_Type aA) {
		// we really want a ci from somewhere
		assert aA.getClassOf().getGenericPart().size() == 0;
		@Nullable ClassInvocation ci = new ClassInvocation(aA.getClassOf(), null);
		ci = phase.registerClassInvocation(ci);

		aVte.genType.resolved = aA; // README assuming OS_Type cannot represent namespaces
		aVte.genType.ci = ci;

		ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
			@Override
			public void onDone(final GeneratedClass result) {
				aVte.resolveTypeToClass(result);
			}
		});
	}

	@NotNull List<Runnable> onRunnables = new ArrayList<Runnable>();

	void onFinish(final Runnable r) {
		onRunnables.add(r);
	}

	public void deduce_generated_constructor(final @NotNull GeneratedFunction generatedFunction) {
		final @NotNull ConstructorDef fd = (ConstructorDef) generatedFunction.getFD();
		deduce_generated_function_base(generatedFunction, fd);
	}

	public void deduce_generated_function(final @NotNull GeneratedFunction generatedFunction) {
		final @NotNull FunctionDef fd = (FunctionDef) generatedFunction.getFD();
		deduce_generated_function_base(generatedFunction, fd);
	}

	public void deduce_generated_function_base(final @NotNull BaseGeneratedFunction generatedFunction, @NotNull final BaseFunctionDef fd) {
		final Context fd_ctx = fd.getContext();
		//
		{
			final ProcTableEntry pte = generatedFunction.fi.pte;
			final @NotNull String pte_string = getPTEString(pte);
			LOG.err("** deduce_generated_function "+ fd.name()+" "+pte_string);//+" "+((OS_Container)((FunctionDef)fd).getParent()).name());
		}
		//
		//
		for (final @NotNull Instruction instruction : generatedFunction.instructions()) {
			final Context context = generatedFunction.getContextFromPC(instruction.getIndex());
//			LOG.info("8006 " + instruction);
			switch (instruction.getName()) {
			case E:
				{
					//
					// resolve all cte expressions
					//
					for (final @NotNull ConstantTableEntry cte : generatedFunction.cte_list) {
						resolve_cte_expression(cte, context);
					}
					//
					// add proc table listeners
					//
					add_proc_table_listeners(generatedFunction);
					//
					// resolve ident table
					//
					for (@NotNull final IdentTableEntry ite : generatedFunction.idte_list) {
						ite.resolveExpectation = promiseExpectation(ite, "Element Resolved");
						resolve_ident_table_entry(ite, generatedFunction, context);
					}
					//
					// resolve arguments table
					//
					@NotNull final Resolve_Variable_Table_Entry rvte = new Resolve_Variable_Table_Entry(generatedFunction, context, this);
					@NotNull final IVariableConnector connector;
					if (generatedFunction instanceof GeneratedConstructor) {
						connector = new CtorConnector((GeneratedConstructor) generatedFunction);
					} else {
						connector = new NullConnector();
					}
					for (@NotNull final VariableTableEntry vte : generatedFunction.vte_list) {
						rvte.action(vte, connector);
					}
				}
				break;
			case X:
				{
					//
					// resolve var table. moved from `E'
					//
					for (@NotNull final VariableTableEntry vte : generatedFunction.vte_list) {
						resolve_var_table_entry(vte, generatedFunction, context);
					}
					for (@NotNull final Runnable runnable : onRunnables) {
						runnable.run();
					}
//					LOG.info("167 "+generatedFunction);
					//
					// ATTACH A TYPE TO VTE'S
					// CONVERT USER TYPES TO USER_CLASS TYPES
					//
					for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
//						LOG.info("704 "+vte.type.attached+" "+vte.potentialTypes());
						final OS_Type attached = vte.type.getAttached();
						if (attached != null && attached.getType() == OS_Type.Type.USER) {
							final TypeName x = attached.getTypeName();
							if (x instanceof NormalTypeName) {
								final String tn = ((NormalTypeName) x).getName();
								final LookupResultList lrl = x.getContext().lookup(tn);
								@Nullable OS_Element best = lrl.chooseBest(null);
								if (best != null) {
									while (best instanceof AliasStatement) {
										best = DeduceLookupUtils._resolveAlias((AliasStatement) best, this);
									}
									if (!(OS_Type.isConcreteType(best))) {
										errSink.reportError(String.format("Not a concrete type %s for (%s)", best, tn));
									} else {
	//									LOG.info("705 " + best);
										// NOTE that when we set USER_CLASS from USER generic information is
										// still contained in constructable_pte
										@NotNull final GenType genType = new GenType();
										genType.typeName = attached;
										genType.resolved = new OS_Type((ClassStatement) best);
//										genType.copy(vte.type.genType);
										genType.ci = genCI(genType, x);
										vte.type.genType.copy(genType);
										// set node when available
										((ClassInvocation) vte.type.genType.ci).resolvePromise().done(new DoneCallback<GeneratedClass>() {
											@Override
											public void onDone(final GeneratedClass result) {
												vte.type.genType.node = result;
												vte.resolveTypeToClass(result);
												vte.genType = vte.type.genType; // TODO who knows if this is necessary?
											}
										});
									}
									//vte.el = best;
									// NOTE we called resolve_var_table_entry above
									LOG.err("200 "+best);
									if (vte.getResolvedElement() != null)
										assert vte.getStatus() == BaseTableEntry.Status.KNOWN;
//									vte.setStatus(BaseTableEntry.Status.KNOWN, best/*vte.el*/);
								} else {
									errSink.reportDiagnostic(new ResolveError(x, lrl));
								}
							}
						}
					}
					for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
						if (vte.vtt == VariableTableType.ARG) {
							final OS_Type attached = vte.type.getAttached();
							if (attached != null) {
								if (attached.getType() == OS_Type.Type.USER)
									//throw new AssertionError();
									errSink.reportError("369 ARG USER type (not deduced) "+vte);
							} else {
								errSink.reportError("457 ARG type not deduced/attached "+vte);
							}
						}
					}
					//
					// ATTACH A TYPE TO IDTE'S
					//
					for (@NotNull final IdentTableEntry ite : generatedFunction.idte_list) {
						final int y = 2;

						Assign_type_to_idte atti = new Assign_type_to_idte(this, ite, generatedFunction, fd_ctx, context);
						atti.run();
					}
					{
						// TODO why are we doing this?
//						Resolve_each_typename ret = new Resolve_each_typename(phase, this, errSink);
//						for (TypeTableEntry typeTableEntry : generatedFunction.tte_list) {
//							ret.action(typeTableEntry);
//						}
					}
					{
						final @NotNull WorkManager workManager = wm;//new WorkManager();
						@NotNull final Dependencies deps = new Dependencies(/*phase, this, errSink*/);
						for (@NotNull final GenType genType : generatedFunction.dependentTypes()) {
							deps.action_type(genType, workManager);
						}
						for (@NotNull final FunctionInvocation dependentFunction : generatedFunction.dependentFunctions()) {
							deps.action_function(dependentFunction, workManager);
						}
						workManager.drain();
					}
					//
					// RESOLVE FUNCTION RETURN TYPES
					//
					resolve_function_return_type(generatedFunction);
					//
					// LOOKUP FUNCTIONS
					//
					{
						@NotNull final Lookup_function_on_exit lfoe = new Lookup_function_on_exit();
						for (@NotNull final ProcTableEntry pte : generatedFunction.prte_list) {
							lfoe.action(pte);
						}
						wm.drain();
					}

					expectations.check();
				}
				break;
			case ES:
				break;
			case XS:
				break;
			case AGN:
				{ // TODO doesn't account for __assign__
					final InstructionArgument agn_lhs = instruction.getArg(0);
					if (agn_lhs instanceof IntegerIA) {
						final @NotNull IntegerIA arg = (IntegerIA) agn_lhs;
						final @NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(arg.getIndex());
						final InstructionArgument i2 = instruction.getArg(1);
						if (i2 instanceof IntegerIA) {
							final @NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(i2));
							vte.addPotentialType(instruction.getIndex(), vte2.type);
						} else if (i2 instanceof FnCallArgs) {
							final @NotNull FnCallArgs fca = (FnCallArgs) i2;
							do_assign_call(generatedFunction, context, vte, fca, instruction);
						} else if (i2 instanceof ConstTableIA) {
							do_assign_constant(generatedFunction, instruction, vte, (ConstTableIA) i2);
						} else if (i2 instanceof IdentIA) {
							@NotNull final IdentTableEntry idte = generatedFunction.getIdentTableEntry(to_int(i2));
							assert idte.type != null;
							assert idte.getResolvedElement() != null;
							vte.addPotentialType(instruction.getIndex(), idte.type);
						} else if (i2 instanceof ProcIA) {
							throw new NotImplementedException();
						} else
							throw new NotImplementedException();
					} else if (agn_lhs instanceof IdentIA) {
						final @NotNull IdentIA arg = (IdentIA) agn_lhs;
						final @NotNull IdentTableEntry idte = arg.getEntry();
						final InstructionArgument i2 = instruction.getArg(1);
						if (i2 instanceof IntegerIA) {
							final @NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(i2));
							idte.addPotentialType(instruction.getIndex(), vte2.type);
						} else if (i2 instanceof FnCallArgs) {
							final @NotNull FnCallArgs fca = (FnCallArgs) i2;
							do_assign_call(generatedFunction, fd_ctx, idte, fca, instruction.getIndex());
						} else if (i2 instanceof IdentIA) {
							@NotNull final IdentTableEntry idte2 = generatedFunction.getIdentTableEntry(to_int(i2));
							if (idte2.type == null) {
								idte2.type = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, idte2.getIdent(), idte2);
							}
							final LookupResultList lrl1 = fd_ctx.lookup(idte2.getIdent().getText());
							@Nullable final OS_Element best1 = lrl1.chooseBest(null);
							if (best1 != null) {
								idte2.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best1));
								// TODO check for elements which may contain type information
								if (best1 instanceof VariableStatement) {
									final @NotNull VariableStatement vs = (VariableStatement) best1;
									@NotNull final DeferredMember dm = deferred_member(vs.getParent().getParent(), null, vs, idte2);
									dm.typePromise().done(new DoneCallback<GenType>() {
											@Override
											public void onDone(@NotNull final GenType result) {
												assert result.resolved != null;
												idte2.type.setAttached(result.resolved);
											}
										});
								}
							} else {
								idte2.setStatus(BaseTableEntry.Status.UNKNOWN, null);
								LOG.err("242 Bad lookup" + idte2.getIdent().getText());
							}
							idte.addPotentialType(instruction.getIndex(), idte2.type);
						} else if (i2 instanceof ConstTableIA) {
							do_assign_constant(generatedFunction, instruction, idte, (ConstTableIA) i2);
						} else if (i2 instanceof ProcIA) {
							throw new NotImplementedException();
						} else
							throw new NotImplementedException();
					}
				}
				break;
			case AGNK:
				{
					final @NotNull IntegerIA arg = (IntegerIA)instruction.getArg(0);
					final @NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(arg.getIndex());
					final InstructionArgument i2 = instruction.getArg(1);
					final @NotNull ConstTableIA ctia = (ConstTableIA) i2;
					do_assign_constant(generatedFunction, instruction, vte, ctia);
				}
				break;
			case AGNT:
				break;
			case AGNF:
				LOG.info("292 Encountered AGNF");
				break;
			case JE:
				LOG.info("296 Encountered JE");
				break;
			case JNE:
				break;
			case JL:
				break;
			case JMP:
				break;
			case CALL: {
				final int pte_num = ((ProcIA)instruction.getArg(0)).getIndex();
				final @NotNull ProcTableEntry pte = generatedFunction.getProcTableEntry(pte_num);
//				final InstructionArgument i2 = (instruction.getArg(1));
				{
					final @NotNull IdentIA identIA = (IdentIA) pte.expression_num;
					final String x = generatedFunction.getIdentIAPathNormal(identIA);
					LOG.info("298 Calling "+x);
					resolveIdentIA_(context, identIA, generatedFunction, new FoundElement(phase) {

						@SuppressWarnings("unused") final String xx = x;

						@Override
						public void foundElement(final OS_Element e) {
							pte.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(e, identIA));
							if (fd instanceof DefFunctionDef) {
								final IInvocation invocation = getInvocation((GeneratedFunction) generatedFunction);
								forFunction(newFunctionInvocation((FunctionDef) e, pte, invocation, phase), new ForFunction() {
									@Override
									public void typeDecided(@NotNull final GenType aType) {
										@Nullable final InstructionArgument x = generatedFunction.vte_lookup("Result");
										assert x != null;
										((IntegerIA) x).getEntry().type.setAttached(gt(aType));
									}
								});
							}
						}

						@Override
						public void noFoundElement() {
							errSink.reportError("370 Can't find callsite "+x);
							// TODO don't know if this is right
							@NotNull final IdentTableEntry entry = identIA.getEntry();
							if (entry.getStatus() != BaseTableEntry.Status.UNKNOWN)
								entry.setStatus(BaseTableEntry.Status.UNKNOWN, null);
						}
					});
				}
			}
			break;
			case CALLS: {
				final int i1 = to_int(instruction.getArg(0));
				final InstructionArgument i2 = (instruction.getArg(1));
				final @NotNull ProcTableEntry fn1 = generatedFunction.getProcTableEntry(i1);
				{
					implement_calls(generatedFunction, fd_ctx, i2, fn1, instruction.getIndex());
				}
/*
				if (i2 instanceof IntegerIA) {
					int i2i = to_int(i2);
					VariableTableEntry vte = generatedFunction.getVarTableEntry(i2i);
					int y =2;
				} else
					throw new NotImplementedException();
*/
			}
			break;
			case RET:
				break;
			case YIELD:
				break;
			case TRY:
				break;
			case PC:
				break;
			case CAST_TO:
				// README potentialType info is already added by MatchConditional
				break;
			case DECL:
				// README for GenerateC, etc: marks the spot where a declaration should go. Wouldn't be necessary if we had proper Range's
				break;
			case IS_A:
				break;
			case NOP:
				break;
			case CONSTRUCT:
				implement_construct(generatedFunction, instruction);
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + instruction.getName());
			}
		}
		for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
			if (vte.type.getAttached() == null) {
				final int potential_size = vte.potentialTypes().size();
				if (potential_size == 1)
					vte.type.setAttached(getPotentialTypesVte(vte).get(0).getAttached());
				else if (potential_size > 1) {
					// TODO Check type compatibility
					LOG.err("703 "+vte.getName()+" "+vte.potentialTypes());
					errSink.reportDiagnostic(new CantDecideType(vte, vte.potentialTypes()));
				} else {
					// potential_size == 0
					// Result is handled by phase.typeDecideds, self is always valid
					if (/*vte.getName() != null &&*/ !(vte.vtt == VariableTableType.RESULT || vte.vtt == VariableTableType.SELF))
						errSink.reportDiagnostic(new CantDecideType(vte, vte.potentialTypes()));
				}
			}
		}
		{
			//
			// NOW CALCULATE DEFERRED CALLS
			//
			for (final Integer deferred_call : generatedFunction.deferred_calls) {
				final Instruction instruction = generatedFunction.getInstruction(deferred_call);

				final int i1 = to_int(instruction.getArg(0));
				final InstructionArgument i2 = (instruction.getArg(1));
				final @NotNull ProcTableEntry fn1 = generatedFunction.getProcTableEntry(i1);
				{
//					generatedFunction.deferred_calls.remove(deferred_call);
					implement_calls_(generatedFunction, fd_ctx, i2, fn1, instruction.getIndex());
				}
			}
		}
	}

	interface IVariableConnector {
		void connect(VariableTableEntry aVte, String aName);
	}

	class CtorConnector implements IVariableConnector {
		private final GeneratedConstructor generatedConstructor;

		public CtorConnector(final GeneratedConstructor aGeneratedConstructor) {
			generatedConstructor = aGeneratedConstructor;
		}

		@Override
		public void connect(final VariableTableEntry aVte, final String aName) {
			final List<GeneratedContainer.VarTableEntry> vt = ((GeneratedClass) generatedConstructor.getGenClass()).varTable;
			for (final GeneratedContainer.VarTableEntry gc_vte : vt) {
				if (gc_vte.nameToken.getText().equals(aName)) {
					gc_vte.connect(aVte, generatedConstructor);
				}
			}
		}
	}

	class NullConnector implements IVariableConnector {
		@Override
		public void connect(final VariableTableEntry aVte, final String aName) {
		}
	}

	private void add_proc_table_listeners(@NotNull final BaseGeneratedFunction generatedFunction) {
		for (final @NotNull ProcTableEntry pte : generatedFunction.prte_list) {
			pte.addStatusListener(new ProcTableListener(pte, generatedFunction, new DeduceClient2(this)));

			final InstructionArgument en = pte.expression_num;
			if (en != null) {
				if (en instanceof IdentIA) {
					final @NotNull IdentIA identIA = (IdentIA) en;
					@NotNull final IdentTableEntry idte = identIA.getEntry();
					idte.addStatusListener(new BaseTableEntry.StatusListener() {
						@Override
						public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
							if (newStatus != BaseTableEntry.Status.KNOWN)
								return;

							final OS_Element el = eh.getElement();

							@NotNull final ElObjectType type = DecideElObjectType.getElObjectType(el);

							switch (type) {
							case NAMESPACE:
								@NotNull final GenType genType = new GenType((NamespaceStatement) el);
								generatedFunction.addDependentType(genType);
								break;
							case CLASS:
								@NotNull final GenType genType2 = new GenType((ClassStatement) el);
								generatedFunction.addDependentType(genType2);
								break;
							case FUNCTION:
								@Nullable IdentIA identIA2 = null;
								if (pte.expression_num instanceof IdentIA)
									identIA2 = (IdentIA) pte.expression_num;
								if (identIA2 != null) {
									@NotNull final IdentTableEntry idte2 = identIA.getEntry();
									@Nullable final ProcTableEntry procTableEntry = idte2.getCallablePTE();
									if (procTableEntry == pte) System.err.println("940 procTableEntry == pte");
									if (procTableEntry != null) {
										// TODO doesn't seem like we need this
										procTableEntry.onFunctionInvocation(new DoneCallback<FunctionInvocation>() {
											@Override
											public void onDone(@NotNull final FunctionInvocation functionInvocation) {
												final ClassInvocation ci = functionInvocation.getClassInvocation();
												final NamespaceInvocation nsi = functionInvocation.getNamespaceInvocation();
												// do we register?? probably not
												if (ci == null && nsi == null)
													assert false;
												@NotNull final FunctionInvocation fi = newFunctionInvocation((FunctionDef) el, pte, ci != null ? ci : nsi, phase);

												{
													if (functionInvocation.getClassInvocation() == fi.getClassInvocation() &&
														functionInvocation.getFunction() == fi.getFunction() &&
														functionInvocation.pte == fi.pte)
														System.err.println("955 It seems like we are generating the same thing...");
													else {
														final int ok=2;
													}

												}
												generatedFunction.addDependentFunction(fi);
											}
										});
										// END
									}
								}
								break;
							default:
								LOG.err(String.format("228 Don't know what to do %s %s", type, el));
								break;
							}
						}
					});
				} else if (en instanceof IntegerIA) {
					final @NotNull IntegerIA integerIA = (IntegerIA) en;
					@NotNull final VariableTableEntry vte = integerIA.getEntry();
					vte.addStatusListener(new BaseTableEntry.StatusListener() {
						@Override
						public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
							if (newStatus != BaseTableEntry.Status.KNOWN)
								return;

							@NotNull final VariableTableEntry vte2 = vte;

							final OS_Element el = eh.getElement();

							@NotNull final ElObjectType type = DecideElObjectType.getElObjectType(el);

							switch (type) {
							case VAR:
								break;
							default:
								throw new NotImplementedException();
							}
						}
					});
				} else
					throw new NotImplementedException();
			}
		}
	}

	private @NotNull String getPTEString(@Nullable final ProcTableEntry pte) {
		final String pte_string;
		if (pte == null)
			pte_string = "[]";
		else {
			@NotNull final List<String> l = new ArrayList<String>();

			for (@NotNull final TypeTableEntry typeTableEntry : pte.getArgs()) {
				final OS_Type attached = typeTableEntry.getAttached();

				if (attached != null)
					l.add(attached.toString());
				else {
					LOG.err("267 attached == null for "+typeTableEntry);

					if (typeTableEntry.expression != null)
						l.add(String.format("<Unknown expression: %s>", typeTableEntry.expression));
					else
						l.add("<Unknkown>");
				}
			}

			@NotNull final StringBuilder sb2 = new StringBuilder();
			sb2.append("[");
			sb2.append(Helpers.String_join(", ", l));
			sb2.append("]");
			pte_string = sb2.toString();
		}
		return pte_string;
	}

	/**
	 * See {@link Implement_construct#_implement_construct_type}
	 */
	private @Nullable ClassInvocation genCI(@NotNull final TypeTableEntry aType) {
		final GenType genType = aType.genType;
		if (genType.nonGenericTypeName != null) {
			@NotNull final NormalTypeName aTyn1 = (NormalTypeName) genType.nonGenericTypeName;
			@Nullable final String constructorName = null; // TODO this comes from nowhere
			final ClassStatement best = genType.resolved.getClassOf();
			//
			@NotNull final List<TypeName> gp = best.getGenericPart();
			@Nullable ClassInvocation clsinv = new ClassInvocation(best, constructorName);
			if (gp.size() > 0) {
				final TypeNameList gp2 = aTyn1.getGenericPart();
				for (int i = 0; i < gp.size(); i++) {
					final TypeName typeName = gp2.get(i);
					@NotNull final GenType genType1;
					try {
						genType1 = resolve_type(new OS_Type(typeName), typeName.getContext());
						clsinv.set(i, gp.get(i), genType1.resolved);
					} catch (final ResolveError aResolveError) {
						aResolveError.printStackTrace();
						return null;
					}
				}
			}
			clsinv = phase.registerClassInvocation(clsinv);
			genType.ci = clsinv;
			return clsinv;
		}
		if (genType.resolved != null) {
			final ClassStatement best = genType.resolved.getClassOf();
			@Nullable final String constructorName = null; // TODO what to do about this, nothing I guess

			@NotNull final List<TypeName> gp = best.getGenericPart();
			@Nullable ClassInvocation clsinv = new ClassInvocation(best, constructorName);
			assert best.getGenericPart().size() == 0;
/*
			if (gp.size() > 0) {
				TypeNameList gp2 = aTyn1.getGenericPart();
				for (int i = 0; i < gp.size(); i++) {
					final TypeName typeName = gp2.get(i);
					@NotNull OS_Type typeName2;
					try {
						typeName2 = resolve_type(new OS_Type(typeName), typeName.getContext());
						clsinv.set(i, gp.get(i), typeName2);
					} catch (ResolveError aResolveError) {
						aResolveError.printStackTrace();
						return null;
					}
				}
			}
*/
			clsinv = phase.registerClassInvocation(clsinv);
			genType.ci = clsinv;
			return clsinv;
		}
		return null;
	}

	@Nullable
	public ClassInvocation genCI(@NotNull final GenType genType, final TypeName aGenericTypeName) {
		if (genType.nonGenericTypeName != null) {
			@NotNull final NormalTypeName aTyn1 = (NormalTypeName) genType.nonGenericTypeName;
			@Nullable final String constructorName = null; // TODO this comes from nowhere
			final ClassStatement best = genType.resolved.getClassOf();
			//
			@NotNull final List<TypeName> gp = best.getGenericPart();
			@Nullable ClassInvocation clsinv = new ClassInvocation(best, constructorName);
			if (gp.size() > 0) {
				final TypeNameList gp2 = aTyn1.getGenericPart();
				for (int i = 0; i < gp.size(); i++) {
					final TypeName typeName = gp2.get(i);
					@NotNull final GenType typeName2;
					try {
						typeName2 = resolve_type(new OS_Type(typeName), typeName.getContext());
						clsinv.set(i, gp.get(i), typeName2.resolved);
					} catch (final ResolveError aResolveError) {
						aResolveError.printStackTrace();
						return null;
					}
				}
			}
			clsinv = phase.registerClassInvocation(clsinv);
			genType.ci = clsinv;
			return clsinv;
		}
		if (genType.resolved != null) {
			final ClassStatement best = genType.resolved.getClassOf();
			@Nullable final String constructorName = null; // TODO what to do about this, nothing I guess

			@NotNull final List<TypeName> gp = best.getGenericPart();
			@Nullable ClassInvocation clsinv;
			if (genType.ci == null) {
				clsinv = new ClassInvocation(best, constructorName);
				if (gp.size() > 0) {
					if (aGenericTypeName instanceof NormalTypeName) {
						final @NotNull NormalTypeName tn = (NormalTypeName) aGenericTypeName;
						final TypeNameList tngp = tn.getGenericPart();
						for (int i = 0; i < gp.size(); i++) {
							final TypeName typeName = tngp.get(i);
							@NotNull final GenType typeName2;
							try {
								typeName2 = resolve_type(new OS_Type(typeName), typeName.getContext());
								clsinv.set(i, gp.get(i), typeName2.resolved);
							} catch (final ResolveError aResolveError) {
//								aResolveError.printStackTrace();
								errSink.reportDiagnostic(aResolveError);
								return null;
							}
						}
					}
				}
				clsinv = phase.registerClassInvocation(clsinv);
				genType.ci = clsinv;
			} else
				clsinv = (ClassInvocation) genType.ci;
			return clsinv;
		}
		return null;
	}

	final List<FunctionInvocation> functionInvocations = new ArrayList<>();

	@NotNull FunctionInvocation newFunctionInvocation(final BaseFunctionDef aFunctionDef, final ProcTableEntry aPte, @NotNull final IInvocation aInvocation, @NotNull final DeducePhase aDeducePhase) {
		@NotNull final FunctionInvocation fi = new FunctionInvocation(aFunctionDef, aPte, aInvocation, aDeducePhase.generatePhase);
		// TODO register here
		return fi;
	}

	public String getFileName() {
		return module.getFileName();
	}

	public @NotNull GenerateFunctions getGenerateFunctions(@NotNull final OS_Module aModule) {
		return phase.generatePhase.getGenerateFunctions(aModule);
	}

	class Resolve_each_typename {

		private final DeducePhase phase;
		private final DeduceTypes2 dt2;
		private final ErrSink errSink;

		public Resolve_each_typename(final DeducePhase aPhase, final DeduceTypes2 aDeduceTypes2, final ErrSink aErrSink) {
			phase = aPhase;
			dt2 = aDeduceTypes2;
			errSink = aErrSink;
		}

		public void action(@NotNull final TypeTableEntry typeTableEntry) {
			@Nullable final OS_Type attached = typeTableEntry.getAttached();
			if (attached == null) return;
			if (attached.getType() == OS_Type.Type.USER) {
				action_USER(typeTableEntry, attached);
			} else if (attached.getType() == OS_Type.Type.USER_CLASS) {
				action_USER_CLASS(typeTableEntry, attached);
			}
		}

		public void action_USER_CLASS(@NotNull final TypeTableEntry typeTableEntry, @NotNull final OS_Type aAttached) {
			final ClassStatement c = aAttached.getClassOf();
			assert c != null;
			phase.onClass(c, new OnClass() {
				// TODO what about ClassInvocation's?
				@Override
				public void classFound(final GeneratedClass cc) {
					typeTableEntry.resolve(cc); // set genType.node
				}
			});
		}

		public void action_USER(@NotNull final TypeTableEntry typeTableEntry, @Nullable final OS_Type aAttached) {
			final TypeName tn = aAttached.getTypeName();
			if (tn == null) return; // hack specifically for Result
			switch (tn.kindOfType()) {
				case FUNCTION:
				case GENERIC:
				case TYPE_OF:
					return;
			}
			try {
				typeTableEntry.setAttached(dt2.resolve_type(aAttached, aAttached.getTypeName().getContext()));
				switch (typeTableEntry.getAttached().getType()) {
				case USER_CLASS:
					action_USER_CLASS(typeTableEntry, typeTableEntry.getAttached());
					break;
				case GENERIC_TYPENAME:
					LOG.err(String.format("801 Generic Typearg %s for %s", tn, "genericFunction.getFD().getParent()"));
					break;
				default:
					LOG.err("245 typeTableEntry attached wrong type " + typeTableEntry);
					break;
				}
			} catch (final ResolveError aResolveError) {
				LOG.err("288 Failed to resolve type "+ aAttached);
				errSink.reportDiagnostic(aResolveError);
			}
		}
	}

	class Dependencies {
		final WorkList wl = new WorkList();

		public void action_type(@NotNull final GenType genType, @NotNull final WorkManager aWorkManager) {
			// TODO work this out further
			if (genType.resolvedn != null) {
				@NotNull final OS_Module mod = genType.resolvedn.getContext().module();
				final @NotNull GenerateFunctions gf = phase.generatePhase.getGenerateFunctions(mod);
				final NamespaceInvocation ni = phase.registerNamespaceInvocation(genType.resolvedn);
				@NotNull final WlGenerateNamespace gen = new WlGenerateNamespace(gf, ni, phase.generatedClasses);
				wl.addJob(gen);
			} else if (genType.resolved != null) {
				final ClassStatement c = genType.resolved.getClassOf();
				final @NotNull OS_Module mod = c.getContext().module();
				final @NotNull GenerateFunctions gf = phase.generatePhase.getGenerateFunctions(mod);
				@Nullable ClassInvocation ci;
				if (genType.ci == null) {
					ci = new ClassInvocation(c, null);
					ci = phase.registerClassInvocation(ci);
				} else {
					assert genType.ci instanceof ClassInvocation;
					ci = (ClassInvocation) genType.ci;
				}
				@Nullable final WlGenerateClass gen = new WlGenerateClass(gf, ci, phase.generatedClasses);
				wl.addJob(gen);
			}
			//
			aWorkManager.addJobs(wl);
		}

		public void action_function(@NotNull final FunctionInvocation aDependentFunction, @NotNull final WorkManager aWorkManager) {
			final BaseFunctionDef function = aDependentFunction.getFunction();
			final WorkJob gen;
			final @NotNull OS_Module mod;
			if (function == ConstructorDef.defaultVirtualCtor) {
				final ClassInvocation ci = aDependentFunction.getClassInvocation();
				if (ci == null) {
					final NamespaceInvocation ni = aDependentFunction.getNamespaceInvocation();
					if (ni == null)
						assert false;
					mod = ni.getNamespace().getContext().module();
				} else {
					mod = ci.getKlass().getContext().module();
				}
				final @NotNull GenerateFunctions gf = getGenerateFunctions(mod);
				gen = new WlGenerateDefaultCtor(gf, aDependentFunction);
			} else {
				mod = function.getContext().module();
				final @NotNull GenerateFunctions gf = getGenerateFunctions(mod);
				gen = new WlGenerateFunction(gf, aDependentFunction);
			}
			wl.addJob(gen);
			aWorkManager.addJobs(wl);
		}
	}

	private void resolve_cte_expression(@NotNull final ConstantTableEntry cte, final Context aContext) {
		final IExpression initialValue = cte.initialValue;
		switch (initialValue.getKind()) {
		case NUMERIC:
			resolve_cte_expression_builtin(cte, aContext, BuiltInTypes.SystemInteger);
			break;
		case STRING_LITERAL:
			resolve_cte_expression_builtin(cte, aContext, BuiltInTypes.String_);
			break;
		case CHAR_LITERAL:
			resolve_cte_expression_builtin(cte, aContext, BuiltInTypes.SystemCharacter);
			break;
		case IDENT:
			{
				final OS_Type a = cte.getTypeTableEntry().getAttached();
				if (a != null) {
					assert a.getType() != null;
					if (a.getType() == OS_Type.Type.BUILT_IN && a.getBType() == BuiltInTypes.Boolean) {
						assert BuiltInTypes.isBooleanText(cte.getName());
					} else
						throw new NotImplementedException();
				} else {
					assert false;
				}
				break;
			}
		default:
			{
				LOG.err("8192 "+initialValue.getKind());
				throw new NotImplementedException();
			}
		}
	}

	private void resolve_cte_expression_builtin(@NotNull final ConstantTableEntry cte, final Context aContext, final BuiltInTypes aBuiltInType) {
		final OS_Type a = cte.getTypeTableEntry().getAttached();
		if (a == null || a.getType() != OS_Type.Type.USER_CLASS) {
			try {
				cte.getTypeTableEntry().setAttached(resolve_type(new OS_Type(aBuiltInType), aContext));
			} catch (final ResolveError resolveError) {
				System.out.println("117 Can't be here");
//				resolveError.printStackTrace(); // TODO print diagnostic
			}
		}
	}

	class Lookup_function_on_exit {
		@NotNull WorkList wl = new WorkList();

		public void action(@NotNull final ProcTableEntry pte) {
			final FunctionInvocation fi = pte.getFunctionInvocation();
			if (fi == null) return;

			if (fi.getFunction() == null) {
				if (fi.pte == null) {
					return;
				} else {
//					LOG.err("592 " + fi.getClassInvocation());
					if (fi.pte.getClassInvocation() != null)
						fi.setClassInvocation(fi.pte.getClassInvocation());
//					else
//						fi.pte.setClassInvocation(fi.getClassInvocation());
				}
			}

			@Nullable ClassInvocation ci = fi.getClassInvocation();
			BaseFunctionDef fd3 = fi.getFunction();
			if (ci == null) {
				ci = fi.pte.getClassInvocation();
			}
			if (fd3 == ConstructorDef.defaultVirtualCtor) {
				if (ci == null) {
					if (/*fi.getClassInvocation() == null &&*/ fi.getNamespaceInvocation() == null) {
						// Assume default constructor
						ci = new ClassInvocation((ClassStatement) pte.getResolvedElement(), null);
						ci = phase.registerClassInvocation(ci);
						fi.setClassInvocation(ci);
					} else
						throw new NotImplementedException();
				}
				final ClassStatement klass = ci.getKlass();

				final Collection<ConstructorDef> cis = klass.getConstructors();
				for (@NotNull final ConstructorDef constructorDef : cis) {
					final Iterable<FormalArgListItem> constructorDefArgs = constructorDef.getArgs();

					if (!constructorDefArgs.iterator().hasNext()) { // zero-sized arg list
						fd3 = constructorDef;
						break;
					}
				}
			}

			final OS_Element parent;
			if (fd3 != null) {
				parent = fd3.getParent();
				if (parent instanceof ClassStatement) {
					ci = new ClassInvocation((ClassStatement) parent, null);
					{
						final ClassInvocation classInvocation = pte.getClassInvocation();
						if (classInvocation != null) {
							final Map<TypeName, OS_Type> gp = classInvocation.genericPart;
							if (gp != null) {
								int i = 0;
								for (final Map.@NotNull Entry<TypeName, OS_Type> entry : gp.entrySet()) {
									ci.set(i, entry.getKey(), entry.getValue());
									i++;
								}
							}
						}
					}
					proceed(fi, ci, (ClassStatement) parent, wl);
				} else if (parent instanceof NamespaceStatement) {
					proceed(fi, (NamespaceStatement) parent, wl);
				}
			} else {
				parent = ci.getKlass();
				{
					final ClassInvocation classInvocation = pte.getClassInvocation();
					if (classInvocation != null && classInvocation.genericPart != null) {
						final Map<TypeName, OS_Type> gp = classInvocation.genericPart;
						int i = 0;
						for (final Map.@NotNull Entry<TypeName, OS_Type> entry : gp.entrySet()) {
							ci.set(i, entry.getKey(), entry.getValue());
							i++;
						}
					}
				}
				proceed(fi, ci, (ClassStatement) parent, wl);
			}

//			proceed(fi, ci, parent);
		}

		void proceed(@NotNull final FunctionInvocation fi, ClassInvocation ci, final ClassStatement aParent, @NotNull final WorkList wl) {
			ci = phase.registerClassInvocation(ci);

			final ClassStatement kl = ci.getKlass(); // TODO Don't you see aParent??
			assert kl != null;

			final BaseFunctionDef fd2 = fi.getFunction();
			int state = 0;

			if (fd2 == ConstructorDef.defaultVirtualCtor) {
				if (fi.pte.getArgs().size() == 0)
					state = 1;
				else
					state = 2;
			} else if (fd2 instanceof ConstructorDef) {
				if (fi.getClassInvocation().getConstructorName() != null)
					state = 3;
				else
					state = 2;
			} else {
				if (fi.getFunction() == null && fi.getClassInvocation() != null)
					state = 3;
				else
					state = 4;
			}

			switch (state) {
			case 1:
				assert fi.pte.getArgs().size() == 0;
				// default ctor
				wl.addJob(new WlGenerateDefaultCtor(phase.generatePhase.getGenerateFunctions(module), fi));
				break;
			case 2:
				wl.addJob(new WlGenerateCtor(phase.generatePhase.getGenerateFunctions(module), fi, null)); // TODO check this
				break;
			case 3:
				// README this is a special case to generate constructor
				// TODO should it be GenerateDefaultCtor? (check args size and ctor-name)
				final String constructorName = fi.getClassInvocation().getConstructorName();
				final @NotNull IdentExpression constructorName1 = constructorName != null ? IdentExpression.forString(constructorName) : null;
				wl.addJob(new WlGenerateCtor(phase.generatePhase.getGenerateFunctions(module), fi, constructorName1));
				break;
			case 4:
				wl.addJob(new WlGenerateFunction(phase.generatePhase.getGenerateFunctions(module), fi));
				break;
			default:
				throw new NotImplementedException();
			}

			wm.addJobs(wl);
		}

		void proceed(@NotNull final FunctionInvocation fi, @NotNull final NamespaceStatement aParent, @NotNull final WorkList wl) {
//			ci = phase.registerClassInvocation(ci);

			final @NotNull OS_Module module1 = aParent.getContext().module();

			final NamespaceInvocation nsi = phase.registerNamespaceInvocation(aParent);

			wl.addJob(new WlGenerateNamespace(phase.generatePhase.getGenerateFunctions(module1), nsi, phase.generatedClasses));
			wl.addJob(new WlGenerateFunction(phase.generatePhase.getGenerateFunctions(module1), fi));

			wm.addJobs(wl);
		}
	}

	public void resolve_ident_table_entry(@NotNull final IdentTableEntry ite, @NotNull final BaseGeneratedFunction generatedFunction, final Context ctx) {
		@Nullable InstructionArgument itex = new IdentIA(ite.getIndex(), generatedFunction);
		{
			while (itex != null && itex instanceof IdentIA) {
				@NotNull final IdentTableEntry itee = ((IdentIA) itex).getEntry();

				@Nullable BaseTableEntry x = null;
				if (itee.backlink instanceof IntegerIA) {
					@NotNull final VariableTableEntry vte = ((IntegerIA) itee.backlink).getEntry();
					x = vte;
//					if (vte.constructable_pte != null)
					itex = null;
				} else if (itee.backlink instanceof IdentIA) {
					x = ((IdentIA) itee.backlink).getEntry();
					itex = ((IdentTableEntry) x).backlink;
				} else if (itee.backlink instanceof ProcIA) {
					x = ((ProcIA) itee.backlink).getEntry();
//					if (itee.getCallablePTE() == null)
//						// turned out to be wrong (by double calling), so let's wrap it
//						itee.setCallablePTE((ProcTableEntry) x);
					itex = null; //((ProcTableEntry) x).backlink;
				} else if (itee.backlink == null) {
					itex = null;
					x = null;
				}

				if (x != null) {
//					LOG.err("162 Adding FoundParent for "+itee);
//					LOG.err(String.format("1656 %s \n\t %s \n\t%s", x, itee, itex));
					x.addStatusListener(new FoundParent(x, itee, itee.getIdent().getContext(), generatedFunction)); // TODO context??
				}
			}
		}
		if (ite.getResolvedElement() != null)
			return;
		if (true || ite.backlink == null) {
			final @NotNull IdentIA identIA = new IdentIA(ite.getIndex(), generatedFunction);
			resolveIdentIA_(ite.getPC(), identIA, generatedFunction, new FoundElement(phase) {

				final String x = generatedFunction.getIdentIAPathNormal(identIA);

				@Override
				public void foundElement(final OS_Element e) {
//					ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(e)); // this is called in resolveIdentIA_
					found_element_for_ite(generatedFunction, ite, e, ctx);
				}

				@Override
				public void noFoundElement() {
					ite.setStatus(BaseTableEntry.Status.UNKNOWN, null);
					//errSink.reportError("1004 Can't find element for "+ x); // Already reported by 1179
				}
			});
		}
	}

	public void resolve_var_table_entry(@NotNull final VariableTableEntry vte, final BaseGeneratedFunction generatedFunction, final Context ctx) {
		if (vte.getResolvedElement() == null)
			return;
		{
			if (vte.type.getAttached() == null && vte.constructable_pte != null) {
				final ClassStatement c = vte.constructable_pte.getFunctionInvocation().getClassInvocation().getKlass();
				final @NotNull OS_Type attached = new OS_Type(c);
				// TODO this should have been set somewhere already
				//  typeName and nonGenericTypeName are not set
				//  but at this point probably wont be needed
				vte.type.genType.resolved = attached;
				vte.type.setAttached(attached);
			}
			vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(vte.getResolvedElement()));
			{
				final GenType genType = vte.type.genType;
				if (genType.resolved != null && genType.node == null) {
					genCI(genType, genType.nonGenericTypeName);
//					genType.node = makeNode(genType);
					//
					// registerClassInvocation does the job of makeNode, so results should be immediately available
					//
					((ClassInvocation) genType.ci).resolvePromise().then(new DoneCallback<GeneratedClass>() {
						@Override
						public void onDone(final GeneratedClass result) {
							genType.node = result;
							if (!vte.typePromise().isResolved()) // HACK
								vte.resolveType(genType);
						}
					});
				}
			}
		}
	}

//	private GeneratedNode makeNode(GenType aGenType) {
//		if (aGenType.ci instanceof ClassInvocation) {
//			final ClassInvocation ci = (ClassInvocation) aGenType.ci;
//			@NotNull GenerateFunctions gen = phase.generatePhase.getGenerateFunctions(ci.getKlass().getContext().module());
//			WlGenerateClass wlgc = new WlGenerateClass(gen, ci, phase.generatedClasses);
//			wlgc.run(null);
//			return wlgc.getResult();
//		}
//		return null;
//	}

	class Implement_construct {

		private final BaseGeneratedFunction generatedFunction;
		private final Instruction instruction;
		private final InstructionArgument expression;

		private final @NotNull ProcTableEntry pte;

		public Implement_construct(final BaseGeneratedFunction aGeneratedFunction, final Instruction aInstruction) {
			generatedFunction = aGeneratedFunction;
			instruction = aInstruction;

			// README all these asserts are redundant, I know
			assert instruction.getName() == InstructionName.CONSTRUCT;
			assert instruction.getArg(0) instanceof ProcIA;

			final int pte_num = ((ProcIA) instruction.getArg(0)).getIndex();
			pte = generatedFunction.getProcTableEntry(pte_num);

			expression = pte.expression_num;

			assert expression instanceof IntegerIA || expression instanceof IdentIA;
		}

		public void action() {
			if (expression instanceof IntegerIA) {
				action_IntegerIA();
			} else if (expression instanceof IdentIA) {
				action_IdentIA();
			} else {
				throw new NotImplementedException();
			}
		}

		public void action_IdentIA() {
			@NotNull final IdentTableEntry idte = ((IdentIA)expression).getEntry();
			final DeducePath deducePath = idte.buildDeducePath(generatedFunction);
			{
				@Nullable OS_Element el3;
				@Nullable Context ectx = generatedFunction.getFD().getContext();
				for (int i = 0; i < deducePath.size(); i++) {
					final InstructionArgument ia2 = deducePath.getIA(i);

					el3 = deducePath.getElement(i);

					if (ia2 instanceof IntegerIA) {
						@NotNull final VariableTableEntry vte = ((IntegerIA) ia2).getEntry();
						// TODO will fail if we try to construct a tmp var, but we never try to do that
						assert vte.vtt != VariableTableType.TEMP;
						assert el3     != null;
						assert i       == 0;
						ectx  = deducePath.getContext(i);
					} else if (ia2 instanceof IdentIA) {
						@NotNull final IdentTableEntry idte2 = ((IdentIA) ia2).getEntry();
						final String s = idte2.getIdent().toString();
						final LookupResultList lrl = ectx.lookup(s);
						@Nullable final OS_Element el2 = lrl.chooseBest(null);
						if (el2 == null) {
							assert el3 instanceof VariableStatement;
							@Nullable final VariableStatement vs = (VariableStatement) el3;
							@NotNull final TypeName tn = vs.typeName();
							@NotNull final OS_Type ty = new OS_Type(tn);

							if (idte2.type == null) {
								// README Don't remember enough about the constructors to select a different one
								@NotNull final TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, ty);
								try {
									@NotNull final GenType resolved = resolve_type(ty, tn.getContext());
									LOG.err("892 resolved: "+resolved);
									tte.setAttached(resolved);
								} catch (final ResolveError aResolveError) {
									errSink.reportDiagnostic(aResolveError);
								}

								idte2.type = tte;
							}
							// s is constructor name
							implement_construct_type(idte2, ty, s);
							return;
						} else {
							if (i+1 == deducePath.size()) {
								assert el3 == el2;
								if (el2 instanceof ConstructorDef) {
									@Nullable final GenType type = deducePath.getType(i);
									if (type.nonGenericTypeName == null) {
										type.nonGenericTypeName = deducePath.getType(i-1).nonGenericTypeName; // HACK. not guararnteed to work!
									}
									@NotNull final OS_Type ty = new OS_Type(type.nonGenericTypeName);
									implement_construct_type(idte2, ty, s);
								}
							} else {
								ectx = deducePath.getContext(i);
							}
						}
//						implement_construct_type(idte/*??*/, ty, null); // TODO how bout when there is no ctor name
					} else {
						throw new NotImplementedException();
					}
				}
			}
		}

		public void action_IntegerIA() {
			@NotNull final VariableTableEntry vte = generatedFunction.getVarTableEntry(((IntegerIA) expression).getIndex());
			assert vte.type.getAttached() != null; // TODO will fail when empty variable expression
			@Nullable final OS_Type ty = vte.type.getAttached();
			implement_construct_type(vte, ty, null);
		}

		private void implement_construct_type(@Nullable final Constructable co, @Nullable final OS_Type aTy, final String constructorName) {
			assert aTy != null;
			if (aTy.getType() == OS_Type.Type.USER) {
				final TypeName tyn = aTy.getTypeName();
				if (tyn instanceof NormalTypeName) {
					final @NotNull NormalTypeName tyn1 = (NormalTypeName) tyn;
					_implement_construct_type(co, constructorName, (NormalTypeName) tyn);
				}
			} else
				throw new NotImplementedException();
			if (co != null) {
				co.setConstructable(pte);
				final ClassInvocation best = pte.getClassInvocation();
				assert best != null;
				best.promise().done(new DoneCallback<GeneratedClass>() {
					@Override
					public void onDone(final GeneratedClass result) {
						co.resolveTypeToClass(result);
					}
				});
			}
		}

		private void _implement_construct_type(@Nullable final Constructable co, @Nullable final String constructorName, @NotNull final NormalTypeName aTyn1) {
			final String s = aTyn1.getName();
			final LookupResultList lrl = aTyn1.getContext().lookup(s);
			@Nullable final OS_Element best = lrl.chooseBest(null);
			assert best instanceof ClassStatement;
			@NotNull final List<TypeName> gp = ((ClassStatement) best).getGenericPart();
			@Nullable ClassInvocation clsinv = new ClassInvocation((ClassStatement) best, constructorName);
			if (gp.size() > 0) {
				final TypeNameList gp2 = aTyn1.getGenericPart();
				for (int i = 0; i < gp.size(); i++) {
					final TypeName typeName = gp2.get(i);
					@NotNull final GenType typeName2;
					try {
						// TODO transition to GenType
						typeName2 = resolve_type(new OS_Type(typeName), typeName.getContext());
						clsinv.set(i, gp.get(i), typeName2.resolved);
					} catch (final ResolveError aResolveError) {
						aResolveError.printStackTrace();
					}
				}
			}
			clsinv = phase.registerClassInvocation(clsinv);
			if (co != null) {
				if (co instanceof IdentTableEntry) {
					final @Nullable IdentTableEntry idte3 = (IdentTableEntry) co;
					idte3.type.genTypeCI(clsinv);
					clsinv.resolvePromise().then(new DoneCallback<GeneratedClass>() {
						@Override
						public void onDone(final GeneratedClass result) {
							idte3.resolveTypeToClass(result);
						}
					});
				} else if (co instanceof VariableTableEntry) {
					final @NotNull VariableTableEntry vte = (VariableTableEntry) co;
					vte.type.genTypeCI(clsinv);
					clsinv.resolvePromise().then(new DoneCallback<GeneratedClass>() {
						@Override
						public void onDone(final GeneratedClass result) {
							vte.resolveTypeToClass(result);
						}
					});
				}
			}
			pte.setClassInvocation(clsinv);
			pte.setResolvedElement(best);
			// set FunctionInvocation with pte args
			{
				@Nullable ConstructorDef cc = null;
				if (constructorName != null) {
					final Collection<ConstructorDef> cs = ((ClassStatement) best).getConstructors();
					for (@NotNull final ConstructorDef c : cs) {
						if (c.name().equals(constructorName)) {
							cc = c;
							break;
						}
					}
				}
				// TODO also check arguments
				{
					if (cc == null) assert pte.getArgs().size() == 0;
					@NotNull final FunctionInvocation fi = newFunctionInvocation(cc, pte, clsinv, phase);
					pte.setFunctionInvocation(fi);
				}
			}
		}
	}

	void implement_construct(final BaseGeneratedFunction generatedFunction, final Instruction instruction) {
		final @NotNull Implement_construct ic = newImplement_construct(generatedFunction, instruction);
		ic.action();
	}

	@NotNull
	public DeduceTypes2.Implement_construct newImplement_construct(final BaseGeneratedFunction generatedFunction, final Instruction instruction) {
		return new Implement_construct(generatedFunction, instruction);
	}

	void resolve_function_return_type(@NotNull final BaseGeneratedFunction generatedFunction) {
		// MODERNIZATION Does this have any affinity with DeferredMember?
		@Nullable final InstructionArgument vte_index = generatedFunction.vte_lookup("Result");
		if (vte_index != null) {
			final @NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(to_int(vte_index));

			if (vte.type.getAttached() != null) {
				phase.typeDecided((GeneratedFunction) generatedFunction, vte.type.genType);
			} else {
				@NotNull final Collection<TypeTableEntry> pot1 = vte.potentialTypes();
				@NotNull final ArrayList<TypeTableEntry> pot = new ArrayList<TypeTableEntry>(pot1);
				if (pot.size() == 1) {
					phase.typeDecided((GeneratedFunction) generatedFunction, pot.get(0).genType);
				} else if (pot.size() == 0) {
					@NotNull final GenType unitType = new GenType();
					unitType.typeName = new OS_Type(BuiltInTypes.Unit);
					phase.typeDecided((GeneratedFunction) generatedFunction, unitType);
				} else {
					// TODO report some kind of error/diagnostic and/or let ForFunction know...
					errSink.reportWarning("Can't resolve type of `Result'. potentialTypes > 1 for "+vte);
				}
			}
		} else {
			if (generatedFunction instanceof GeneratedConstructor) {
				// cant set return type of constructors
			} else {
				// if Result is not present, then make function return Unit
				// TODO May not be correct in all cases, such as when Value is present
				// but works for current code structure, where Result is a always present
				@NotNull final GenType unitType = new GenType();
				unitType.typeName = new OS_Type(BuiltInTypes.Unit);
				phase.typeDecided((GeneratedFunction) generatedFunction, unitType);
			}
		}
	}

	static class DeduceClient1 {
		private final DeduceTypes2 dt2;

		@Contract(pure = true)
		public DeduceClient1(final DeduceTypes2 aDeduceTypes2) {
			dt2 = aDeduceTypes2;
		}

		public @Nullable OS_Element _resolveAlias(@NotNull final AliasStatement aAliasStatement) {
			return DeduceLookupUtils._resolveAlias(aAliasStatement, dt2);
		}

		public void found_element_for_ite(final BaseGeneratedFunction aGeneratedFunction, @NotNull final IdentTableEntry aIte, final OS_Element aX, final Context aCtx) {
			dt2.found_element_for_ite(aGeneratedFunction, aIte, aX, aCtx);
		}

		public @NotNull GenType resolve_type(@NotNull final OS_Type aType, final Context aCtx) throws ResolveError {
			return dt2.resolve_type(aType, aCtx);
		}

		public @Nullable IInvocation getInvocationFromBacklink(final InstructionArgument aInstructionArgument) {
			return dt2.getInvocationFromBacklink(aInstructionArgument);
		}

		public @NotNull DeferredMember deferred_member(final OS_Element aParent, final IInvocation aInvocation, final VariableStatement aVariableStatement, @NotNull final IdentTableEntry aIdentTableEntry) {
			return dt2.deferred_member(aParent, aInvocation, aVariableStatement, aIdentTableEntry);
		}

		public void genCI(final GenType aResult, final TypeName aNonGenericTypeName) {
			dt2.genCI(aResult, aNonGenericTypeName);
		}

		public @Nullable ClassInvocation registerClassInvocation(final ClassStatement aClassStatement, final String aS) {
			return dt2.phase.registerClassInvocation(aClassStatement, aS);
		}
	}

	void found_element_for_ite(final BaseGeneratedFunction generatedFunction, @NotNull final IdentTableEntry ite, @Nullable final OS_Element y, final Context ctx) {
		assert y == ite.getResolvedElement();

		@NotNull final Found_Element_For_ITE fefi = new Found_Element_For_ITE(generatedFunction, ctx, LOG, errSink, new DeduceClient1(this));
		fefi.action(ite);
	}

	private @Nullable IInvocation getInvocationFromBacklink(@Nullable final InstructionArgument aBacklink) {
		if (aBacklink == null) return null;
		// TODO implement me
		return null;
	}

	private @NotNull DeferredMember deferred_member(final OS_Element aParent, final IInvocation aInvocation, final VariableStatement aVariableStatement, @NotNull final IdentTableEntry ite) {
		@NotNull final DeferredMember dm = deferred_member(aParent, aInvocation, aVariableStatement);
		dm.externalRef().then(new DoneCallback<GeneratedNode>() {
			@Override
			public void onDone(final GeneratedNode result) {
				ite.externalRef = result;
			}
		});
		return dm;
	}

	private @Nullable DeferredMember deferred_member(final OS_Element aParent, @Nullable IInvocation aInvocation, final VariableStatement aVariableStatement) {
		if (aInvocation == null) {
			if (aParent instanceof NamespaceStatement)
				aInvocation = phase.registerNamespaceInvocation((NamespaceStatement) aParent);
		}
		@Nullable final DeferredMember dm = new DeferredMember(aParent, aInvocation, aVariableStatement);
		phase.addDeferredMember(dm);
		return dm;
	}

	@NotNull
	public GenType resolve_type(final @NotNull OS_Type type, final Context ctx) throws ResolveError {
		return resolve_type(module, type, ctx);
	}

	/*static*/
	@NotNull GenType resolve_type(final OS_Module module, final @NotNull OS_Type type, final Context ctx) throws ResolveError {
		@NotNull final GenType R = new GenType();
		R.typeName = type;

		switch (type.getType()) {

			case BUILT_IN:
			{
				switch (type.getBType()) {
				case SystemInteger:
					{
						@NotNull final String typeName = type.getBType().name();
						assert typeName.equals("SystemInteger");
						OS_Module prelude = module.prelude;
						if (prelude == null) // README Assume `module' IS prelude
							prelude = module;
						final LookupResultList lrl = prelude.getContext().lookup(typeName);
						@Nullable OS_Element best = lrl.chooseBest(null);
						while (!(best instanceof ClassStatement)) {
							if (best instanceof AliasStatement) {
								best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, this);
							} else if (OS_Type.isConcreteType(best)) {
								throw new NotImplementedException();
							} else
								throw new NotImplementedException();
						}
						if (best == null) {
							throw new ResolveError(IdentExpression.forString(typeName), lrl);
						}
						R.resolved = new OS_Type((ClassStatement) best);
						break;
					}
				case String_:
					{
						@NotNull final String typeName = type.getBType().name();
						assert typeName.equals("String_");
						OS_Module prelude = module.prelude;
						if (prelude == null) // README Assume `module' IS prelude
							prelude = module;
						final LookupResultList lrl = prelude.getContext().lookup("ConstString"); // TODO not sure about String
						@Nullable OS_Element best = lrl.chooseBest(null);
						while (!(best instanceof ClassStatement)) {
							if (best instanceof AliasStatement) {
								best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, this);
							} else if (OS_Type.isConcreteType(best)) {
								throw new NotImplementedException();
							} else
								throw new NotImplementedException();
						}
						if (best == null) {
							throw new ResolveError(IdentExpression.forString(typeName), lrl);
						}
						R.resolved = new OS_Type((ClassStatement) best);
						break;
					}
				case SystemCharacter:
					{
						@NotNull final String typeName = type.getBType().name();
						assert typeName.equals("SystemCharacter");
						OS_Module prelude = module.prelude;
						if (prelude == null) // README Assume `module' IS prelude
							prelude = module;
						final LookupResultList lrl = prelude.getContext().lookup("SystemCharacter");
						@Nullable OS_Element best = lrl.chooseBest(null);
						while (!(best instanceof ClassStatement)) {
							if (best instanceof AliasStatement) {
								best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, this);
							} else if (OS_Type.isConcreteType(best)) {
								throw new NotImplementedException();
							} else
								throw new NotImplementedException();
						}
						if (best == null) {
							throw new ResolveError(IdentExpression.forString(typeName), lrl);
						}
						R.resolved = new OS_Type((ClassStatement) best);
						break;
					}
				case Boolean:
					{
						OS_Module prelude = module.prelude;
						if (prelude == null) // README Assume `module' IS prelude
							prelude = module;
						final LookupResultList lrl = prelude.getContext().lookup("Boolean");
						final @Nullable OS_Element best = lrl.chooseBest(null);
						R.resolved = new OS_Type((ClassStatement) best); // TODO might change to Type
						break;
					}
				default:
					throw new IllegalStateException("531 Unexpected value: " + type.getBType());
				}
				break;
			}
		case USER:
			{
				final TypeName tn1 = type.getTypeName();
				switch (tn1.kindOfType()) {
				case NORMAL:
					{
						final Qualident tn = ((NormalTypeName) tn1).getRealName();
						LOG.info("799 [resolving USER type named] " + tn);
						final LookupResultList lrl = DeduceLookupUtils.lookupExpression(tn, tn1.getContext(), this);
						@Nullable OS_Element best = lrl.chooseBest(null);
						while (best instanceof AliasStatement) {
							best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, this);
						}
						if (best == null) {
							if (tn.asSimpleString().equals("Any"))
								/*return*/R.resolved = new OS_AnyType(); // TODO not a class
							throw new ResolveError(tn1, lrl);
						}

						if (best instanceof ClassContext.OS_TypeNameElement) {
							/*return*/R.resolved = new OS_GenericTypeNameType((ClassContext.OS_TypeNameElement) best); // TODO not a class
						} else
							R.resolved = new OS_Type((ClassStatement) best);
						break;
					}
				case FUNCTION:
				case GENERIC:
				case TYPE_OF:
					throw new NotImplementedException();
				default:
					throw new IllegalStateException("414 Unexpected value: " + tn1.kindOfType());
				}
			}
		case USER_CLASS:
			break;
		case FUNCTION:
			break;
		default:
			throw new IllegalStateException("565 Unexpected value: " + type.getType());
		}

		return R;
	}

	private void do_assign_constant(final @NotNull BaseGeneratedFunction generatedFunction, final @NotNull Instruction instruction, final @NotNull VariableTableEntry vte, final @NotNull ConstTableIA i2) {
		if (vte.type.getAttached() != null) {
			// TODO check types
		}
		final @NotNull ConstantTableEntry cte = generatedFunction.getConstTableEntry(i2.getIndex());
		if (cte.type.getAttached() == null) {
			LOG.info("Null type in CTE "+cte);
		}
//		vte.type = cte.type;
		vte.addPotentialType(instruction.getIndex(), cte.type);
	}

	private void do_assign_call(final @NotNull BaseGeneratedFunction generatedFunction,
								final @NotNull Context ctx,
								final @NotNull VariableTableEntry vte,
								final @NotNull FnCallArgs fca,
								final @NotNull Instruction instruction) {
		final int instructionIndex = instruction.getIndex();
		final @NotNull ProcTableEntry pte = generatedFunction.getProcTableEntry(to_int(fca.getArg(0)));
		@NotNull final IdentIA identIA = (IdentIA) pte.expression_num;

		if (vte.getStatus() == BaseTableEntry.Status.UNCHECKED) {
			pte.typePromise().then(new DoneCallback<GenType>() {
				@Override
				public void onDone(final GenType result) {
					vte.resolveType(result);
				}
			});
			if (vte.getResolvedElement() != null) {
				try {
					final OS_Element el;
					if (vte.getResolvedElement() instanceof IdentExpression)
						el = DeduceLookupUtils.lookup((IdentExpression) vte.getResolvedElement(), ctx, this);
					else
						el = DeduceLookupUtils.lookup(((VariableStatement) vte.getResolvedElement()).getNameToken(), ctx, this);
					vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));
				} catch (final ResolveError aResolveError) {
					errSink.reportDiagnostic(aResolveError);
					return;
				}
			}
		}

		if (identIA != null){
//			LOG.info("594 "+identIA.getEntry().getStatus());

			resolveIdentIA_(ctx, identIA, generatedFunction, new FoundElement(phase) {

				final String xx = generatedFunction.getIdentIAPathNormal(identIA);

				@Override
				public void foundElement(final OS_Element e) {
//					LOG.info(String.format("600 %s %s", xx ,e));
//					LOG.info("601 "+identIA.getEntry().getStatus());
					final OS_Element resolved_element = identIA.getEntry().getResolvedElement();
					assert e == resolved_element;
//					set_resolved_element_pte(identIA, e, pte);
					pte.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(e, identIA));
					pte.onFunctionInvocation(new DoneCallback<FunctionInvocation>() {
						@Override
						public void onDone(@NotNull final FunctionInvocation result) {
							result.generateDeferred().done(new DoneCallback<BaseGeneratedFunction>() {
								@Override
								public void onDone(@NotNull final BaseGeneratedFunction bgf) {
									@NotNull final PromiseExpectation<GenType> pe = promiseExpectation(bgf, "Function Result type");
									bgf.typePromise().then(new DoneCallback<GenType>() {
										@Override
										public void onDone(@NotNull final GenType result) {
											pe.satisfy(result);
											@NotNull final TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, result.resolved); // TODO there has to be a better way
											tte.genType.copy(result);
											vte.addPotentialType(instructionIndex, tte);
										}
									});
								}
							});
						}
					});
				}

				@Override
				public void noFoundElement() {
					// TODO create Diagnostic and quit
					LOG.info("1005 Can't find element for " + xx);
				}
			});
		}
		final List<TypeTableEntry> args = pte.getArgs();
		for (int i = 0; i < args.size(); i++) {
			final TypeTableEntry tte = args.get(i); // TODO this looks wrong
//			LOG.info("770 "+tte);
			final IExpression e = tte.expression;
			if (e == null) continue;
			switch (e.getKind()) {
			case NUMERIC:
				tte.setAttached(new OS_Type(BuiltInTypes.SystemInteger));
				//vte.type = tte;
				break;
			case CHAR_LITERAL:
				tte.setAttached(new OS_Type(BuiltInTypes.SystemCharacter));
				break;
			case IDENT:
				do_assign_call_args_ident(generatedFunction, ctx, vte, instructionIndex, pte, i, tte, (IdentExpression) e);
				break;
			case PROCEDURE_CALL:
				{
					final @NotNull ProcedureCallExpression pce = (ProcedureCallExpression) e;
					try {
						final LookupResultList lrl = DeduceLookupUtils.lookupExpression(pce.getLeft(), ctx, this);
						@Nullable OS_Element best = lrl.chooseBest(null);
						if (best != null) {
							while (best instanceof AliasStatement) {
								best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, this);
							}
							if (best instanceof FunctionDef) {
								final OS_Element parent = best.getParent();
								@Nullable final IInvocation invocation;
								if (parent instanceof NamespaceStatement) {
									invocation = phase.registerNamespaceInvocation((NamespaceStatement) parent);
								} else if (parent instanceof ClassStatement) {
									@NotNull final ClassInvocation ci = new ClassInvocation((ClassStatement) parent, null);
									invocation = phase.registerClassInvocation(ci);
								} else 
									throw new NotImplementedException(); // TODO implement me
								
								forFunction(newFunctionInvocation((FunctionDef) best, pte, invocation, phase), new ForFunction() {
									@Override
									public void typeDecided(@NotNull final GenType aType) {
										tte.setAttached(gt(aType)); // TODO stop setting attached!
										tte.genType.copy(aType);
//										vte.addPotentialType(instructionIndex, tte);
									}
								});
//								tte.setAttached(new OS_FuncType((FunctionDef) best));
								
							} else {
								final int y=2;
								throw new NotImplementedException();
							}
						} else {
							final int y=2;
							throw new NotImplementedException();
						}
					} catch (final ResolveError aResolveError) {
						aResolveError.printStackTrace();
						final int y=2;
						throw new NotImplementedException();
					}
				}
				break;
			case DOT_EXP:
				{
					final @NotNull DotExpression de = (DotExpression) e;
					try {
						final LookupResultList lrl = DeduceLookupUtils.lookupExpression(de.getLeft(), ctx, this);
						@Nullable OS_Element best = lrl.chooseBest(null);
						if (best != null) {
							while (best instanceof AliasStatement) {
								best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, this);
							}
							if (best instanceof FunctionDef) {
								tte.setAttached(new OS_FuncType((FunctionDef) best));
								//vte.addPotentialType(instructionIndex, tte);
							} else if (best instanceof ClassStatement) {
								tte.setAttached(new OS_Type((ClassStatement) best));
							} else if (best instanceof VariableStatement) {
								final @NotNull VariableStatement vs = (VariableStatement) best;
								@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(vs.getName());
								final TypeTableEntry tte1 = ((IntegerIA) vte_ia).getEntry().type;
								tte.setAttached(tte1.getAttached());
							} else {
								final int y=2;
								LOG.err(best.getClass().getName());
								throw new NotImplementedException();
							}
						} else {
							final int y=2;
							throw new NotImplementedException();
						}
					} catch (final ResolveError aResolveError) {
						aResolveError.printStackTrace();
						final int y=2;
						throw new NotImplementedException();
					}
				}
				break;

			case GET_ITEM:
				{
					final @NotNull GetItemExpression gie = (GetItemExpression) e;
					do_assign_call_GET_ITEM(gie, tte, generatedFunction, ctx);
					continue;
				}
//				break;
			default:
				throw new IllegalStateException("Unexpected value: " + e.getKind());
			}
		}
		{
			if (pte.expression_num == null) {
				if (fca.expression_to_call.getName() != InstructionName.CALLS) {
					final String text = ((IdentExpression) pte.expression).getText();
					final LookupResultList lrl = ctx.lookup(text);

					final @Nullable OS_Element best = lrl.chooseBest(null);
					if (best != null)
						pte.setResolvedElement(best); // TODO do we need to add a dependency for class?
					else {
						errSink.reportError("Cant resolve "+text);
					}
				} else {
					implement_calls(generatedFunction, ctx.getParent(), instruction.getArg(1), pte, instructionIndex);
				}
			} else {
				final int y=2;
				resolveIdentIA_(ctx, identIA, generatedFunction, new FoundElement(phase) {

					final String x = generatedFunction.getIdentIAPathNormal(identIA);

					@Override
					public void foundElement(final OS_Element el) {
						if (pte.getResolvedElement() == null)
							pte.setResolvedElement(el);
						if (el instanceof FunctionDef) {
							@NotNull final FunctionDef fd = (FunctionDef) el;
							final @Nullable IInvocation invocation;
							if (fd.getParent() == generatedFunction.getFD().getParent()) {
								invocation = getInvocation((GeneratedFunction) generatedFunction);
							} else {
								if (fd.getParent() instanceof NamespaceStatement) {
									final NamespaceInvocation ni = phase.registerNamespaceInvocation((NamespaceStatement) fd.getParent());
									invocation = ni;
								} else if (fd.getParent() instanceof ClassStatement) {
									final @NotNull ClassStatement classStatement = (ClassStatement) fd.getParent();
									@Nullable ClassInvocation ci = new ClassInvocation(classStatement, null);
									final @NotNull List<TypeName> genericPart = classStatement.getGenericPart();
									if (genericPart.size() > 0) {
										// TODO handle generic parameters somehow (getInvocationFromBacklink?)

									}
									ci = phase.registerClassInvocation(ci);
									invocation = ci;
								} else
									throw new NotImplementedException();
							}
							forFunction(newFunctionInvocation(fd, pte, invocation, phase), new ForFunction() {
								@Override
								public void typeDecided(@NotNull final GenType aType) {
									if (!vte.typeDeferred_isPending()) {
										if (vte.resolvedType() == null) {
											final @Nullable ClassInvocation ci = genCI(aType, null);
											vte.type.genTypeCI(ci);
											ci.resolvePromise().then(new DoneCallback<GeneratedClass>() {
												@Override
												public void onDone(final GeneratedClass result) {
													vte.resolveTypeToClass(result);
												}
											});
										}
										LOG.err("2041 type already found "+vte);
										return; // type already found
									}
									// I'm not sure if below is ever called
									@NotNull final TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, gt(aType), pte.expression, pte);
									vte.addPotentialType(instructionIndex, tte);
								}
							});
						} else if (el instanceof ClassStatement) {
							@NotNull final ClassStatement kl = (ClassStatement) el;
							@NotNull final OS_Type type = new OS_Type(kl);
							@NotNull final TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, type, pte.expression, pte);
							vte.addPotentialType(instructionIndex, tte);
							vte.setConstructable(pte);

							register_and_resolve(vte, kl);
						} else {
							LOG.err("7890 "+el.getClass().getName());
						}
					}

					@Override
					public void noFoundElement() {
						LOG.err("IdentIA path cannot be resolved "+ x);
					}
				});
			}
		}
	}

	@NotNull PromiseExpectations expectations = new PromiseExpectations();

	class PromiseExpectations {
		long counter = 0;

		@NotNull List<PromiseExpectation> exp = new ArrayList<>();

		public void add(@NotNull final PromiseExpectation aExpectation) {
			counter++;
			aExpectation.setCounter(counter);
			exp.add(aExpectation);
		}

		public void check() {
			for (@NotNull final PromiseExpectation promiseExpectation : exp) {
				if (!promiseExpectation.isSatisfied())
					promiseExpectation.fail();
			}
		}
	}

	public interface ExpectationBase {
		String expectationString();
	}

	public class PromiseExpectation<B> {

		private final ExpectationBase base;
		private B result;
		private long counter;
		private final String desc;
		private boolean satisfied;
		private boolean _printed;

		public PromiseExpectation(final ExpectationBase aBase, final String aDesc) {
			base = aBase;
			desc = aDesc;
		}

		public void satisfy(final B aResult) {
			result = aResult;
			satisfied = true;
			LOG.info(String.format("Expectation (%s, %d) met: %s %s", DeduceTypes2.this, counter, desc, base.expectationString()));
		}

		public void fail() {
			if (!_printed) {
				LOG.err(String.format("Expectation (%s, %d) not met", DeduceTypes2.this, counter));
				_printed = true;
			}
		}

		public boolean isSatisfied() {
			return satisfied;
		}

		public void setCounter(final long aCounter) {
			counter = aCounter;

			LOG.info(String.format("Expectation (%s, %d) set: %s %s", DeduceTypes2.this, counter, desc, base.expectationString()));
		}
	}

	public <B> @NotNull PromiseExpectation<B> promiseExpectation(final ExpectationBase base, final String desc) {
		final @NotNull PromiseExpectation<B> promiseExpectation = new PromiseExpectation<>(base, desc);
		expectations.add(promiseExpectation);
		return promiseExpectation;
	}

	public IInvocation getInvocation(@NotNull final GeneratedFunction generatedFunction) {
		final ClassInvocation classInvocation = generatedFunction.fi.getClassInvocation();
		final NamespaceInvocation ni;
		if (classInvocation == null) {
			ni = generatedFunction.fi.getNamespaceInvocation();
			return ni;
		} else
			return classInvocation;
	}

	private void do_assign_call_args_ident(@NotNull final BaseGeneratedFunction generatedFunction,
										   @NotNull final Context ctx,
										   @NotNull final VariableTableEntry vte,
										   final int aInstructionIndex,
										   @NotNull final ProcTableEntry aPte,
										   final int aI,
										   @NotNull final TypeTableEntry aTte,
										   @NotNull final IdentExpression aExpression) {
		final String e_text = aExpression.getText();
		final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(e_text);
//		LOG.info("10000 "+vte_ia);
		if (vte_ia != null) {
			final @NotNull VariableTableEntry vte1 = generatedFunction.getVarTableEntry(to_int(vte_ia));
			final Promise<GenType, Void, Void> p = vte1.typePromise();
			p.done(new DoneCallback<GenType>() {
				@Override
				public void onDone(final GenType result) {
//					assert vte != vte1;
//					aTte.setAttached(result.resolved != null ? result.resolved : result.typeName);
					aTte.genType.copy(result);
//					vte.addPotentialType(aInstructionIndex, result); // TODO!!
				}
			});
			@NotNull final Runnable runnable = new Runnable() {
				@Override
				public void run() {
					final @NotNull List<TypeTableEntry> ll = getPotentialTypesVte((GeneratedFunction) generatedFunction, vte_ia);
					doLogic(ll);
				}

				public void doLogic(@NotNull final List<TypeTableEntry> potentialTypes) {
					assert potentialTypes.size() >= 0;
					switch (potentialTypes.size()) {
						case 1:
//							tte.attached = ll.get(0).attached;
//							vte.addPotentialType(instructionIndex, ll.get(0));
							if (p.isResolved())
								LOG.info(String.format("1047 (vte already resolved) %s vte1.type = %s, gf = %s, tte1 = %s %n", vte1.getName(), vte1.type, generatedFunction, potentialTypes.get(0)));
							else {
								final OS_Type attached = potentialTypes.get(0).getAttached();
								if (attached == null) return;
								switch (attached.getType()) {
									case USER:
										vte1.type.setAttached(attached); // !!
										break;
									case USER_CLASS:
										final GenType gt = vte1.genType;
										gt.resolved = attached;
										vte1.resolveType(gt);
										break;
									default:
										errSink.reportWarning("Unexpected value: " + attached.getType());
//										throw new IllegalStateException("Unexpected value: " + attached.getType());
								}
							}
							break;
						case 0:
							// README moved up here to elimiate work
							if (p.isResolved()) {
								System.out.printf("890-1 Already resolved type: vte1.type = %s, gf = %s %n", vte1.type, generatedFunction);
								break;
							}
							final LookupResultList lrl = ctx.lookup(e_text);
							@Nullable final OS_Element best = lrl.chooseBest(null);
							if (best instanceof FormalArgListItem) {
								@NotNull final FormalArgListItem fali = (FormalArgListItem) best;
								final @NotNull OS_Type osType = new OS_Type(fali.typeName());
								if (!osType.equals(vte.type.getAttached())) {
									@NotNull final TypeTableEntry tte1 = generatedFunction.newTypeTableEntry(
											TypeTableEntry.Type.SPECIFIED, osType, fali.getNameToken(), vte1);
									/*if (p.isResolved())
										System.out.printf("890 Already resolved type: vte1.type = %s, gf = %s, tte1 = %s %n", vte1.type, generatedFunction, tte1);
									else*/ {
										final OS_Type attached = tte1.getAttached();
										switch (attached.getType()) {
											case USER:
												vte1.type.setAttached(attached); // !!
												break;
											case USER_CLASS:
												final GenType gt = vte1.genType;
												gt.resolved = attached;
												vte1.resolveType(gt);
												break;
											default:
												errSink.reportWarning("2853 Unexpected value: " + attached.getType());
//												throw new IllegalStateException("Unexpected value: " + attached.getType());
										}
									}
								}
//								vte.type = tte1;
//								tte.attached = tte1.attached;
//								vte.setStatus(BaseTableEntry.Status.KNOWN, best);
							} else if (best instanceof VariableStatement) {
								final @NotNull VariableStatement vs = (VariableStatement) best;
								//
								assert vs.getName().equals(e_text);
								//
								@Nullable final InstructionArgument vte2_ia = generatedFunction.vte_lookup(vs.getName());
								@NotNull final VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(vte2_ia));
								if (p.isResolved())
									System.out.printf("915 Already resolved type: vte2.type = %s, gf = %s %n", vte1.type, generatedFunction);
								else {
									final GenType gt = vte1.genType;
									final OS_Type attached = vte2.type.getAttached();
									gt.resolved = attached;
									vte1.resolveType(gt);
								}
//								vte.type = vte2.type;
//								tte.attached = vte.type.attached;
								vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
								vte2.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best)); // TODO ??
							} else {
								final int y = 2;
								LOG.err("543 " + best.getClass().getName());
								throw new NotImplementedException();
							}
							break;
						default:
							// TODO hopefully this works
							final @NotNull ArrayList<TypeTableEntry> potentialTypes1 = new ArrayList<TypeTableEntry>(
									Collections2.filter(potentialTypes, new Predicate<TypeTableEntry>() {
										@Override
										public boolean apply(@org.checkerframework.checker.nullness.qual.Nullable final TypeTableEntry input) {
											assert input != null;
											return input.getAttached() != null;
										}
									}));
							// prevent infinite recursion
							if (potentialTypes1.size() < potentialTypes.size())
								doLogic(potentialTypes1);
							else
								LOG.info("913 Don't know");
							break;
					}
				}
			};
			onFinish(runnable);
		} else {
			final int ia = generatedFunction.addIdentTableEntry(aExpression, ctx);
			@NotNull final IdentTableEntry idte = generatedFunction.getIdentTableEntry(ia);
			idte.addPotentialType(aInstructionIndex, aTte); // TODO DotExpression??
			final int ii = aI;
			idte.onType(phase, new OnType() {
				@Override
				public void typeDeduced(@NotNull final OS_Type aType) {
					aPte.setArgType(ii, aType); // TODO does this belong here or in FunctionInvocation?
					aTte.setAttached(aType); // since we know that tte.attached is always null here
				}

				@Override
				public void noTypeFound() {
					LOG.err("719 no type found "+generatedFunction.getIdentIAPathNormal(new IdentIA(ia, generatedFunction)));
				}
			});
		}
	}

	private void do_assign_call_GET_ITEM(@NotNull final GetItemExpression gie, final TypeTableEntry tte, @NotNull final BaseGeneratedFunction generatedFunction, final Context ctx) {
		try {
			final LookupResultList lrl = DeduceLookupUtils.lookupExpression(gie.getLeft(), ctx, this);
			final @Nullable OS_Element best = lrl.chooseBest(null);
			if (best != null) {
				if (best instanceof VariableStatement) { // TODO what about alias?
					@NotNull final VariableStatement vs = (VariableStatement) best;
					final String s = vs.getName();
					@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(s);
					if (vte_ia != null) {
						@NotNull final VariableTableEntry vte1 = generatedFunction.getVarTableEntry(to_int(vte_ia));
						throw new NotImplementedException();
					} else {
						final IdentTableEntry idte = generatedFunction.getIdentTableEntryFor(vs.getNameToken());
						assert idte != null;
						@Nullable OS_Type ty = idte.type.getAttached();
						idte.onType(phase, new OnType() {
							@Override public void typeDeduced(final @NotNull OS_Type ty) {
								assert ty != null;
								@NotNull GenType rtype = null;
								try {
									rtype = resolve_type(ty, ctx);
								} catch (final ResolveError resolveError) {
	//								resolveError.printStackTrace();
									errSink.reportError("Cant resolve " + ty); // TODO print better diagnostic
									return;
								}
								if (rtype.resolved != null && rtype.resolved.getType() == OS_Type.Type.USER_CLASS) {
									final LookupResultList lrl2 = rtype.resolved.getClassOf().getContext().lookup("__getitem__");
									@Nullable final OS_Element best2 = lrl2.chooseBest(null);
									if (best2 != null) {
										if (best2 instanceof FunctionDef) {
											@NotNull final FunctionDef fd = (FunctionDef) best2;
											@Nullable final ProcTableEntry pte = null;
											final IInvocation invocation = getInvocation((GeneratedFunction) generatedFunction);
											forFunction(newFunctionInvocation(fd, pte, invocation, phase), new ForFunction() {
												@Override
												public void typeDecided(final @NotNull GenType aType) {
													assert fd == generatedFunction.getFD();
													//
													if (idte.type == null) {
														@NotNull final TypeTableEntry tte1 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, gt(aType), idte); // TODO expression?
														idte.type = tte1;
													} else
														idte.type.setAttached(gt(aType));
												}
											});
										} else {
											throw new NotImplementedException();
										}
									} else {
										throw new NotImplementedException();
									}
								}
							}

							@Override
							public void noTypeFound() {
								throw new NotImplementedException();
							}
						});
						if (ty == null) {
							@NotNull final TypeTableEntry tte3 = generatedFunction.newTypeTableEntry(
									TypeTableEntry.Type.SPECIFIED, new OS_Type(vs.typeName()), vs.getNameToken());
							idte.type = tte3;
							ty = idte.type.getAttached();
						}
					}

	//				tte.attached = new OS_FuncType((FunctionDef) best); // TODO: what is this??
					//vte.addPotentialType(instructionIndex, tte);
				} else if (best instanceof FormalArgListItem) {
					final @Nullable FormalArgListItem fali = (FormalArgListItem) best;
					final String s = fali.name();
					@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(s);
					if (vte_ia != null) {
						@NotNull final VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(vte_ia));

	//					final @Nullable OS_Type ty2 = vte2.type.attached;
						vte2.typePromise().done(new DoneCallback<GenType>() {
							@Override
							public void onDone(@NotNull final GenType result) {
	//							assert false; // TODO this code is never reached
								final @Nullable OS_Type ty2 = result.typeName/*.getAttached()*/;
								assert ty2 != null;
								@NotNull GenType rtype = null;
								try {
									rtype = resolve_type(ty2, ctx);
								} catch (final ResolveError resolveError) {
	//								resolveError.printStackTrace();
									errSink.reportError("Cant resolve " + ty2); // TODO print better diagnostic
									return;
								}
								if (rtype.resolved != null && rtype.resolved.getType() == OS_Type.Type.USER_CLASS) {
									final LookupResultList lrl2 = rtype.resolved.getClassOf().getContext().lookup("__getitem__");
									@Nullable final OS_Element best2 = lrl2.chooseBest(null);
									if (best2 != null) {
										if (best2 instanceof FunctionDef) {
											@Nullable final FunctionDef fd = (FunctionDef) best2;
											@Nullable final ProcTableEntry pte = null;
											final IInvocation invocation = getInvocation((GeneratedFunction) generatedFunction);
											forFunction(newFunctionInvocation(fd, pte, invocation, phase), new ForFunction() {
												@Override
												public void typeDecided(final @NotNull GenType aType) {
													assert fd == generatedFunction.getFD();
													//
													@NotNull final TypeTableEntry tte1 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, gt(aType), vte2); // TODO expression?
													vte2.type = tte1;
												}
											});
										} else {
											throw new NotImplementedException();
										}
									} else {
										throw new NotImplementedException();
									}
								}
							}
						});
	//					vte2.onType(phase, new OnType() {
	//						@Override public void typeDeduced(final OS_Type ty2) {
	//						}
	//
	//						@Override
	//						public void noTypeFound() {
	//							throw new NotImplementedException();
	//						}
	//					});
	/*
						if (ty2 == null) {
							@NotNull TypeTableEntry tte3 = generatedFunction.newTypeTableEntry(
									TypeTableEntry.Type.SPECIFIED, new OS_Type(fali.typeName()), fali.getNameToken());
							vte2.type = tte3;
	//						ty2 = vte2.type.attached; // TODO this is final, but why assign anyway?
						}
	*/
					}
				} else {
					final int y=2;
					throw new NotImplementedException();
				}
			} else {
				final int y=2;
				throw new NotImplementedException();
			}
		} catch (final ResolveError aResolveError) {
			aResolveError.printStackTrace();
			final int y=2;
			throw new NotImplementedException();
		}
	}

/*
	private void forFunction(GeneratedFunction gf, ForFunction forFunction) {
		phase.forFunction(this, gf, forFunction);
	}
*/

	void forFunction(@NotNull final FunctionInvocation gf, @NotNull final ForFunction forFunction) {
		phase.forFunction(this, gf, forFunction);
	}

	private void do_assign_constant(final @NotNull BaseGeneratedFunction generatedFunction, final @NotNull Instruction instruction, final @NotNull IdentTableEntry idte, final @NotNull ConstTableIA i2) {
		if (idte.type != null && idte.type.getAttached() != null) {
			// TODO check types
		}
		final @NotNull ConstantTableEntry cte = generatedFunction.getConstTableEntry(i2.getIndex());
		if (cte.type.getAttached() == null) {
			LOG.err("*** ERROR: Null type in CTE "+cte);
		}
		// idte.type may be null, but we still addPotentialType here
		idte.addPotentialType(instruction.getIndex(), cte.type);
	}

	private void do_assign_call(final @NotNull BaseGeneratedFunction generatedFunction,
								final @NotNull Context ctx,
								final @NotNull IdentTableEntry idte,
								final @NotNull FnCallArgs fca,
								final int instructionIndex) {
		final @NotNull ProcTableEntry pte = generatedFunction.getProcTableEntry(to_int(fca.getArg(0)));
		for (final @NotNull TypeTableEntry tte : pte.getArgs()) {
			LOG.info("771 "+tte);
			final IExpression e = tte.expression;
			if (e == null) continue;
			switch (e.getKind()) {
			case NUMERIC:
			{
				tte.setAttached(new OS_Type(BuiltInTypes.SystemInteger));
				idte.type = tte; // TODO why not addPotentialType ? see below for example
			}
			break;
			case IDENT:
			{
				final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(((IdentExpression) e).getText());
				final @NotNull List<TypeTableEntry> ll = getPotentialTypesVte((GeneratedFunction) generatedFunction, vte_ia);
				if (ll.size() == 1) {
					tte.setAttached(ll.get(0).getAttached());
					idte.addPotentialType(instructionIndex, ll.get(0));
				} else
					throw new NotImplementedException();
			}
			break;
			default:
			{
				throw new NotImplementedException();
			}
			}
		}
		{
			final LookupResultList lrl = ctx.lookup(((IdentExpression)pte.expression).getText());
			final @Nullable OS_Element best = lrl.chooseBest(null);
			if (best != null)
				pte.setResolvedElement(best); // TODO do we need to add a dependency for class?
			else
				throw new NotImplementedException();
		}
	}

	private void implement_calls(final @NotNull BaseGeneratedFunction gf, final @NotNull Context context, final InstructionArgument i2, final @NotNull ProcTableEntry fn1, final int pc) {
		if (gf.deferred_calls.contains(pc)) {
			LOG.err("Call is deferred "/*+gf.getInstruction(pc)*/+" "+fn1);
			return;
		}
		implement_calls_(gf, context, i2, fn1, pc);
	}

	class Implement_Calls_ {
		private final BaseGeneratedFunction gf;
		private final Context context;
		private final InstructionArgument i2;
		private final ProcTableEntry pte;
		private final int pc;

		public Implement_Calls_(final @NotNull BaseGeneratedFunction aGf,
								final @NotNull Context aContext,
								final @NotNull InstructionArgument aI2,
								final @NotNull ProcTableEntry aPte,
								final int aPc) {
			gf = aGf;
			context = aContext;
			i2 = aI2;
			pte = aPte;
			pc = aPc;
		}

		void action() {
			final IExpression pn1 = pte.expression;
			if (!(pn1 instanceof IdentExpression)) {
				throw new IllegalStateException("pn1 is not IdentExpression");
			}

			final String pn = ((IdentExpression) pn1).getText();
			boolean found = lookup_name_calls(context, pn, pte);
			if (found) return;

			final @Nullable String pn2 = SpecialFunctions.reverse_name(pn);
			if (pn2 != null) {
//				LOG.info("7002 "+pn2);
				found = lookup_name_calls(context, pn2, pte);
				if (found) return;
			}

			if (i2 instanceof IntegerIA) {
				found = action_i2_IntegerIA(pn, pn2);
			} else {
				found = action_dunder(pn);
			}

			if (!found)
				pte.setStatus(BaseTableEntry.Status.UNKNOWN, null);
		}

		private boolean action_dunder(final String pn) {
			assert Pattern.matches("__[a-z]+__", pn);
//			LOG.info(String.format("i2 is not IntegerIA (%s)",i2.getClass().getName()));
			//
			// try to get dunder method from class
			//
			final IExpression exp = pte.getArgs().get(0).expression;
			if (exp instanceof IdentExpression) {
				return action_dunder_doIt(pn, (IdentExpression) exp);
			}
			return false;
		}

		private boolean action_dunder_doIt(final String pn, final IdentExpression exp) {
			final @NotNull IdentExpression identExpression = exp;
			@Nullable final InstructionArgument vte_ia = gf.vte_lookup(identExpression.getText());
			if (vte_ia != null) {
				((IntegerIA) vte_ia).getEntry().typePromise().then(new DoneCallback<GenType>() {
					@Override
					public void onDone(@NotNull final GenType result) {
						final boolean found1 = lookup_name_calls(result.resolved.getClassOf().getContext(), pn, pte);
						if (found1) {
							final int y=2;
							System.out.println("3071 "+pte.getStatus());
							final IInvocation invocation = result.ci;
//							final BaseFunctionDef fd = gf.getFD();
							final BaseFunctionDef fd = pte.getFunctionInvocation().getFunction();
							@NotNull final FunctionInvocation fi = newFunctionInvocation(fd, pte, invocation, phase);
							pte.setFunctionInvocation(fi);
						} else {
							final int y=3;
							System.out.println("3074");
						}
					}
				});
				return true;
			}
			return false;
		}

		private boolean action_i2_IntegerIA(final String pn, @Nullable final String pn2) {
			final boolean found;
			final @NotNull VariableTableEntry vte = gf.getVarTableEntry(to_int(i2));
			final Context ctx = gf.getContextFromPC(pc); // might be inside a loop or something
			final String vteName = vte.getName();
			if (vteName != null) {
				found = action_i2_IntegerIA_vteName_is_null(pn, pn2, ctx, vteName);
			} else {
				found = action_i2_IntegerIA_vteName_is_not_null(pn, pn2, vte);
			}
			return found;
		}

		private boolean action_i2_IntegerIA_vteName_is_not_null(final String pn, @Nullable final String pn2, @NotNull final VariableTableEntry vte) {
			final @NotNull List<TypeTableEntry> tt = getPotentialTypesVte(vte);
			if (tt.size() != 1) {
				return false;
			}
			final OS_Type x = tt.get(0).getAttached();
			assert x != null;
			switch (x.getType()) {
			case USER_CLASS:
				pot_types_size_is_1_USER_CLASS(pn, pn2, x);
				return true;
			case BUILT_IN:
				final Context ctx2 = context;//x.getTypeName().getContext();
				try {
					@NotNull final GenType ty2 = resolve_type(x, ctx2);
					pot_types_size_is_1_USER_CLASS(pn, pn2, ty2.resolved);
					return true;
				} catch (final ResolveError resolveError) {
					resolveError.printStackTrace();
					errSink.reportDiagnostic(resolveError);
					return false;
				}
			default:
				assert false;
				return false;
			}
		}

		private void pot_types_size_is_1_USER_CLASS(final String pn, @Nullable final String pn2, final OS_Type x) {
			boolean found;
			final Context ctx1 = x.getClassOf().getContext();

			found = lookup_name_calls(ctx1, pn, pte);
			if (found) return;

			if (pn2 != null) {
				found = lookup_name_calls(ctx1, pn2, pte);
			}

			if (!found) {
				//throw new NotImplementedException(); // TODO
				errSink.reportError("Special Function not found " + pn);
			}
		}

		private boolean action_i2_IntegerIA_vteName_is_null(final String pn, @Nullable final String pn2, final Context ctx, final String vteName) {
			boolean found = false;
			if (SpecialVariables.contains(vteName)) {
				LOG.err("Skipping special variable " + vteName + " " + pn);
			} else {
				final LookupResultList lrl2 = ctx.lookup(vteName);
//				LOG.info("7003 "+vteName+" "+ctx);
				final @Nullable OS_Element best2 = lrl2.chooseBest(null);
				if (best2 != null) {
					found = lookup_name_calls(best2.getContext(), pn, pte);
					if (found) return true;

					if (pn2 != null) {
						found = lookup_name_calls(best2.getContext(), pn2, pte);
						if (found) return true;
					}

					errSink.reportError("Special Function not found " + pn);
				} else {
					throw new NotImplementedException(); // Cant find vte, should never happen
				}
			}
			return found;
		}
	}

	private void implement_calls_(final @NotNull BaseGeneratedFunction gf,
								  final @NotNull Context context,
								  final InstructionArgument i2,
								  final @NotNull ProcTableEntry pte,
								  final int pc) {
		final Implement_Calls_ ic = new Implement_Calls_(gf, context, i2, pte, pc);
		ic.action();
	}

	private boolean lookup_name_calls(final @NotNull Context ctx, final @NotNull String pn, final @NotNull ProcTableEntry pte) {
		final LookupResultList lrl = ctx.lookup(pn);
		final @Nullable OS_Element best = lrl.chooseBest(null); // TODO check arity and arg matching
		if (best != null) {
			pte.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(best, null)); // TODO why include if only to be null?
			return true;
		}
		return false;
	}

	public static int to_int(@NotNull final InstructionArgument arg) {
		if (arg instanceof IntegerIA)
			return ((IntegerIA) arg).getIndex();
		if (arg instanceof ProcIA)
			return ((ProcIA) arg).getIndex();
		if (arg instanceof IdentIA)
			return ((IdentIA) arg).getIndex();
		throw new NotImplementedException();
	}

	public void resolveIdentIA2_(@NotNull final Context context, @NotNull final IdentIA identIA, @NotNull final GeneratedFunction generatedFunction, @NotNull final FoundElement foundElement) {
		final @NotNull List<InstructionArgument> s = generatedFunction._getIdentIAPathList(identIA);
		resolveIdentIA2_(context, identIA, s, generatedFunction, foundElement);
	}

	public void resolveIdentIA_(@NotNull final Context context, @NotNull final IdentIA identIA, final BaseGeneratedFunction generatedFunction, @NotNull final FoundElement foundElement) {
		@NotNull final Resolve_Ident_IA ria = new Resolve_Ident_IA(this, phase, context, identIA, generatedFunction, foundElement, errSink);
		ria.action();
	}

	public static class Holder<T> {
		private T el;

		public void set(final T el) {
			this.el = el;
		}

		public T get() {
			return el;
		}
	}


	public void resolveIdentIA2_(@NotNull final Context ctx,
								 @Nullable final IdentIA identIA,
								 @Nullable final List<InstructionArgument> s,
								 @NotNull final BaseGeneratedFunction generatedFunction,
								 @NotNull final FoundElement foundElement) {
		@NotNull final Resolve_Ident_IA2 ria2 = new Resolve_Ident_IA2(this, errSink, phase, generatedFunction, foundElement);
		ria2.resolveIdentIA2_(ctx, identIA, s);
	}

	OS_Type gt(@NotNull final GenType aType) {
		return aType.resolved != null ? aType.resolved : aType.typeName;
	}

	@NotNull ArrayList<TypeTableEntry> getPotentialTypesVte(@NotNull final VariableTableEntry vte) {
		return new ArrayList<TypeTableEntry>(vte.potentialTypes());
	}

	@NotNull
	private ArrayList<TypeTableEntry> getPotentialTypesVte(@NotNull final GeneratedFunction generatedFunction, @NotNull final InstructionArgument vte_index) {
		return getPotentialTypesVte(generatedFunction.getVarTableEntry(to_int(vte_index)));
	}

	static class DeduceClient2 {
		private final DeduceTypes2 deduceTypes2;

		public DeduceClient2(final DeduceTypes2 deduceTypes2) {
			this.deduceTypes2 = deduceTypes2;
		}

		public @Nullable ClassInvocation registerClassInvocation(@NotNull final ClassInvocation ci) {
			return deduceTypes2.phase.registerClassInvocation(ci);
		}

		public @NotNull FunctionInvocation newFunctionInvocation(final BaseFunctionDef constructorDef, final ProcTableEntry pte, @NotNull final IInvocation ci) {
			return deduceTypes2.newFunctionInvocation(constructorDef, pte, ci, deduceTypes2.phase);
		}

		public NamespaceInvocation registerNamespaceInvocation(final NamespaceStatement namespaceStatement) {
			return deduceTypes2.phase.registerNamespaceInvocation(namespaceStatement);
		}

		public @Nullable ClassInvocation genCI(@NotNull final GenType genType, final TypeName typeName) {
			return deduceTypes2.genCI(genType, typeName);
		}

		public @NotNull ElLog getLOG() {
			return deduceTypes2.LOG;
		}
	}

	public void register_and_resolve(@NotNull final VariableTableEntry aVte, @NotNull final ClassStatement aKlass) {
		@Nullable ClassInvocation ci = new ClassInvocation(aKlass, null);
		ci = phase.registerClassInvocation(ci);
		ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
			@Override
			public void onDone(final GeneratedClass result) {
				aVte.resolveTypeToClass(result);
			}
		});
	}

	public class FoundParent implements BaseTableEntry.StatusListener {
		private final BaseTableEntry bte;
		private final IdentTableEntry ite;
		private final Context ctx;
		private final BaseGeneratedFunction generatedFunction;

		@Contract(pure = true)
		public FoundParent(final BaseTableEntry aBte, final IdentTableEntry aIte, final Context aCtx, final BaseGeneratedFunction aGeneratedFunction) {
			bte = aBte;
			ite = aIte;
			ctx = aCtx;
			generatedFunction = aGeneratedFunction;
		}

		@Override
		public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
			if (newStatus == BaseTableEntry.Status.KNOWN) {
				if (bte instanceof VariableTableEntry) {
					final @NotNull VariableTableEntry vte = (VariableTableEntry) bte;
					onChangeVTE(vte);
				} else if (bte instanceof ProcTableEntry) {
					final @NotNull ProcTableEntry pte = (ProcTableEntry) bte;
					onChangePTE(pte);
				} else if (bte instanceof IdentTableEntry) {
					final @NotNull IdentTableEntry ite = (IdentTableEntry) bte;
					onChangeITE(ite);
				}
				postOnChange(eh);
			}
		}

		/* @ensures ite.type != null */
		private void postOnChange(@NotNull final IElementHolder eh) {
			if (ite.type == null && eh.getElement() instanceof VariableStatement) {
				@NotNull final TypeName typ = ((VariableStatement) eh.getElement()).typeName();
				@NotNull final OS_Type ty = new OS_Type(typ);

				try {
					@Nullable final GenType ty2 = getTY2(typ, ty);

					// no expression or TableEntryIV below
					if (ty2 != null) {
						final @NotNull TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null);
						// trying to keep genType up to date
						tte.setAttached(ty);
						tte.setAttached(ty2);
						ite.type = tte;
					}
				} catch (final ResolveError aResolveError) {
					errSink.reportDiagnostic(aResolveError);
				}
			}
		}

		private @Nullable GenType getTY2(@NotNull final TypeName aTyp, @NotNull final OS_Type aTy) throws ResolveError {
			if (aTy.getType() != OS_Type.Type.USER) {
				assert false;
				@NotNull final GenType genType = new GenType();
				genType.set(aTy);
				return genType;
			}

			@Nullable GenType ty2 = null;
			if (!aTyp.isNull()) {
				assert aTy.getTypeName() != null;
				ty2 = resolve_type(aTy, aTy.getTypeName().getContext());
				return ty2;
			}

			if (bte instanceof VariableTableEntry) {
				final OS_Type attached = ((VariableTableEntry) bte).type.getAttached();
				if (attached == null) {
					type_is_null_and_attached_is_null_vte();
					// ty2 will probably be null here
				} else {
					ty2 = new GenType();
					ty2.set(attached);
				}
			} else if (bte instanceof IdentTableEntry) {
				final TypeTableEntry tte = ((IdentTableEntry) bte).type;
				if (tte != null) {
					final OS_Type attached = tte.getAttached();

					if (attached == null) {
						type_is_null_and_attached_is_null_ite((IdentTableEntry) bte);
						// ty2 will be null here
					} else {
						ty2 = new GenType();
						ty2.set(attached);
					}
				}
			}

			return ty2;
		}

		private void type_is_null_and_attached_is_null_vte() {
			//LOG.err("2842 attached == null for "+((VariableTableEntry) bte).type);
			@NotNull final PromiseExpectation<GenType> pe = promiseExpectation((VariableTableEntry) bte, "Null USER type attached resolved");
			((VariableTableEntry) bte).typePromise().done(new DoneCallback<GenType>() {
				@Override
				public void onDone(@NotNull final GenType result) {
					pe.satisfy(result);
					final OS_Type attached1 = result.resolved != null ? result.resolved : result.typeName;
					if (attached1 != null) {
						switch (attached1.getType()) {
						case USER_CLASS:
							ite.type = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, attached1);
							break;
						case USER:
							try {
								@NotNull final GenType ty3 = resolve_type(attached1, attached1.getTypeName().getContext());
								// no expression or TableEntryIV below
								@NotNull final TypeTableEntry tte4 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null);
								// README trying to keep genType up to date
								tte4.setAttached(attached1);
								tte4.setAttached(ty3);
								ite.type = tte4; // or ty2?
							} catch (final ResolveError aResolveError) {
								aResolveError.printStackTrace();
							}
							break;
						}
					}
				}
			});
		}

		private void type_is_null_and_attached_is_null_ite(final IdentTableEntry ite) {
//			PromiseExpectation<GenType> pe = promiseExpectation(ite, "Null USER type attached resolved");
//			ite.typePromise().done(new DoneCallback<GenType>() {
//				@Override
//				public void onDone(GenType result) {
//					pe.satisfy(result);
//					final OS_Type attached1 = result.resolved != null ? result.resolved : result.typeName;
//					if (attached1 != null) {
//						switch (attached1.getType()) {
//						case USER_CLASS:
//							FoundParent.this.ite.type = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, attached1);
//							break;
//						case USER:
//							try {
//								OS_Type ty3 = resolve_type(attached1, attached1.getTypeName().getContext());
//								// no expression or TableEntryIV below
//								@NotNull TypeTableEntry tte4 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null);
//								// README trying to keep genType up to date
//								tte4.setAttached(attached1);
//								tte4.setAttached(ty3);
//								FoundParent.this.ite.type = tte4; // or ty2?
//							} catch (ResolveError aResolveError) {
//								aResolveError.printStackTrace();
//							}
//							break;
//						}
//					}
//				}
//			});
		}

		private void onChangePTE(@NotNull final ProcTableEntry aPte) {
			if (aPte.getStatus() == BaseTableEntry.Status.KNOWN) { // TODO might be obvious
				if (aPte.getFunctionInvocation() != null) {
					final FunctionInvocation fi = aPte.getFunctionInvocation();
					final BaseFunctionDef fd = fi.getFunction();
					if (fd instanceof ConstructorDef) {
						fi.generateDeferred().done(new DoneCallback<BaseGeneratedFunction>() {
							@Override
							public void onDone(final BaseGeneratedFunction result) {
								@NotNull final GeneratedConstructor constructorDef = (GeneratedConstructor) result;

								@NotNull final BaseFunctionDef ele = constructorDef.getFD();

								try {
									final LookupResultList lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), DeduceTypes2.this);
									@Nullable final OS_Element best = lrl.chooseBest(null);
									ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
								} catch (final ResolveError aResolveError) {
									aResolveError.printStackTrace();
									errSink.reportDiagnostic(aResolveError);
								}
							}
						});
					}
				} else
					throw new NotImplementedException();
			} else {
				LOG.info("1621");
				@Nullable LookupResultList lrl = null;
				try {
					lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ctx, DeduceTypes2.this);
					@Nullable final OS_Element best = lrl.chooseBest(null);
					assert best != null;
					ite.setResolvedElement(best);
					found_element_for_ite(null, ite, best, ctx);
//						ite.setStatus(BaseTableEntry.Status.KNOWN, best);
				} catch (final ResolveError aResolveError) {
					aResolveError.printStackTrace();
				}
			}
		}

		private void onChangeVTE(@NotNull final VariableTableEntry vte) {
			@NotNull final ArrayList<TypeTableEntry> pot = getPotentialTypesVte(vte);
			if (vte.getStatus() == BaseTableEntry.Status.KNOWN && vte.type.getAttached() != null && vte.getResolvedElement() != null) {

				final OS_Type ty = vte.type.getAttached();

				@Nullable OS_Element ele2 = null;

				try {
					if (ty.getType() == OS_Type.Type.USER) {
						@NotNull final GenType ty2 = resolve_type(ty, ty.getTypeName().getContext());
						final OS_Element ele;
						if (vte.type.genType.resolved == null) {
							if (ty2.resolved.getType() == OS_Type.Type.USER_CLASS) {
								vte.type.genType.copy(ty2);
							}
						}
						ele = ty2.resolved.getElement();
						final LookupResultList lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), DeduceTypes2.this);
						ele2 = lrl.chooseBest(null);
					} else
						ele2 = ty.getClassOf(); // TODO might fail later (use getElement?)

					@Nullable LookupResultList lrl = null;

					lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele2.getContext(), DeduceTypes2.this);
					@Nullable final OS_Element best = lrl.chooseBest(null);
					// README commented out because only firing for dir.listFiles, and we always use `best'
//					if (best != ele2) LOG.err(String.format("2824 Divergent for %s, %s and %s", ite, best, ele2));;
					ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
				} catch (final ResolveError aResolveError) {
					aResolveError.printStackTrace();
					errSink.reportDiagnostic(aResolveError);
				}
			} else if (pot.size() == 1) {
				final TypeTableEntry tte = pot.get(0);
				@Nullable final OS_Type ty = tte.getAttached();
				if (ty != null) {
					switch (ty.getType()) {
					case USER:
						vte_pot_size_is_1_USER_TYPE(vte, ty);
						break;
					case USER_CLASS:
						vte_pot_size_is_1_USER_CLASS_TYPE(vte, ty);
						break;
					}
				} else {
					LOG.err("1696");
				}
			}
		}

		private void onChangeITE(@NotNull final IdentTableEntry identTableEntry) {
			if (identTableEntry.type != null) {
				final OS_Type ty = identTableEntry.type.getAttached();

				@Nullable OS_Element ele2 = null;

				try {
					if (ty.getType() == OS_Type.Type.USER) {
						@NotNull final GenType ty2 = resolve_type(ty, ty.getTypeName().getContext());
						final OS_Element ele;
						if (identTableEntry.type.genType.resolved == null) {
							if (ty2.resolved.getType() == OS_Type.Type.USER_CLASS) {
								identTableEntry.type.genType.copy(ty2);
							}
						}
						ele = ty2.resolved.getElement();
						final LookupResultList lrl = DeduceLookupUtils.lookupExpression(this.ite.getIdent(), ele.getContext(), DeduceTypes2.this);
						ele2 = lrl.chooseBest(null);
					} else
						ele2 = ty.getClassOf(); // TODO might fail later (use getElement?)

					@Nullable LookupResultList lrl = null;

					lrl = DeduceLookupUtils.lookupExpression(this.ite.getIdent(), ele2.getContext(), DeduceTypes2.this);
					@Nullable final OS_Element best = lrl.chooseBest(null);
					// README commented out because only firing for dir.listFiles, and we always use `best'
//					if (best != ele2) LOG.err(String.format("2824 Divergent for %s, %s and %s", identTableEntry, best, ele2));;
					this.ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
				} catch (final ResolveError aResolveError) {
					aResolveError.printStackTrace();
					errSink.reportDiagnostic(aResolveError);
				}
			} else {
				if (!identTableEntry.fefi) {
					final Found_Element_For_ITE fefi = new Found_Element_For_ITE(generatedFunction, ctx, LOG, errSink, new DeduceClient1(DeduceTypes2.this));
					fefi.action(identTableEntry);
					identTableEntry.fefi = true;
					identTableEntry.onFefiDone(new DoneCallback<GenType>() {
						@Override
						public void onDone(final GenType result) {
							LookupResultList lrl = null;
							final OS_Element ele2;
							try {
								lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), result.resolved.getClassOf().getContext(), DeduceTypes2.this);
								ele2 = lrl.chooseBest(null);

								if (ele2 != null) {
									ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(ele2));
									ite.resolveTypeToClass(result.node);
								}
							} catch (final ResolveError aResolveError) {
								aResolveError.printStackTrace();
							}
						}
					});
				}
				// TODO we want to setStatus but have no USER or USER_CLASS to perform lookup with
			}
		}

		private void vte_pot_size_is_1_USER_CLASS_TYPE(@NotNull final VariableTableEntry vte, @Nullable final OS_Type aTy) {
			final ClassStatement klass = aTy.getClassOf();
			@Nullable LookupResultList lrl = null;
			try {
				lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), klass.getContext(), DeduceTypes2.this);
				@Nullable final OS_Element best = lrl.chooseBest(null);
//							ite.setStatus(BaseTableEntry.Status.KNOWN, best);
				assert best != null;
				ite.setResolvedElement(best);

				final @NotNull GenType genType = new GenType(klass);
				final TypeName typeName = vte.type.genType.nonGenericTypeName;
				final @Nullable ClassInvocation ci = genCI(genType, typeName);
//							resolve_vte_for_class(vte, klass);
				ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
					@Override
					public void onDone(final GeneratedClass result) {
						vte.resolveTypeToClass(result);
					}
				});
			} catch (final ResolveError aResolveError) {
				errSink.reportDiagnostic(aResolveError);
			}
		}

		private void vte_pot_size_is_1_USER_TYPE(@NotNull final VariableTableEntry vte, @Nullable final OS_Type aTy) {
			try {
				@NotNull final GenType ty2 = resolve_type(aTy, aTy.getTypeName().getContext());
				// TODO ite.setAttached(ty2) ??
				final OS_Element ele = ty2.resolved.getElement();
				final LookupResultList lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), DeduceTypes2.this);
				@Nullable final OS_Element best = lrl.chooseBest(null);
				ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
//									ite.setResolvedElement(best);

				final @NotNull ClassStatement klass = (ClassStatement) ele;

				register_and_resolve(vte, klass);
			} catch (final ResolveError resolveError) {
				errSink.reportDiagnostic(resolveError);
			}
		}
	}

	public static class ClassInvocationMake {
		public static ClassInvocation withGenericPart(ClassStatement best,
													  String constructorName,
													  NormalTypeName aTyn1,
													  DeduceTypes2 dt2,
													  final ErrSink aErrSink) {
			@NotNull GenericPart genericPart = new GenericPart(best, aTyn1);

			@Nullable ClassInvocation clsinv = new ClassInvocation(best, constructorName);

			if (genericPart.hasGenericPart()) {
				final @NotNull List<TypeName> gp = best.getGenericPart();
				final @NotNull TypeNameList gp2 = genericPart.getGenericPartFromTypeName();

				for (int i = 0; i < gp.size(); i++) {
					final TypeName typeName = gp2.get(i);
					@NotNull GenType typeName2;
					try {
						typeName2 = dt2.resolve_type(new OS_Type(typeName), typeName.getContext());
						// TODO transition to GenType
						clsinv.set(i, gp.get(i), typeName2.resolved);
					} catch (ResolveError aResolveError) {
//						aResolveError.printStackTrace();
						aErrSink.reportDiagnostic(aResolveError);
					}
				}
			}
			return clsinv;
		}
	}

	static class GenericPart {
		private final ClassStatement classStatement;
		private final TypeName genericTypeName;

		@Contract(pure = true)
		public GenericPart(final ClassStatement aClassStatement, final TypeName aGenericTypeName) {
			classStatement = aClassStatement;
			genericTypeName = aGenericTypeName;
		}

		@Contract(pure = true)
		public boolean hasGenericPart() {
			return classStatement.getGenericPart().size() > 0;
		}

		@Contract(pure = true)
		private NormalTypeName getGenericTypeName() {
			assert genericTypeName != null;
			assert genericTypeName instanceof NormalTypeName;

			return (NormalTypeName) genericTypeName;
		}

		@Contract(pure = true)
		public TypeNameList getGenericPartFromTypeName() {
			final NormalTypeName ntn = getGenericTypeName();
			return ntn.getGenericPart();
		}
	}


}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
