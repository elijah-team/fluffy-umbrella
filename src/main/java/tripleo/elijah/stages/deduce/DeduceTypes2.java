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

	public DeduceTypes2(@NotNull OS_Module module, @NotNull DeducePhase phase) {
		this(module, phase, ElLog.Verbosity.VERBOSE);
	}

	public DeduceTypes2(@NotNull OS_Module module, @NotNull DeducePhase phase, ElLog.Verbosity verbosity) {
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
				@NotNull GeneratedFunction generatedFunction = (GeneratedFunction) generatedNode;
				deduceOneFunction(generatedFunction, phase);
			}
		}
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
	<T> int df_helper(@NotNull List<GeneratedNode> aGeneratedClasses, @NotNull df_helper_i<T> dfhi) {
		int size = 0;
		for (GeneratedNode generatedNode : aGeneratedClasses) {
			@NotNull GeneratedContainerNC generatedContainerNC = (GeneratedContainerNC) generatedNode;
			final @Nullable df_helper<T> dfh = dfhi.get(generatedContainerNC);
			if (dfh == null) continue;
			@NotNull Collection<T> lgf2 = dfh.collection();
			for (final T generatedConstructor : lgf2) {
				if (dfh.deduce(generatedConstructor))
					size++;
			}
		}
		return size;
	}

	public void deduceClasses(final List<GeneratedNode> lgc) {
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
	}

	interface IElementProcessor {
		void elementIsNull();
		void hasElement(OS_Element el);
	}

	static class ProcessElement {
		static void processElement(OS_Element el, IElementProcessor ep) {
			if (el == null)
				ep.elementIsNull();
			else
				ep.hasElement(el);
		}
	}

	private GenType makeGenTypeFromOSType(final OS_Type aType, final @Nullable Map<TypeName, OS_Type> aGenericPart) {
		GenType gt = new GenType();
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
					int y=2;
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
					} catch (ResolveError aResolveError) {
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
		public @Nullable df_helper_Constructors get(GeneratedContainerNC aGeneratedContainerNC) {
			if (aGeneratedContainerNC instanceof GeneratedClass) // TODO namespace constructors
				return new df_helper_Constructors((GeneratedClass) aGeneratedContainerNC);
			else
				return null;
		}
	}

	class dfhi_functions implements df_helper_i<GeneratedFunction> {
		@Override
		public @NotNull df_helper_Functions get(GeneratedContainerNC aGeneratedContainerNC) {
			return new df_helper_Functions(aGeneratedContainerNC);
		}
	}

	interface df_helper<T> {
		@NotNull Collection<T> collection();

		boolean deduce(T generatedConstructor);
	}

	class df_helper_Constructors implements df_helper<GeneratedConstructor> {
		private final GeneratedClass generatedClass;

		public df_helper_Constructors(GeneratedClass aGeneratedClass) {
			generatedClass = aGeneratedClass;
		}

		@Override
		public @NotNull Collection<GeneratedConstructor> collection() {
			return generatedClass.constructors.values();
		}

		@Override
		public boolean deduce(@NotNull GeneratedConstructor generatedConstructor) {
			return deduceOneConstructor(generatedConstructor, phase);
		}
	}

	class df_helper_Functions implements df_helper<GeneratedFunction> {
		private final GeneratedContainerNC generatedContainerNC;

		public df_helper_Functions(GeneratedContainerNC aGeneratedContainerNC) {
			generatedContainerNC = aGeneratedContainerNC;
		}

		@Override
		public @NotNull Collection<GeneratedFunction> collection() {
			return generatedContainerNC.functionMap.values();
		}

		@Override
		public boolean deduce(@NotNull GeneratedFunction aGeneratedFunction) {
			return deduceOneFunction(aGeneratedFunction, phase);
		}
	}
	
	public boolean deduceOneFunction(@NotNull GeneratedFunction aGeneratedFunction, @NotNull DeducePhase aDeducePhase) {
		if (aGeneratedFunction.deducedAlready) return false;
		deduce_generated_function(aGeneratedFunction);
		aGeneratedFunction.deducedAlready = true;
		for (@NotNull IdentTableEntry identTableEntry : aGeneratedFunction.idte_list) {
			if (identTableEntry.getResolvedElement() instanceof VariableStatement) {
				final @NotNull VariableStatement vs = (VariableStatement) identTableEntry.getResolvedElement();
				OS_Element el = vs.getParent().getParent();
				OS_Element el2 = aGeneratedFunction.getFD().getParent();
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
					@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
					if (vte.vtt != VariableTableType.RESULT) {
						result_index = null;
					}
				}
			}
			if (result_index != null) {
				@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
				if (vte.resolvedType() == null) {
					GenType b = vte.genType;
					OS_Type a = vte.type.getAttached();
					if (a != null) {
						// see resolve_function_return_type
						switch (a.getType()) {
							case USER_CLASS:
								dof_uc(vte, a);
								break;
							case USER:
								vte.genType.typeName = a;
								try {
									@NotNull GenType rt = resolve_type(a, a.getTypeName().getContext());
									if (rt.resolved != null && rt.resolved.getType() == OS_Type.Type.USER_CLASS) {
										if (rt.resolved.getClassOf().getGenericPart().size() > 0)
											vte.genType.nonGenericTypeName = a.getTypeName(); // TODO might be wrong
										dof_uc(vte, rt.resolved);
									}
								} catch (ResolveError aResolveError) {
									errSink.reportDiagnostic(aResolveError);
								}
								break;
							default:
								// TODO do nothing for now
								int y3 = 2;
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

	public boolean deduceOneConstructor(@NotNull GeneratedConstructor aGeneratedConstructor, @NotNull DeducePhase aDeducePhase) {
		if (aGeneratedConstructor.deducedAlready) return false;
		deduce_generated_function_base(aGeneratedConstructor, aGeneratedConstructor.getFD());
		aGeneratedConstructor.deducedAlready = true;
		for (@NotNull IdentTableEntry identTableEntry : aGeneratedConstructor.idte_list) {
			if (identTableEntry.getResolvedElement() instanceof VariableStatement) {
				final @NotNull VariableStatement vs = (VariableStatement) identTableEntry.getResolvedElement();
				OS_Element el = vs.getParent().getParent();
				OS_Element el2 = aGeneratedConstructor.getFD().getParent();
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
					@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
					if (vte.vtt != VariableTableType.RESULT) {
						result_index = null;
					}
				}
			}
			if (result_index != null) {
				@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
				if (vte.resolvedType() == null) {
					GenType b = vte.genType;
					OS_Type a = vte.type.getAttached();
					if (a != null) {
						// see resolve_function_return_type
						switch (a.getType()) {
							case USER_CLASS:
								dof_uc(vte, a);
								break;
							case USER:
								b.typeName = a;
								try {
									@NotNull GenType rt = resolve_type(a, a.getTypeName().getContext());
									if (rt.resolved != null && rt.resolved.getType() == OS_Type.Type.USER_CLASS) {
										if (rt.resolved.getClassOf().getGenericPart().size() > 0)
											b.nonGenericTypeName = a.getTypeName(); // TODO might be wrong
										dof_uc(vte, rt.resolved);
									}
								} catch (ResolveError aResolveError) {
									errSink.reportDiagnostic(aResolveError);
								}
								break;
							default:
								// TODO do nothing for now
								int y3 = 2;
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

	private void dof_uc(@NotNull VariableTableEntry aVte, @NotNull OS_Type aA) {
		// we really want a ci from somewhere
		assert aA.getClassOf().getGenericPart().size() == 0;
		@Nullable ClassInvocation ci = new ClassInvocation(aA.getClassOf(), null);
		ci = phase.registerClassInvocation(ci);

		aVte.genType.resolved = aA; // README assuming OS_Type cannot represent namespaces
		aVte.genType.ci = ci;

		ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
			@Override
			public void onDone(GeneratedClass result) {
				aVte.resolveTypeToClass(result);
			}
		});
	}

	@NotNull List<Runnable> onRunnables = new ArrayList<Runnable>();

	void onFinish(Runnable r) {
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

	public void deduce_generated_function_base(final @NotNull BaseGeneratedFunction generatedFunction, @NotNull BaseFunctionDef fd) {
		final Context fd_ctx = fd.getContext();
		//
		{
			ProcTableEntry pte = generatedFunction.fi.pte;
			final @NotNull String pte_string = getPTEString(pte);
//			LOG.err("** deduce_generated_function "+ fd.name()+" "+pte_string);//+" "+((OS_Container)((FunctionDef)fd).getParent()).name());
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
					for (@NotNull IdentTableEntry ite : generatedFunction.idte_list) {
						ite.resolveExpectation = promiseExpectation(ite, "Element Resolved");
						resolve_ident_table_entry(ite, generatedFunction, context);
					}
					//
					// resolve arguments table
					//
					@NotNull Resolve_Variable_Table_Entry rvte = new Resolve_Variable_Table_Entry(generatedFunction, context, this);
					@NotNull IVariableConnector connector;
					if (generatedFunction instanceof GeneratedConstructor) {
						connector = new CtorConnector((GeneratedConstructor) generatedFunction);
					} else {
						connector = new NullConnector();
					}
					for (@NotNull VariableTableEntry vte : generatedFunction.vte_list) {
						rvte.action(vte, connector);
					}
				}
				break;
			case X:
				{
					//
					// resolve var table. moved from `E'
					//
					for (@NotNull VariableTableEntry vte : generatedFunction.vte_list) {
						resolve_var_table_entry(vte, generatedFunction, context);
					}
					for (@NotNull Runnable runnable : onRunnables) {
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
										@NotNull GenType genType = new GenType();
										genType.typeName = attached;
										genType.resolved = new OS_Type((ClassStatement) best);
//										genType.copy(vte.type.genType);
										genType.ci = genCI(genType, x);
										vte.type.genType.copy(genType);
										// set node when available
										((ClassInvocation) vte.type.genType.ci).resolvePromise().done(new DoneCallback<GeneratedClass>() {
											@Override
											public void onDone(GeneratedClass result) {
												vte.type.genType.node = result;
												vte.resolveTypeToClass(result);
												vte.genType = vte.type.genType; // TODO who knows if this is necessary?
											}
										});
									}
									//vte.el = best;
									// NOTE we called resolve_var_table_entry above
//									LOG.err("200 "+best);
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
					for (@NotNull IdentTableEntry ite : generatedFunction.idte_list) {
						int y=2;
						assign_type_to_idte(ite, generatedFunction, fd_ctx, context);
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
						@NotNull Dependencies deps = new Dependencies(/*phase, this, errSink*/);
						for (@NotNull GenType genType : generatedFunction.dependentTypes()) {
							deps.action_type(genType, workManager);
						}
						for (@NotNull FunctionInvocation dependentFunction : generatedFunction.dependentFunctions()) {
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
						@NotNull Lookup_function_on_exit lfoe = new Lookup_function_on_exit();
						for (@NotNull ProcTableEntry pte : generatedFunction.prte_list) {
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
							@NotNull IdentTableEntry idte = generatedFunction.getIdentTableEntry(to_int(i2));
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
							@NotNull IdentTableEntry idte2 = generatedFunction.getIdentTableEntry(to_int(i2));
							if (idte2.type == null) {
								idte2.type = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null, idte2.getIdent(), idte2);
							}
							LookupResultList lrl1 = fd_ctx.lookup(idte2.getIdent().getText());
							@Nullable OS_Element best1 = lrl1.chooseBest(null);
							if (best1 != null) {
								idte2.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best1));
								// TODO check for elements which may contain type information
								if (best1 instanceof VariableStatement) {
									final @NotNull VariableStatement vs = (VariableStatement) best1;
									@NotNull DeferredMember dm = deferred_member(vs.getParent().getParent(), null, vs, idte2);
									dm.typePromise().done(new DoneCallback<GenType>() {
											@Override
											public void onDone(@NotNull GenType result) {
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
//					LOG.info("298 Calling "+x);
					resolveIdentIA_(context, identIA, generatedFunction, new FoundElement(phase) {

						@SuppressWarnings("unused") final String xx = x;

						@Override
						public void foundElement(OS_Element e) {
							pte.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(e, identIA));
							if (fd instanceof DefFunctionDef) {
								final IInvocation invocation = getInvocation((GeneratedFunction) generatedFunction);
								forFunction(newFunctionInvocation((FunctionDef) e, pte, invocation, phase), new ForFunction() {
									@Override
									public void typeDecided(@NotNull GenType aType) {
										@Nullable InstructionArgument x = generatedFunction.vte_lookup("Result");
										assert x != null;
										((IntegerIA) x).getEntry().type.setAttached(gt(aType));
									}
								});
							}
						}

						@Override
						public void noFoundElement() {
//							errSink.reportError("370 Can't find callsite "+x);
							// TODO don't know if this is right
							@NotNull IdentTableEntry entry = identIA.getEntry();
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
				int potential_size = vte.potentialTypes().size();
				if (potential_size == 1)
					vte.type.setAttached(getPotentialTypesVte(vte).get(0).getAttached());
				else if (potential_size > 1) {
					// TODO Check type compatibility
//					LOG.err("703 "+vte.getName()+" "+vte.potentialTypes());
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
			for (GeneratedContainer.VarTableEntry gc_vte : vt) {
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

	private void add_proc_table_listeners(@NotNull BaseGeneratedFunction generatedFunction) {
		for (final @NotNull ProcTableEntry pte : generatedFunction.prte_list) {
			pte.addStatusListener(new ProcTableListener(pte, generatedFunction, new DeduceClient2(this)));

			InstructionArgument en = pte.expression_num;
			if (en != null) {
				if (en instanceof IdentIA) {
					final @NotNull IdentIA identIA = (IdentIA) en;
					@NotNull IdentTableEntry idte = identIA.getEntry();
					idte.addStatusListener(new BaseTableEntry.StatusListener() {
						@Override
						public void onChange(IElementHolder eh, BaseTableEntry.Status newStatus) {
							if (newStatus != BaseTableEntry.Status.KNOWN)
								return;

							final OS_Element el = eh.getElement();

							@NotNull ElObjectType type = DecideElObjectType.getElObjectType(el);

							switch (type) {
							case NAMESPACE:
								@NotNull GenType genType = new GenType((NamespaceStatement) el);
								generatedFunction.addDependentType(genType);
								break;
							case CLASS:
								@NotNull GenType genType2 = new GenType((ClassStatement) el);
								generatedFunction.addDependentType(genType2);
								break;
							case FUNCTION:
								@Nullable IdentIA identIA2 = null;
								if (pte.expression_num instanceof IdentIA)
									identIA2 = (IdentIA) pte.expression_num;
								if (identIA2 != null) {
									@NotNull IdentTableEntry idte2 = identIA.getEntry();
									@Nullable ProcTableEntry procTableEntry = idte2.getCallablePTE();
//									if (procTableEntry == pte) System.err.println("940 procTableEntry == pte");
									if (procTableEntry != null) {
										// TODO doesn't seem like we need this
										procTableEntry.onFunctionInvocation(new DoneCallback<FunctionInvocation>() {
											@Override
											public void onDone(@NotNull FunctionInvocation functionInvocation) {
												ClassInvocation ci = functionInvocation.getClassInvocation();
												NamespaceInvocation nsi = functionInvocation.getNamespaceInvocation();
												// do we register?? probably not
												if (ci == null && nsi == null)
													assert false;
												@NotNull FunctionInvocation fi = newFunctionInvocation((FunctionDef) el, pte, ci != null ? ci : nsi, phase);

												{
													if (functionInvocation.getClassInvocation() == fi.getClassInvocation() &&
														functionInvocation.getFunction() == fi.getFunction() &&
														functionInvocation.pte == fi.pte) {
//														System.err.println("955 It seems like we are generating the same thing...");
													} else {
														int ok=2;
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
//								LOG.err(String.format("228 Don't know what to do %s %s", type, el));
								break;
							}
						}
					});
				} else if (en instanceof IntegerIA) {
					final @NotNull IntegerIA integerIA = (IntegerIA) en;
					@NotNull VariableTableEntry vte = integerIA.getEntry();
					vte.addStatusListener(new BaseTableEntry.StatusListener() {
						@Override
						public void onChange(IElementHolder eh, BaseTableEntry.Status newStatus) {
							if (newStatus != BaseTableEntry.Status.KNOWN)
								return;

							@NotNull VariableTableEntry vte2 = vte;

							final OS_Element el = eh.getElement();

							@NotNull ElObjectType type = DecideElObjectType.getElObjectType(el);

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

	private @NotNull String getPTEString(@Nullable ProcTableEntry pte) {
		String pte_string;
		if (pte == null)
			pte_string = "[]";
		else {
			@NotNull List<String> l = new ArrayList<String>();

			for (@NotNull TypeTableEntry typeTableEntry : pte.getArgs()) {
				OS_Type attached = typeTableEntry.getAttached();

				if (attached != null)
					l.add(attached.toString());
				else {
//					LOG.err("267 attached == null for "+typeTableEntry);

					if (typeTableEntry.expression != null)
						l.add(String.format("<Unknown expression: %s>", typeTableEntry.expression));
					else
						l.add("<Unknkown>");
				}
			}

			@NotNull StringBuilder sb2 = new StringBuilder();
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
	private @Nullable ClassInvocation genCI(@NotNull TypeTableEntry aType) {
		GenType genType = aType.genType;
		if (genType.nonGenericTypeName != null) {
			@NotNull NormalTypeName aTyn1 = (NormalTypeName) genType.nonGenericTypeName;
			@Nullable String constructorName = null; // TODO this comes from nowhere
			ClassStatement best = genType.resolved.getClassOf();
			//
			@NotNull List<TypeName> gp = best.getGenericPart();
			@Nullable ClassInvocation clsinv = new ClassInvocation(best, constructorName);
			if (gp.size() > 0) {
				TypeNameList gp2 = aTyn1.getGenericPart();
				for (int i = 0; i < gp.size(); i++) {
					final TypeName typeName = gp2.get(i);
					@NotNull GenType genType1;
					try {
						genType1 = resolve_type(new OS_Type(typeName), typeName.getContext());
						clsinv.set(i, gp.get(i), genType1.resolved);
					} catch (ResolveError aResolveError) {
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
			ClassStatement best = genType.resolved.getClassOf();
			@Nullable String constructorName = null; // TODO what to do about this, nothing I guess

			@NotNull List<TypeName> gp = best.getGenericPart();
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

	@Nullable ClassInvocation genCI(@NotNull GenType genType, TypeName aGenericTypeName) {
		if (genType.nonGenericTypeName != null) {
			@NotNull NormalTypeName aTyn1 = (NormalTypeName) genType.nonGenericTypeName;
			@Nullable String constructorName = null; // TODO this comes from nowhere
			ClassStatement best = genType.resolved.getClassOf();
			//
			@NotNull List<TypeName> gp = best.getGenericPart();
			@Nullable ClassInvocation clsinv = new ClassInvocation(best, constructorName);
			if (gp.size() > 0) {
				TypeNameList gp2 = aTyn1.getGenericPart();
				for (int i = 0; i < gp.size(); i++) {
					final TypeName typeName = gp2.get(i);
					@NotNull GenType typeName2;
					try {
						typeName2 = resolve_type(new OS_Type(typeName), typeName.getContext());
						clsinv.set(i, gp.get(i), typeName2.resolved);
					} catch (ResolveError aResolveError) {
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
			ClassStatement best = genType.resolved.getClassOf();
			@Nullable String constructorName = null; // TODO what to do about this, nothing I guess

			@NotNull List<TypeName> gp = best.getGenericPart();
			@Nullable ClassInvocation clsinv;
			if (genType.ci == null) {
				clsinv = new ClassInvocation(best, constructorName);
				if (gp.size() > 0) {
					if (aGenericTypeName instanceof NormalTypeName) {
						final @NotNull NormalTypeName tn = (NormalTypeName) aGenericTypeName;
						TypeNameList tngp = tn.getGenericPart();
						for (int i = 0; i < gp.size(); i++) {
							final TypeName typeName = tngp.get(i);
							@NotNull GenType typeName2;
							try {
								typeName2 = resolve_type(new OS_Type(typeName), typeName.getContext());
								clsinv.set(i, gp.get(i), typeName2.resolved);
							} catch (ResolveError aResolveError) {
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

	@NotNull FunctionInvocation newFunctionInvocation(BaseFunctionDef aFunctionDef, ProcTableEntry aPte, @NotNull IInvocation aInvocation, @NotNull DeducePhase aDeducePhase) {
		@NotNull FunctionInvocation fi = new FunctionInvocation(aFunctionDef, aPte, aInvocation, aDeducePhase.generatePhase);
		// TODO register here
		return fi;
	}

	public String getFileName() {
		return module.getFileName();
	}

	public @NotNull GenerateFunctions getGenerateFunctions(@NotNull OS_Module aModule) {
		return phase.generatePhase.getGenerateFunctions(aModule);
	}

	class Resolve_each_typename {

		private final DeducePhase phase;
		private final DeduceTypes2 dt2;
		private final ErrSink errSink;

		public Resolve_each_typename(DeducePhase aPhase, DeduceTypes2 aDeduceTypes2, ErrSink aErrSink) {
			phase = aPhase;
			dt2 = aDeduceTypes2;
			errSink = aErrSink;
		}

		public void action(@NotNull TypeTableEntry typeTableEntry) {
			@Nullable OS_Type attached = typeTableEntry.getAttached();
			if (attached == null) return;
			if (attached.getType() == OS_Type.Type.USER) {
				action_USER(typeTableEntry, attached);
			} else if (attached.getType() == OS_Type.Type.USER_CLASS) {
				action_USER_CLASS(typeTableEntry, attached);
			}
		}

		public void action_USER_CLASS(@NotNull TypeTableEntry typeTableEntry, @NotNull OS_Type aAttached) {
			ClassStatement c = aAttached.getClassOf();
			assert c != null;
			phase.onClass(c, new OnClass() {
				// TODO what about ClassInvocation's?
				@Override
				public void classFound(GeneratedClass cc) {
					typeTableEntry.resolve(cc); // set genType.node
				}
			});
		}

		public void action_USER(@NotNull TypeTableEntry typeTableEntry, @Nullable OS_Type aAttached) {
			TypeName tn = aAttached.getTypeName();
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
			} catch (ResolveError aResolveError) {
				LOG.err("288 Failed to resolve type "+ aAttached);
				errSink.reportDiagnostic(aResolveError);
			}
		}
	}

	class Dependencies {
		final WorkList wl = new WorkList();

		public void action_type(@NotNull GenType genType, @NotNull WorkManager aWorkManager) {
			// TODO work this out further
			if (genType.resolvedn != null) {
				@NotNull OS_Module mod = genType.resolvedn.getContext().module();
				final @NotNull GenerateFunctions gf = phase.generatePhase.getGenerateFunctions(mod);
				NamespaceInvocation ni = phase.registerNamespaceInvocation(genType.resolvedn);
				@NotNull WlGenerateNamespace gen = new WlGenerateNamespace(gf, ni, phase.generatedClasses);
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
				@Nullable WlGenerateClass gen = new WlGenerateClass(gf, ci, phase.generatedClasses);
				wl.addJob(gen);
			}
			//
			aWorkManager.addJobs(wl);
		}

		public void action_function(@NotNull FunctionInvocation aDependentFunction, @NotNull WorkManager aWorkManager) {
			final BaseFunctionDef function = aDependentFunction.getFunction();
			WorkJob gen;
			final @NotNull OS_Module mod;
			if (function == ConstructorDef.defaultVirtualCtor) {
				ClassInvocation ci = aDependentFunction.getClassInvocation();
				if (ci == null) {
					NamespaceInvocation ni = aDependentFunction.getNamespaceInvocation();
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

	private void resolve_cte_expression(@NotNull ConstantTableEntry cte, Context aContext) {
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

	private void resolve_cte_expression_builtin(@NotNull ConstantTableEntry cte, Context aContext, BuiltInTypes aBuiltInType) {
		final OS_Type a = cte.getTypeTableEntry().getAttached();
		if (a == null || a.getType() != OS_Type.Type.USER_CLASS) {
			try {
				cte.getTypeTableEntry().setAttached(resolve_type(new OS_Type(aBuiltInType), aContext));
			} catch (ResolveError resolveError) {
				System.out.println("117 Can't be here");
//				resolveError.printStackTrace(); // TODO print diagnostic
			}
		}
	}

	class Lookup_function_on_exit {
		@NotNull WorkList wl = new WorkList();

		public void action(@NotNull ProcTableEntry pte) {
			FunctionInvocation fi = pte.getFunctionInvocation();
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

				Collection<ConstructorDef> cis = klass.getConstructors();
				for (@NotNull ConstructorDef constructorDef : cis) {
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
							Map<TypeName, OS_Type> gp = classInvocation.genericPart;
							if (gp != null) {
								int i = 0;
								for (Map.@NotNull Entry<TypeName, OS_Type> entry : gp.entrySet()) {
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
						Map<TypeName, OS_Type> gp = classInvocation.genericPart;
						int i = 0;
						for (Map.@NotNull Entry<TypeName, OS_Type> entry : gp.entrySet()) {
							ci.set(i, entry.getKey(), entry.getValue());
							i++;
						}
					}
				}
				proceed(fi, ci, (ClassStatement) parent, wl);
			}

//			proceed(fi, ci, parent);
		}

		void proceed(@NotNull FunctionInvocation fi, ClassInvocation ci, ClassStatement aParent, @NotNull WorkList wl) {
			ci = phase.registerClassInvocation(ci);

			ClassStatement kl = ci.getKlass(); // TODO Don't you see aParent??
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

		void proceed(@NotNull FunctionInvocation fi, @NotNull NamespaceStatement aParent, @NotNull WorkList wl) {
//			ci = phase.registerClassInvocation(ci);

			final @NotNull OS_Module module1 = aParent.getContext().module();

			final NamespaceInvocation nsi = phase.registerNamespaceInvocation(aParent);

			wl.addJob(new WlGenerateNamespace(phase.generatePhase.getGenerateFunctions(module1), nsi, phase.generatedClasses));
			wl.addJob(new WlGenerateFunction(phase.generatePhase.getGenerateFunctions(module1), fi));

			wm.addJobs(wl);
		}
	}

	public void assign_type_to_idte(@NotNull IdentTableEntry ite,
									@NotNull BaseGeneratedFunction generatedFunction,
									Context aFunctionContext,
									@NotNull Context aContext) {
		if (!ite.hasResolvedElement()) {
			@NotNull IdentIA ident_a = new IdentIA(ite.getIndex(), generatedFunction);
			resolveIdentIA_(aContext, ident_a, generatedFunction, new FoundElement(phase) {

				final String path = generatedFunction.getIdentIAPathNormal(ident_a);

				@Override
				public void foundElement(OS_Element x) {
					if (ite.getResolvedElement() != x)
						ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
					if (ite.type != null && ite.type.getAttached() != null) {
						switch (ite.type.getAttached().getType()) {
						case USER:
							try {
								@NotNull GenType xx = resolve_type(ite.type.getAttached(), aFunctionContext);
								ite.type.setAttached(xx);
							} catch (ResolveError resolveError) {
								LOG.info("192 Can't attach type to " + path);
								errSink.reportDiagnostic(resolveError);
							}
							if (ite.type.getAttached().getType() == OS_Type.Type.USER_CLASS) {
								use_user_class(ite.type.getAttached(), ite);
							}
							break;
						case USER_CLASS:
							use_user_class(ite.type.getAttached(), ite);
							break;
						case FUNCTION:
							{
								// TODO All this for nothing
								//  the ite points to a function, not a function call,
								//  so there is no point in resolving it
								if (ite.type.tableEntry instanceof ProcTableEntry) {
									final @NotNull ProcTableEntry pte = (ProcTableEntry) ite.type.tableEntry;

								} else if (ite.type.tableEntry instanceof IdentTableEntry) {
									final @NotNull IdentTableEntry identTableEntry = (IdentTableEntry) ite.type.tableEntry;
									if (identTableEntry.getCallablePTE() != null) {
										@Nullable ProcTableEntry cpte = identTableEntry.getCallablePTE();
										cpte.typePromise().then(new DoneCallback<GenType>() {
											@Override
											public void onDone(@NotNull GenType result) {
												System.out.println("1483 "+result.resolved+" "+result.node);
											}
										});
									}
								}
							}
							break;
						default:
							throw new IllegalStateException("Unexpected value: " + ite.type.getAttached().getType());
						}
					} else {
						int yy=2;
						if (!ite.hasResolvedElement()) {
							@Nullable LookupResultList lrl = null;
							try {
								lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), aFunctionContext, DeduceTypes2.this);
								@Nullable OS_Element best = lrl.chooseBest(null);
								if (best != null) {
									ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
									if (ite.type != null && ite.type.getAttached() != null) {
										if (ite.type.getAttached().getType() == OS_Type.Type.USER) {
											try {
												@NotNull GenType xx = resolve_type(ite.type.getAttached(), aFunctionContext);
												ite.type.setAttached(xx);
											} catch (ResolveError resolveError) { // TODO double catch
												LOG.info("210 Can't attach type to "+ite.getIdent());
												errSink.reportDiagnostic(resolveError);
//												continue;
											}
										}
									}
								} else {
									LOG.err("184 Couldn't resolve "+ite.getIdent());
								}
							} catch (ResolveError aResolveError) {
								LOG.err("184-506 Couldn't resolve "+ite.getIdent());
								aResolveError.printStackTrace();
							}
							if (ite.type.getAttached().getType() == OS_Type.Type.USER_CLASS) {
								use_user_class(ite.type.getAttached(), ite);
							}
						}
					}
				}

				private void use_user_class(@NotNull OS_Type aType, @NotNull IdentTableEntry aEntry) {
					final ClassStatement cs = aType.getClassOf();
					if (aEntry.constructable_pte != null) {
						int yyy=3;
						System.out.println("use_user_class: "+cs);
					}
				}

				@Override
				public void noFoundElement() {
					ite.setStatus(BaseTableEntry.Status.UNKNOWN, null);
					errSink.reportError("165 Can't resolve "+path);
				}
			});
		}
	}

	public void resolve_ident_table_entry(@NotNull IdentTableEntry ite, @NotNull BaseGeneratedFunction generatedFunction, Context ctx) {
		new Resolve_ident_table_entry(this).act(ite, generatedFunction, ctx);
	}

	public void resolve_var_table_entry(@NotNull VariableTableEntry vte, BaseGeneratedFunction generatedFunction, Context ctx) {
		new Resolve_var_table_entry(this).act(vte, generatedFunction, ctx);
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

	void implement_construct(BaseGeneratedFunction generatedFunction, Instruction instruction) {
		final @NotNull Implement_construct ic = newImplement_construct(generatedFunction, instruction);
		ic.action();
	}

	@NotNull
	public Implement_construct newImplement_construct(BaseGeneratedFunction generatedFunction, Instruction instruction) {
		return new Implement_construct(this, generatedFunction, instruction);
	}

	void resolve_function_return_type(@NotNull BaseGeneratedFunction generatedFunction) {
		// MODERNIZATION Does this have any affinity with DeferredMember?
		@Nullable final InstructionArgument vte_index = generatedFunction.vte_lookup("Result");
		if (vte_index != null) {
			final @NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(to_int(vte_index));

			if (vte.type.getAttached() != null) {
				phase.typeDecided((GeneratedFunction) generatedFunction, vte.type.genType);
			} else {
				@NotNull Collection<TypeTableEntry> pot1 = vte.potentialTypes();
				@NotNull ArrayList<TypeTableEntry> pot = new ArrayList<TypeTableEntry>(pot1);
				if (pot.size() == 1) {
					phase.typeDecided((GeneratedFunction) generatedFunction, pot.get(0).genType);
				} else if (pot.size() == 0) {
					@NotNull GenType unitType = new GenType();
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
				@NotNull GenType unitType = new GenType();
				unitType.typeName = new OS_Type(BuiltInTypes.Unit);
				phase.typeDecided((GeneratedFunction) generatedFunction, unitType);
			}
		}
	}

	static class DeduceClient1 {
		private final DeduceTypes2 dt2;

		@Contract(pure = true)
		public DeduceClient1(DeduceTypes2 aDeduceTypes2) {
			dt2 = aDeduceTypes2;
		}

		public @Nullable OS_Element _resolveAlias(@NotNull AliasStatement aAliasStatement) {
			return DeduceLookupUtils._resolveAlias(aAliasStatement, dt2);
		}

		public void found_element_for_ite(BaseGeneratedFunction aGeneratedFunction, @NotNull IdentTableEntry aIte, OS_Element aX, Context aCtx) {
			dt2.found_element_for_ite(aGeneratedFunction, aIte, aX, aCtx);
		}

		public @NotNull GenType resolve_type(@NotNull OS_Type aType, Context aCtx) throws ResolveError {
			return dt2.resolve_type(aType, aCtx);
		}

		public @Nullable IInvocation getInvocationFromBacklink(InstructionArgument aInstructionArgument) {
			return dt2.getInvocationFromBacklink(aInstructionArgument);
		}

		public @NotNull DeferredMember deferred_member(OS_Element aParent, IInvocation aInvocation, VariableStatement aVariableStatement, @NotNull IdentTableEntry aIdentTableEntry) {
			return dt2.deferred_member(aParent, aInvocation, aVariableStatement, aIdentTableEntry);
		}

		public void genCI(final GenType aResult, final TypeName aNonGenericTypeName) {
			dt2.genCI(aResult, aNonGenericTypeName);
		}

		public @Nullable ClassInvocation registerClassInvocation(final ClassStatement aClassStatement, final String aS) {
			return dt2.phase.registerClassInvocation(aClassStatement, aS);
		}
	}

	void found_element_for_ite(BaseGeneratedFunction generatedFunction, @NotNull IdentTableEntry ite, @Nullable OS_Element y, Context ctx) {
		assert y == ite.getResolvedElement();

		@NotNull Found_Element_For_ITE fefi = new Found_Element_For_ITE(generatedFunction, ctx, LOG, errSink, new DeduceClient1(this));
		fefi.action(ite);
	}

	private @Nullable IInvocation getInvocationFromBacklink(@Nullable InstructionArgument aBacklink) {
		if (aBacklink == null) return null;
		// TODO implement me
		return null;
	}

	private @NotNull DeferredMember deferred_member(OS_Element aParent, IInvocation aInvocation, VariableStatement aVariableStatement, @NotNull IdentTableEntry ite) {
		@NotNull DeferredMember dm = deferred_member(aParent, aInvocation, aVariableStatement);
		dm.externalRef().then(new DoneCallback<GeneratedNode>() {
			@Override
			public void onDone(GeneratedNode result) {
				ite.externalRef = result;
			}
		});
		return dm;
	}

	private @Nullable DeferredMember deferred_member(OS_Element aParent, @Nullable IInvocation aInvocation, VariableStatement aVariableStatement) {
		if (aInvocation == null) {
			if (aParent instanceof NamespaceStatement)
				aInvocation = phase.registerNamespaceInvocation((NamespaceStatement) aParent);
		}
		@Nullable DeferredMember dm = new DeferredMember(aParent, aInvocation, aVariableStatement);
		phase.addDeferredMember(dm);
		return dm;
	}

	@NotNull GenType resolve_type(final @NotNull OS_Type type, final Context ctx) throws ResolveError {
		return resolve_type(module, type, ctx);
	}

	/*static*/ @NotNull GenType resolve_type(final OS_Module module, final @NotNull OS_Type type, final Context ctx) throws ResolveError {
		@NotNull GenType R = new GenType();
		R.typeName = type;

		switch (type.getType()) {

		case BUILT_IN:
			{
				switch (type.getBType()) {
				case SystemInteger:
					{
						@NotNull String typeName = type.getBType().name();
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
						@NotNull String typeName = type.getBType().name();
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
						@NotNull String typeName = type.getBType().name();
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
//						LOG.info("799 [resolving USER type named] " + tn);
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
		new Do_assign_call(this).do_assign_call(generatedFunction, ctx, vte, fca, instruction);
	}

	@NotNull PromiseExpectations expectations = new PromiseExpectations();

	class PromiseExpectations {
		long counter = 0;

		@NotNull List<PromiseExpectation> exp = new ArrayList<>();

		public void add(@NotNull PromiseExpectation aExpectation) {
			counter++;
			aExpectation.setCounter(counter);
			exp.add(aExpectation);
		}

		public void check() {
			for (@NotNull PromiseExpectation promiseExpectation : exp) {
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

		public PromiseExpectation(ExpectationBase aBase, String aDesc) {
			base = aBase;
			desc = aDesc;
		}

		public void satisfy(B aResult) {
			result = aResult;
			satisfied = true;
//			LOG.info(String.format("Expectation (%s, %d) met: %s %s", DeduceTypes2.this, counter, desc, base.expectationString()));
		}

		public void fail() {
			if (!_printed) {
//				LOG.err(String.format("Expectation (%s, %d) not met", DeduceTypes2.this, counter));
				_printed = true;
			}
		}

		public boolean isSatisfied() {
			return satisfied;
		}

		public void setCounter(long aCounter) {
			counter = aCounter;

//			LOG.info(String.format("Expectation (%s, %d) set: %s %s", DeduceTypes2.this, counter, desc, base.expectationString()));
		}
	}

	public <B> @NotNull PromiseExpectation<B> promiseExpectation(ExpectationBase base, String desc) {
		final @NotNull PromiseExpectation<B> promiseExpectation = new PromiseExpectation<>(base, desc);
		expectations.add(promiseExpectation);
		return promiseExpectation;
	}

	public IInvocation getInvocation(@NotNull GeneratedFunction generatedFunction) {
		final ClassInvocation classInvocation = generatedFunction.fi.getClassInvocation();
		final NamespaceInvocation ni;
		if (classInvocation == null) {
			ni = generatedFunction.fi.getNamespaceInvocation();
			return ni;
		} else
			return classInvocation;
	}

	void do_assign_call_args_ident(@NotNull BaseGeneratedFunction generatedFunction,
	                               @NotNull Context ctx,
	                               @NotNull VariableTableEntry vte,
	                               int aInstructionIndex,
	                               @NotNull ProcTableEntry aPte,
	                               int aI,
	                               @NotNull TypeTableEntry aTte,
	                               @NotNull IdentExpression aExpression) {
		final String e_text = aExpression.getText();
		final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(e_text);
//		LOG.info("10000 "+vte_ia);
		if (vte_ia != null) {
			final @NotNull VariableTableEntry vte1 = generatedFunction.getVarTableEntry(to_int(vte_ia));
			final Promise<GenType, Void, Void> p = vte1.typePromise();
			p.done(new DoneCallback<GenType>() {
				@Override
				public void onDone(GenType result) {
//					assert vte != vte1;
//					aTte.setAttached(result.resolved != null ? result.resolved : result.typeName);
					aTte.genType.copy(result);
//					vte.addPotentialType(aInstructionIndex, result); // TODO!!
				}
			});
			@NotNull Runnable runnable = new Runnable() {
				@Override
				public void run() {
					final @NotNull List<TypeTableEntry> ll = getPotentialTypesVte((GeneratedFunction) generatedFunction, vte_ia);
					doLogic(ll);
				}

				public void doLogic(@NotNull List<TypeTableEntry> potentialTypes) {
					assert potentialTypes.size() >= 0;
					switch (potentialTypes.size()) {
						case 1:
//							tte.attached = ll.get(0).attached;
//							vte.addPotentialType(instructionIndex, ll.get(0));
							if (p.isResolved()) {
//								LOG.info(String.format("1047 (vte already resolved) %s vte1.type = %s, gf = %s, tte1 = %s %n", vte1.getName(), vte1.type, generatedFunction, potentialTypes.get(0)));
							} else {
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
//								System.out.printf("890-1 Already resolved type: vte1.type = %s, gf = %s %n", vte1.type, generatedFunction);
								break;
							}
							LookupResultList lrl = ctx.lookup(e_text);
							@Nullable OS_Element best = lrl.chooseBest(null);
							if (best instanceof FormalArgListItem) {
								@NotNull final FormalArgListItem fali = (FormalArgListItem) best;
								final @NotNull OS_Type osType = new OS_Type(fali.typeName());
								if (!osType.equals(vte.type.getAttached())) {
									@NotNull TypeTableEntry tte1 = generatedFunction.newTypeTableEntry(
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
								@Nullable InstructionArgument vte2_ia = generatedFunction.vte_lookup(vs.getName());
								@NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(vte2_ia));
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
								int y = 2;
								LOG.err("543 " + best.getClass().getName());
								throw new NotImplementedException();
							}
							break;
						default:
							// TODO hopefully this works
							final @NotNull ArrayList<TypeTableEntry> potentialTypes1 = new ArrayList<TypeTableEntry>(
									Collections2.filter(potentialTypes, new Predicate<TypeTableEntry>() {
										@Override
										public boolean apply(@org.checkerframework.checker.nullness.qual.Nullable TypeTableEntry input) {
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
			int ia = generatedFunction.addIdentTableEntry(aExpression, ctx);
			@NotNull IdentTableEntry idte = generatedFunction.getIdentTableEntry(ia);
			idte.addPotentialType(aInstructionIndex, aTte); // TODO DotExpression??
			final int ii = aI;
			idte.onType(phase, new OnType() {
				@Override
				public void typeDeduced(@NotNull OS_Type aType) {
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

	void do_assign_call_GET_ITEM(@NotNull GetItemExpression gie, TypeTableEntry tte, @NotNull BaseGeneratedFunction generatedFunction, Context ctx) {
		try {
			final LookupResultList lrl = DeduceLookupUtils.lookupExpression(gie.getLeft(), ctx, this);
			final @Nullable OS_Element best = lrl.chooseBest(null);
			if (best != null) {
				if (best instanceof VariableStatement) { // TODO what about alias?
					@NotNull VariableStatement vs = (VariableStatement) best;
					String s = vs.getName();
					@Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(s);
					if (vte_ia != null) {
						@NotNull VariableTableEntry vte1 = generatedFunction.getVarTableEntry(to_int(vte_ia));
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
								} catch (ResolveError resolveError) {
	//								resolveError.printStackTrace();
									errSink.reportError("Cant resolve " + ty); // TODO print better diagnostic
									return;
								}
								if (rtype.resolved != null && rtype.resolved.getType() == OS_Type.Type.USER_CLASS) {
									LookupResultList lrl2 = rtype.resolved.getClassOf().getContext().lookup("__getitem__");
									@Nullable OS_Element best2 = lrl2.chooseBest(null);
									if (best2 != null) {
										if (best2 instanceof FunctionDef) {
											@NotNull FunctionDef fd = (FunctionDef) best2;
											@Nullable ProcTableEntry pte = null;
											final IInvocation invocation = getInvocation((GeneratedFunction) generatedFunction);
											forFunction(newFunctionInvocation(fd, pte, invocation, phase), new ForFunction() {
												@Override
												public void typeDecided(final @NotNull GenType aType) {
													assert fd == generatedFunction.getFD();
													//
													if (idte.type == null) {
														@NotNull TypeTableEntry tte1 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, gt(aType), idte); // TODO expression?
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
							@NotNull TypeTableEntry tte3 = generatedFunction.newTypeTableEntry(
									TypeTableEntry.Type.SPECIFIED, new OS_Type(vs.typeName()), vs.getNameToken());
							idte.type = tte3;
							ty = idte.type.getAttached();
						}
					}

	//				tte.attached = new OS_FuncType((FunctionDef) best); // TODO: what is this??
					//vte.addPotentialType(instructionIndex, tte);
				} else if (best instanceof FormalArgListItem) {
					final @Nullable FormalArgListItem fali = (FormalArgListItem) best;
					String s = fali.name();
					@Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(s);
					if (vte_ia != null) {
						@NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(vte_ia));

	//					final @Nullable OS_Type ty2 = vte2.type.attached;
						vte2.typePromise().done(new DoneCallback<GenType>() {
							@Override
							public void onDone(@NotNull GenType result) {
	//							assert false; // TODO this code is never reached
								final @Nullable OS_Type ty2 = result.typeName/*.getAttached()*/;
								assert ty2 != null;
								@NotNull GenType rtype = null;
								try {
									rtype = resolve_type(ty2, ctx);
								} catch (ResolveError resolveError) {
	//								resolveError.printStackTrace();
									errSink.reportError("Cant resolve " + ty2); // TODO print better diagnostic
									return;
								}
								if (rtype.resolved != null && rtype.resolved.getType() == OS_Type.Type.USER_CLASS) {
									LookupResultList lrl2 = rtype.resolved.getClassOf().getContext().lookup("__getitem__");
									@Nullable OS_Element best2 = lrl2.chooseBest(null);
									if (best2 != null) {
										if (best2 instanceof FunctionDef) {
											@Nullable FunctionDef fd = (FunctionDef) best2;
											@Nullable ProcTableEntry pte = null;
											final IInvocation invocation = getInvocation((GeneratedFunction) generatedFunction);
											forFunction(newFunctionInvocation(fd, pte, invocation, phase), new ForFunction() {
												@Override
												public void typeDecided(final @NotNull GenType aType) {
													assert fd == generatedFunction.getFD();
													//
													@NotNull TypeTableEntry tte1 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, gt(aType), vte2); // TODO expression?
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
		} catch (ResolveError aResolveError) {
			aResolveError.printStackTrace();
			int y=2;
			throw new NotImplementedException();
		}
	}

/*
	private void forFunction(GeneratedFunction gf, ForFunction forFunction) {
		phase.forFunction(this, gf, forFunction);
	}
*/

	void forFunction(@NotNull FunctionInvocation gf, @NotNull ForFunction forFunction) {
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

	void implement_calls(final @NotNull BaseGeneratedFunction gf, final @NotNull Context context, final InstructionArgument i2, final @NotNull ProcTableEntry fn1, final int pc) {
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

		private boolean action_dunder(String pn) {
			assert Pattern.matches("__[a-z]+__", pn);
//			LOG.info(String.format("i2 is not IntegerIA (%s)",i2.getClass().getName()));
			//
			// try to get dunder method from class
			//
			IExpression exp = pte.getArgs().get(0).expression;
			if (exp instanceof IdentExpression) {
				return action_dunder_doIt(pn, (IdentExpression) exp);
			}
			return false;
		}

		private boolean action_dunder_doIt(String pn, IdentExpression exp) {
			final @NotNull IdentExpression identExpression = exp;
			@Nullable InstructionArgument vte_ia = gf.vte_lookup(identExpression.getText());
			if (vte_ia != null) {
				((IntegerIA) vte_ia).getEntry().typePromise().then(new DoneCallback<GenType>() {
					@Override
					public void onDone(@NotNull GenType result) {
						boolean found1 = lookup_name_calls(result.resolved.getClassOf().getContext(), pn, pte);
						if (found1) {
							int y=2;
							System.out.println("3071 "+pte.getStatus());
							IInvocation invocation = result.ci;
//							final BaseFunctionDef fd = gf.getFD();
							final BaseFunctionDef fd = pte.getFunctionInvocation().getFunction();
							@NotNull FunctionInvocation fi = newFunctionInvocation(fd, pte, invocation, phase);
							pte.setFunctionInvocation(fi);
						} else {
							int y=3;
							System.out.println("3074");
						}
					}
				});
				return true;
			}
			return false;
		}

		private boolean action_i2_IntegerIA(String pn, @Nullable String pn2) {
			boolean found;
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

		private boolean action_i2_IntegerIA_vteName_is_not_null(String pn, @Nullable String pn2, @NotNull VariableTableEntry vte) {
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
					@NotNull GenType ty2 = resolve_type(x, ctx2);
					pot_types_size_is_1_USER_CLASS(pn, pn2, ty2.resolved);
					return true;
				} catch (ResolveError resolveError) {
					resolveError.printStackTrace();
					errSink.reportDiagnostic(resolveError);
					return false;
				}
			default:
				assert false;
				return false;
			}
		}

		private void pot_types_size_is_1_USER_CLASS(String pn, @Nullable String pn2, OS_Type x) {
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

		private boolean action_i2_IntegerIA_vteName_is_null(String pn, @Nullable String pn2, Context ctx, String vteName) {
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
		Implement_Calls_ ic = new Implement_Calls_(gf, context, i2, pte, pc);
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

	public void resolveIdentIA2_(@NotNull Context context, @NotNull IdentIA identIA, @NotNull GeneratedFunction generatedFunction, @NotNull FoundElement foundElement) {
		final @NotNull List<InstructionArgument> s = generatedFunction._getIdentIAPathList(identIA);
		resolveIdentIA2_(context, identIA, s, generatedFunction, foundElement);
	}

	public void resolveIdentIA_(@NotNull Context context, @NotNull IdentIA identIA, BaseGeneratedFunction generatedFunction, @NotNull FoundElement foundElement) {
		@NotNull Resolve_Ident_IA ria = new Resolve_Ident_IA(this, phase, context, identIA, generatedFunction, foundElement, errSink);
		ria.action();
	}

	public static class Holder<T> {
		private T el;

		public void set(T el) {
			this.el = el;
		}

		public T get() {
			return el;
		}
	}


	public void resolveIdentIA2_(@NotNull final Context ctx,
								 @Nullable IdentIA identIA,
								 @Nullable List<InstructionArgument> s,
								 @NotNull final BaseGeneratedFunction generatedFunction,
								 @NotNull final FoundElement foundElement) {
		@NotNull Resolve_Ident_IA2 ria2 = new Resolve_Ident_IA2(this, errSink, phase, generatedFunction, foundElement);
		ria2.resolveIdentIA2_(ctx, identIA, s);
	}

	OS_Type gt(@NotNull GenType aType) {
		return aType.resolved != null ? aType.resolved : aType.typeName;
	}

	@NotNull ArrayList<TypeTableEntry> getPotentialTypesVte(@NotNull VariableTableEntry vte) {
		return new ArrayList<TypeTableEntry>(vte.potentialTypes());
	}

	@NotNull
	private ArrayList<TypeTableEntry> getPotentialTypesVte(@NotNull GeneratedFunction generatedFunction, @NotNull InstructionArgument vte_index) {
		return getPotentialTypesVte(generatedFunction.getVarTableEntry(to_int(vte_index)));
	}

	public void register_and_resolve(@NotNull VariableTableEntry aVte, @NotNull ClassStatement aKlass) {
		@Nullable ClassInvocation ci = new ClassInvocation(aKlass, null);
		ci = phase.registerClassInvocation(ci);
		ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
			@Override
			public void onDone(GeneratedClass result) {
				aVte.resolveTypeToClass(result);
			}
		});
	}

	static class DeduceClient2 {
		private DeduceTypes2 deduceTypes2;

		public DeduceClient2(DeduceTypes2 deduceTypes2) {
			this.deduceTypes2 = deduceTypes2;
		}

		public @Nullable ClassInvocation registerClassInvocation(@NotNull ClassInvocation ci) {
			return deduceTypes2.phase.registerClassInvocation(ci);
		}

		public @NotNull FunctionInvocation newFunctionInvocation(BaseFunctionDef constructorDef, ProcTableEntry pte, @NotNull IInvocation ci) {
			return deduceTypes2.newFunctionInvocation(constructorDef, pte, ci, deduceTypes2.phase);
		}

		public NamespaceInvocation registerNamespaceInvocation(NamespaceStatement namespaceStatement) {
			return deduceTypes2.phase.registerNamespaceInvocation(namespaceStatement);
		}

		public @Nullable ClassInvocation genCI(@NotNull GenType genType, TypeName typeName) {
			return deduceTypes2.genCI(genType, typeName);
		}

		public @NotNull ElLog getLOG() {
			return deduceTypes2.LOG;
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
