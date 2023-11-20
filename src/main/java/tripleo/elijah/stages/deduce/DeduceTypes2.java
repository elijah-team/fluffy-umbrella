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

import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.contexts.ClassContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_AnyType;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_FuncExprType;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.lang.types.OS_GenericTypeNameType;
import tripleo.elijah.lang.types.OS_UserClassType;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.stages.deduce.declarations.DeferredMember;
import tripleo.elijah.stages.deduce.declarations.DeferredMemberFunction;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_IdentTableEntry;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_ProcTableEntry;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_VariableTableEntry;
import tripleo.elijah.stages.deduce.zero.IZero;
import tripleo.elijah.stages.deduce.zero.Zero_FuncExprType;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.ConstTableIA;
import tripleo.elijah.stages.instructions.FnCallArgs;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.InstructionName;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.Label;
import tripleo.elijah.stages.instructions.LabelIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkJob;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created 9/15/20 12:51 PM
 */
public class DeduceTypes2 {
	private static final   String             PHASE  = "DeduceTypes2";
	final @NotNull         DeducePhase        phase;
	final                  ErrSink            errSink;
	final @NotNull         ElLog              LOG;
	private final @NotNull OS_Module          module;
	private final          Map<Object, IZero> _zeros = new HashMap<>();

	public DeduceTypes2(@NotNull final OS_Module module, @NotNull final DeducePhase phase) {
		this(module, phase, ElLog.Verbosity.VERBOSE);
	}

	@NotNull WorkManager wm = new WorkManager();
	final    List<FunctionInvocation> functionInvocations = new ArrayList<>();

	public void deduceFunctions(final @NotNull Iterable<GeneratedNode> lgf) {
		for (final GeneratedNode generatedNode : lgf) {
			if (generatedNode instanceof @NotNull final GeneratedFunction generatedFunction) {
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
			size             = df_helper(generatedClasses, new dfhi_functions());
			generatedClasses = phase.generatedClasses.copy();
		} while (size > 0);
		do {
			size             = df_helper(generatedClasses, new dfhi_constructors());
			generatedClasses = phase.generatedClasses.copy();
		} while (size > 0);
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

	public boolean deduceOneFunction(@NotNull final GeneratedFunction aGeneratedFunction, @NotNull final DeducePhase aDeducePhase) {
		if (aGeneratedFunction.deducedAlready) return false;
		deduce_generated_function(aGeneratedFunction);
		aGeneratedFunction.deducedAlready = true;
		for (@NotNull final IdentTableEntry identTableEntry : aGeneratedFunction.idte_list) {
			if (identTableEntry.getResolvedElement() instanceof final @NotNull VariableStatement vs) {
				final @Nullable OS_Element       el  = vs.getParent().getParent();
				final OS_Element                 el2 = aGeneratedFunction.getFD().getParent();
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

	public DeducePhase _phase() {
		return phase;
	}

	public ErrSink _errSink() {
		return errSink;
	}

	@NotNull List<IStateRunnable> onRunnables = new ArrayList<>();

	/**
	 * Deduce functions or constructors contained in classes list
	 *
	 * @param aGeneratedClasses assumed to be a list of {@link GeneratedContainerNC}
	 * @param dfhi              specifies what to select for:<br>
	 *                          {@link dfhi_functions} will select all functions from {@code functionMap}, and <br>
	 *                          {@link dfhi_constructors} will select all constructors from {@code constructors}.
	 * @param <T>               generic parameter taken from {@code dfhi}
	 * @return the number of deduced functions or constructors, or 0
	 */
	<T> int df_helper(@NotNull final List<GeneratedNode> aGeneratedClasses, @NotNull final df_helper_i<T> dfhi) {
		int size = 0;
		for (final GeneratedNode generatedNode : aGeneratedClasses) {
			@NotNull final GeneratedContainerNC generatedContainerNC = (GeneratedContainerNC) generatedNode;
			final @Nullable df_helper<T>        dfh                  = dfhi.get(generatedContainerNC);
			if (dfh == null) continue;
			@NotNull final Collection<T> lgf2 = dfh.collection();
			for (final T generatedConstructor : lgf2) {
				if (dfh.deduce(generatedConstructor))
					size++;
			}
		}
		return size;
	}

	@NotNull PromiseExpectations expectations = new PromiseExpectations();

	public DeduceTypes2(@NotNull final OS_Module module, @NotNull final DeducePhase phase, final ElLog.Verbosity verbosity) {
		this.module  = module;
		this.phase   = phase;
		this.errSink = module.getCompilation().getErrSink();
		this.LOG     = new ElLog(module.getFileName(), verbosity, PHASE);
		//
		phase.addLog(LOG);
		//
		DeduceElement3_VariableTableEntry.ST.register(phase);
	}

	void onFinish(final Runnable r) {
		onRunnables.add(new StatefulRunnable(r));
	}

	public void deduce_generated_function(final @NotNull GeneratedFunction generatedFunction) {
		final @NotNull FunctionDef fd = (FunctionDef) generatedFunction.getFD();
		deduce_generated_function_base(generatedFunction, fd);
	}

	private void add_proc_table_listeners(@NotNull final BaseGeneratedFunction generatedFunction) {
		for (final @NotNull ProcTableEntry pte : generatedFunction.prte_list) {
			pte.addStatusListener(new ProcTableListener(pte, generatedFunction, new DeduceClient2(this)));

			final InstructionArgument en = pte.expression_num;
			if (en != null) {
				if (en instanceof final @NotNull IdentIA identIA) {
					@NotNull final IdentTableEntry idte    = identIA.getEntry();
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
										@NotNull final IdentTableEntry idte2          = identIA.getEntry();
										@Nullable final ProcTableEntry procTableEntry = idte2.getCallablePTE();
//										if (procTableEntry == pte) tripleo.elijah.util.Stupidity.println_err2("940 procTableEntry == pte");
										if (procTableEntry != null) {
											// TODO doesn't seem like we need this
											procTableEntry.onFunctionInvocation(new DoneCallback<FunctionInvocation>() {
												@Override
												public void onDone(@NotNull final FunctionInvocation functionInvocation) {
													final ClassInvocation     ci  = functionInvocation.getClassInvocation();
													final NamespaceInvocation nsi = functionInvocation.getNamespaceInvocation();
													// do we register?? probably not
													assert ci != null || nsi != null;
													@NotNull final FunctionInvocation fi = newFunctionInvocation((FunctionDef) el, pte, ci != null ? ci : nsi, phase);

													{
														if (functionInvocation.getClassInvocation() == fi.getClassInvocation() &&
														  functionInvocation.getFunction() == fi.getFunction() &&
														  functionInvocation.pte == fi.pte) {
//														tripleo.elijah.util.Stupidity.println_err2("955 It seems like we are generating the same thing...");
														} else {
															final int ok = 2;
														}

													}
													generatedFunction.addDependentFunction(fi);
												}
											});
											// END
										}
									}
									break;
								case CONSTRUCTOR:
									final int y = 2;
									break;
								default:
									LOG.err(String.format("228 Don't know what to do %s %s", type, el));
									break;
							}
						}
					});
				} else if (en instanceof IntegerIA) {
					// TODO this code does nothing so commented out
/*
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
*/
				} else
					throw new NotImplementedException();
			}
		}
	}

	@NotNull FunctionInvocation newFunctionInvocation(final BaseFunctionDef aFunctionDef, final ProcTableEntry aPte, @NotNull final IInvocation aInvocation, @NotNull final DeducePhase aDeducePhase) {
		@NotNull final FunctionInvocation fi = new FunctionInvocation(aFunctionDef, aPte, aInvocation, aDeducePhase.generatePhase);
		// TODO register here
		return fi;
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
					LOG.err("267 attached == null for " + typeTableEntry);

					if (typeTableEntry.expression != null)
						l.add(String.format("<Unknown expression: %s>", typeTableEntry.expression));
					else
						l.add("<Unknkown>");
				}
			}

			final String sb2 = "[" +
			  Helpers.String_join(", ", l) +
			  "]";
			pte_string = sb2;
		}
		return pte_string;
	}

//	public boolean deduceOneFunction(@NotNull final GeneratedFunction aGeneratedFunction, @NotNull final DeducePhase aDeducePhase) {
//		if (aGeneratedFunction.deducedAlready) return false;
//		deduce_generated_function(aGeneratedFunction);
//		aGeneratedFunction.deducedAlready = true;
//		for (@NotNull final IdentTableEntry identTableEntry : aGeneratedFunction.idte_list) {
//			if (identTableEntry.getResolvedElement() instanceof VariableStatement) {
//				final @NotNull VariableStatement vs = (VariableStatement) identTableEntry.getResolvedElement();
//				final OS_Element el = vs.getParent().getParent();
//				final OS_Element el2 = aGeneratedFunction.getFD().getParent();
//				if (el != el2) {
//					if (el instanceof ClassStatement || el instanceof NamespaceStatement)
//						// NOTE there is no concept of gf here
//						aDeducePhase.registerResolvedVariable(identTableEntry, el, vs.getName());
//				}
//			}
//		}
//		{
//			final @NotNull GeneratedFunction gf = aGeneratedFunction;
//
//			@Nullable InstructionArgument result_index = gf.vte_lookup("Result");
//			if (result_index == null) {
//				// if there is no Result, there should be Value
//				result_index = gf.vte_lookup("Value");
//				// but Value might be passed in. If it is, discard value
//				if (result_index != null) {
//					@NotNull final VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
//					if (vte.vtt != VariableTableType.RESULT) {
//						result_index = null;
//					}
//				}
//			}
//			if (result_index != null) {
//				@NotNull final VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
//				if (vte.resolvedType() == null) {
//					final GenType b = vte.genType;
//					final OS_Type a = vte.type.getAttached();
//					if (a != null) {
//						// see resolve_function_return_type
//						switch (a.getType()) {
//							case USER_CLASS:
//								dof_uc(vte, a);
//								break;
//							case USER:
//								vte.genType.typeName = a;
//								try {
//									@NotNull final GenType rt = resolve_type(a, a.getTypeName().getContext());
//									if (rt.resolved != null && rt.resolved.getType() == OS_Type.Type.USER_CLASS) {
//										if (rt.resolved.getClassOf().getGenericPart().size() > 0)
//											vte.genType.nonGenericTypeName = a.getTypeName(); // TODO might be wrong
//										dof_uc(vte, rt.resolved);
//									}
//								} catch (final ResolveError aResolveError) {
//									errSink.reportDiagnostic(aResolveError);
//								}
//								break;
//							default:
//								// TODO do nothing for now
//								final int y3 = 2;
//								break;
//						}
//					} /*else
//							throw new NotImplementedException();*/
//				}
//			}
//		}
//		aDeducePhase.addFunction(aGeneratedFunction, (FunctionDef) aGeneratedFunction.getFD());
//		return true;
//	}

	private void dof_uc(@NotNull final VariableTableEntry aVte, final OS_Type aA) {
		// we really want a ci from somewhere
		assert aA.getClassOf().getGenericPart().size() == 0;
		@Nullable ClassInvocation ci = new ClassInvocation(aA.getClassOf(), null);
		ci = phase.registerClassInvocation(ci);

		aVte.genType.resolved = aA; // README assuming OS_Type cannot represent namespaces
		aVte.genType.ci       = ci;

		ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
			@Override
			public void onDone(final GeneratedClass result) {
				aVte.resolveTypeToClass(result);
			}
		});
	}

	public void deduceClasses(@NotNull final List<GeneratedClass> matching_class_list) {
		for (final GeneratedClass generatedClass : matching_class_list) {
			for (final GeneratedContainer.VarTableEntry entry : generatedClass.varTable) {
				final OS_Type vt      = entry.varType;
				final GenType genType = makeGenTypeFromOSType(vt, generatedClass.ci.genericPart);
				if (genType != null) {
					if (genType.node != null) {
						entry.resolve(genType.node);
					} else {
						NotImplementedException.raise();
					}
				}

				NotImplementedException.raise();
			}
		}
	}

	public String getFileName() {
		return module.getFileName();
	}

	public @NotNull GenerateFunctions getGenerateFunctions(@NotNull final OS_Module aModule) {
		return phase.generatePhase.getGenerateFunctions(aModule);
	}

	private GenType makeGenTypeFromOSType(final OS_Type aType, final @Nullable Map<TypeName, OS_Type> aGenericPart) {
		final GenType gt = new GenType();
		gt.typeName = aType;
		if (aType.getType() == OS_Type.Type.USER) {
			final TypeName tn1 = aType.getTypeName();
			if (tn1.isNull()) return null; // TODO Unknown, needs to resolve somewhere

			assert tn1 instanceof NormalTypeName;
			final NormalTypeName tn = (NormalTypeName) tn1;
			_makeGenTypeFromOSType__NormalTypeName(aGenericPart, gt, tn1, tn);
		} else if (aType.getType() == OS_Type.Type.USER_CLASS) {
			gt.resolved = new OS_UserClassType(aType.getClassOf());
		} else
			throw new AssertionError("Not a USER Type");
		return gt;
	}

	private void _makeGenTypeFromOSType__NormalTypeName(final @Nullable Map<TypeName, OS_Type> aGenericPart, final GenType gt, final TypeName tn1, final @NotNull NormalTypeName tn) {
		final LookupResultList     lrl = tn.getContext().lookup(tn.getName());
		final @Nullable OS_Element el  = lrl.chooseBest(null);

		ProcessElement.processElement(el, new IElementProcessor() {
			@Override
			public void elementIsNull() {
				final int y = 2;
			}

			@Override
			public void hasElement(final OS_Element el) {
				final @Nullable OS_Element best = preprocess(el);
				if (best == null) return;

				if (best instanceof final ClassStatement classStatement) {
					gt.resolved = new OS_UserClassType(classStatement);
				} else if (best instanceof final ClassContext.OS_TypeNameElement typeNameElement) {
					_makeGenTypeFromOSType__NormalTypeName__TypeNameElement(typeNameElement);
				} else {
					LOG.err("143 " + el);
					throw new NotImplementedException();
				}

				gotResolved(gt);
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
					LOG.err("152 Can't resolve Alias statement " + best);
					errSink.reportDiagnostic(aResolveError);
					return null;
				}
			}

			private void _makeGenTypeFromOSType__NormalTypeName__TypeNameElement(final ClassContext.OS_TypeNameElement typeNameElement) {
				assert aGenericPart != null;
				final OS_Type x = aGenericPart.get(typeNameElement.getTypeName());
				switch (x.getType()) {
					case USER_CLASS:
						final OS_Element best2 = x.getClassOf(); // always a ClassStatement

						// TODO test next 4 lines are copies of above
						if (best2 instanceof final ClassStatement classStatement) {
							gt.resolved = new OS_UserClassType(classStatement);
						}
						break;
					case USER:
						final NormalTypeName tn2 = (NormalTypeName) x.getTypeName();
						final LookupResultList lrl2 = tn.getContext().lookup(tn2.getName());
						final @Nullable OS_Element el2 = lrl2.chooseBest(null);

						// TODO test next 4 lines are copies of above
						if (el2 instanceof final ClassStatement classStatement) {
							gt.resolved = new OS_UserClassType(classStatement);
						} else
							throw new NotImplementedException();
						break;
				}
			}

			private void gotResolved(final GenType gt) {
				if (gt.resolved.getClassOf().getGenericPart().size() != 0) {
					//throw new AssertionError();
					LOG.info("149 non-generic type " + tn1);
				}
				genCI(gt, null); // TODO aGenericPart
				assert gt.ci != null;
				if (gt.ci instanceof final NamespaceInvocation nsi) {
					nsi.resolveDeferred().then(new DoneCallback<GeneratedNamespace>() {
						@Override
						public void onDone(final GeneratedNamespace result) {
							gt.node = result;
						}
					});
				} else if (gt.ci instanceof final ClassInvocation ci) {
					ci.resolvePromise().then(new DoneCallback<GeneratedClass>() {
						@Override
						public void onDone(final GeneratedClass result) {
							gt.node = result;
						}
					});
				} else
					throw new NotImplementedException();
			}
		});
	}

	public void resolve_ident_table_entry(@NotNull final IdentTableEntry ite, @NotNull final BaseGeneratedFunction generatedFunction, final Context ctx) {
		@Nullable InstructionArgument itex = new IdentIA(ite.getIndex(), generatedFunction);
		{
			while (itex != null && itex instanceof IdentIA) {
				@NotNull final IdentTableEntry itee = ((IdentIA) itex).getEntry();

				@Nullable BaseTableEntry x = null;
				if (itee.getBacklink() instanceof IntegerIA) {
					@NotNull final VariableTableEntry vte = ((IntegerIA) itee.getBacklink()).getEntry();
					x = vte;
//					if (vte.constructable_pte != null)
					itex = null;
				} else if (itee.getBacklink() instanceof IdentIA) {
					x    = ((IdentIA) itee.getBacklink()).getEntry();
					itex = ((IdentTableEntry) x).getBacklink();
				} else if (itee.getBacklink() instanceof ProcIA) {
					x = ((ProcIA) itee.getBacklink()).getEntry();
//					if (itee.getCallablePTE() == null)
//						// turned out to be wrong (by double calling), so let's wrap it
//						itee.setCallablePTE((ProcTableEntry) x);
					itex = null; //((ProcTableEntry) x).backlink;
				} else if (itee.getBacklink() == null) {
					itex = null;
					x    = null;
				}

				if (x != null) {
//					LOG.err("162 Adding FoundParent for "+itee);
//					LOG.err(String.format("1656 %s \n\t %s \n\t%s", x, itee, itex));
					x.addStatusListener(new FoundParent(this, x, itee, itee.getIdent().getContext(), generatedFunction)); // TODO context??
				}
			}
		}
		if (ite.getResolvedElement() != null)
			return;
		if (true) {
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

	@Nullable
	public ClassInvocation genCI(@NotNull final GenType genType, final TypeName aGenericTypeName) {
		if (genType.nonGenericTypeName != null) {
			@NotNull final NormalTypeName aTyn1           = (NormalTypeName) genType.nonGenericTypeName;
			@Nullable final String        constructorName = null; // TODO this comes from nowhere
			final ClassStatement          best            = genType.resolved.getClassOf();
			//
			@NotNull final List<TypeName> gp     = best.getGenericPart();
			@Nullable ClassInvocation     clsinv = new ClassInvocation(best, constructorName);
			if (gp.size() > 0) {
				final TypeNameList gp2 = aTyn1.getGenericPart();
				for (int i = 0; i < gp.size(); i++) {
					final TypeName         typeName = gp2.get(i);
					@NotNull final GenType typeName2;
					try {
						typeName2 = resolve_type(new OS_UserType(typeName), typeName.getContext());
						clsinv.set(i, gp.get(i), typeName2.resolved);
					} catch (final ResolveError aResolveError) {
						aResolveError.printStackTrace();
						return null;
					}
				}
			}
			clsinv     = phase.registerClassInvocation(clsinv);
			genType.ci = clsinv;
			return clsinv;
		}
		if (genType.resolved != null) {
			final ClassStatement   best            = genType.resolved.getClassOf();
			@Nullable final String constructorName = null; // TODO what to do about this, nothing I guess

			@NotNull final List<TypeName> gp = best.getGenericPart();
			@Nullable ClassInvocation     clsinv;
			if (genType.ci == null) {
				clsinv = new ClassInvocation(best, constructorName);
				if (gp.size() > 0) {
					if (aGenericTypeName instanceof final @NotNull NormalTypeName tn) {
						final TypeNameList            tngp = tn.getGenericPart();
						for (int i = 0; i < gp.size(); i++) {
							final TypeName         typeName = tngp.get(i);
							@NotNull final GenType typeName2;
							try {
								typeName2 = resolve_type(new OS_UserType(typeName), typeName.getContext());
								clsinv.set(i, gp.get(i), typeName2.resolved);
							} catch (final ResolveError aResolveError) {
//								aResolveError.printStackTrace();
								errSink.reportDiagnostic(aResolveError);
								return null;
							}
						}
					}
				}
				clsinv     = phase.registerClassInvocation(clsinv);
				genType.ci = clsinv;
			} else
				clsinv = (ClassInvocation) genType.ci;
			return clsinv;
		}
		return null;
	}

	public boolean deduceOneConstructor(@NotNull final GeneratedConstructor aGeneratedConstructor, @NotNull final DeducePhase aDeducePhase) {
		if (aGeneratedConstructor.deducedAlready) return false;
		deduce_generated_function_base(aGeneratedConstructor, aGeneratedConstructor.getFD());
		aGeneratedConstructor.deducedAlready = true;
		for (@NotNull final IdentTableEntry identTableEntry : aGeneratedConstructor.idte_list) {
			if (identTableEntry.getResolvedElement() instanceof final @NotNull VariableStatement vs) {
				final OS_Element                 el  = vs.getParent().getParent();
				final OS_Element                 el2 = aGeneratedConstructor.getFD().getParent();
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

	public void deduce_generated_constructor(final GeneratedConstructor generatedFunction) {
		final @NotNull ConstructorDef fd = (ConstructorDef) generatedFunction.getFD();
		deduce_generated_function_base(generatedFunction, fd);
	}

	public void deduce_generated_function_base(final @NotNull BaseGeneratedFunction generatedFunction, @NotNull final BaseFunctionDef fd) {
		final Context fd_ctx = fd.getContext();
		//
		{
			final ProcTableEntry  pte        = generatedFunction.fi.pte;
			final @NotNull String pte_string = getPTEString(pte);
			LOG.err("** deduce_generated_function " + fd.name() + " " + pte_string);//+" "+((OS_Container)((FunctionDef)fd).getParent()).name());
		}
		//
		//
		for (final @NotNull Instruction instruction : generatedFunction.instructions()) {
			final Context context = generatedFunction.getContextFromPC(instruction.getIndex());
//			LOG.info("8006 " + instruction);
			switch (instruction.getName()) {
				case E:
					onEnterFunction(generatedFunction, context);
					break;
				case X:
					onExitFunction(generatedFunction, fd_ctx, context);
					break;
				case ES:
					break;
				case XS:
					break;
				case AGN:
					do_assign_normal(generatedFunction, fd_ctx, instruction, context);
					break;
				case AGNK: {
					final @NotNull IntegerIA          arg  = (IntegerIA) instruction.getArg(0);
					final @NotNull VariableTableEntry vte  = generatedFunction.getVarTableEntry(arg.getIndex());
					final InstructionArgument         i2   = instruction.getArg(1);
					final @NotNull ConstTableIA       ctia = (ConstTableIA) i2;
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
					final int                     pte_num = ((ProcIA) instruction.getArg(0)).getIndex();
					final @NotNull ProcTableEntry pte     = generatedFunction.getProcTableEntry(pte_num);
//				final InstructionArgument i2 = (instruction.getArg(1));
					{
						final @NotNull IdentIA identIA = (IdentIA) pte.expression_num;
						final String           x       = generatedFunction.getIdentIAPathNormal(identIA);
						LOG.info("298 Calling " + x);
						resolveIdentIA_(context, identIA, generatedFunction, new FoundElement(phase) {

							@SuppressWarnings("unused")
							final String xx = x;

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
								errSink.reportError("370 Can't find callsite " + x);
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
					final int                     i1  = to_int(instruction.getArg(0));
					final InstructionArgument     i2  = (instruction.getArg(1));
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
					implement_is_a(generatedFunction, instruction);
					break;
				case NOP:
					break;
				case CONSTRUCT:
					implement_construct(generatedFunction, instruction/*, context*/);
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
					LOG.err("703 " + vte.getName() + " " + vte.potentialTypes());
					errSink.reportDiagnostic(new CantDecideType(vte, vte.potentialTypes()));
				} else {
					// potential_size == 0
					// Result is handled by phase.typeDecideds, self is always valid
					if (/*vte.getName() != null &&*/ !(vte.vtt == VariableTableType.RESULT || vte.vtt == VariableTableType.SELF))
						errSink.reportDiagnostic(new CantDecideType(vte, vte.potentialTypes()));
				}
			} else if (vte.vtt == VariableTableType.RESULT) {
				final OS_Type attached = vte.type.getAttached();
				if (attached.getType() == OS_Type.Type.USER) {
					try {
						vte.type.setAttached(resolve_type(attached, fd_ctx));
					} catch (final ResolveError aResolveError) {
						aResolveError.printStackTrace();
						assert false;
					}
				}
			}
		}
		{
			//
			// NOW CALCULATE DEFERRED CALLS
			//
			for (final Integer deferred_call : generatedFunction.deferred_calls) {
				final Instruction instruction = generatedFunction.getInstruction(deferred_call);

				final int                     i1  = to_int(instruction.getArg(0));
				final InstructionArgument     i2  = (instruction.getArg(1));
				final @NotNull ProcTableEntry fn1 = generatedFunction.getProcTableEntry(i1);
				{
//					generatedFunction.deferred_calls.remove(deferred_call);
					implement_calls_(generatedFunction, fd_ctx, i2, fn1, instruction.getIndex());
				}
			}
		}
	}

	public void do_assign_normal(final @NotNull BaseGeneratedFunction generatedFunction, final Context aFd_ctx, final @NotNull Instruction instruction, final Context aContext) {
		// TODO doesn't account for __assign__
		final InstructionArgument agn_lhs = instruction.getArg(0);
		if (agn_lhs instanceof final @NotNull IntegerIA arg) {
			final @NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(arg.getIndex());
			final InstructionArgument         i2  = instruction.getArg(1);
			if (i2 instanceof IntegerIA) {
				final @NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(i2));
				vte.addPotentialType(instruction.getIndex(), vte2.type);
			} else if (i2 instanceof final @NotNull FnCallArgs fca) {
				do_assign_call(generatedFunction, aContext, vte, fca, instruction);
			} else if (i2 instanceof ConstTableIA) {
				do_assign_constant(generatedFunction, instruction, vte, (ConstTableIA) i2);
			} else if (i2 instanceof IdentIA) {
				@NotNull final IdentTableEntry idte = generatedFunction.getIdentTableEntry(to_int(i2));
				if (idte.type == null) {
					final IdentIA identIA = new IdentIA(idte.getIndex(), generatedFunction);
					resolveIdentIA_(aContext, identIA, generatedFunction, new FoundElement(phase) {

						@Override
						public void foundElement(final OS_Element e) {
							found_element_for_ite(generatedFunction, idte, e, aContext);
						}

						@Override
						public void noFoundElement() {
							// TODO: log error
						}
					});
				}
				assert idte.type != null;
				assert idte.getResolvedElement() != null;
				vte.addPotentialType(instruction.getIndex(), idte.type);
			} else if (i2 instanceof ProcIA) {
				throw new NotImplementedException();
			} else
				throw new NotImplementedException();
		} else if (agn_lhs instanceof final @NotNull IdentIA arg) {
			final @NotNull IdentTableEntry idte = arg.getEntry();
			final InstructionArgument      i2   = instruction.getArg(1);
			if (i2 instanceof IntegerIA) {
				final @NotNull VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(i2));
				idte.addPotentialType(instruction.getIndex(), vte2.type);
			} else if (i2 instanceof final @NotNull FnCallArgs fca) {
				do_assign_call(generatedFunction, aFd_ctx, idte, fca, instruction.getIndex());
			} else if (i2 instanceof IdentIA) {
				if (idte.getResolvedElement() instanceof VariableStatement) {
					do_assign_normal_ident_deferred(generatedFunction, aFd_ctx, idte);
				}
				@NotNull final IdentTableEntry idte2 = generatedFunction.getIdentTableEntry(to_int(i2));
				do_assign_normal_ident_deferred(generatedFunction, aFd_ctx, idte2);
				idte.addPotentialType(instruction.getIndex(), idte2.type);
			} else if (i2 instanceof ConstTableIA) {
				do_assign_constant(generatedFunction, instruction, idte, (ConstTableIA) i2);
			} else if (i2 instanceof ProcIA) {
				throw new NotImplementedException();
			} else
				throw new NotImplementedException();
		}
	}

	public void do_assign_normal_ident_deferred(final @NotNull BaseGeneratedFunction generatedFunction,
	                                            final @NotNull Context aContext,
	                                            final @NotNull IdentTableEntry aIdentTableEntry) {
		if (aIdentTableEntry.type == null) {
			aIdentTableEntry.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, (OS_Type) null);
		}
		final LookupResultList     lrl1 = aContext.lookup(aIdentTableEntry.getIdent().getText());
		@Nullable final OS_Element best = lrl1.chooseBest(null);
		if (best != null) {
			aIdentTableEntry.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
			// TODO check for elements which may contain type information
			if (best instanceof final @NotNull VariableStatement vs) {
				do_assign_normal_ident_deferred_VariableStatement(generatedFunction, aIdentTableEntry, vs);
			} else if (best instanceof final FormalArgListItem fali) {
				do_assign_normal_ident_deferred_FALI(generatedFunction, aIdentTableEntry, fali);
			} else
				throw new NotImplementedException();
		} else {
			aIdentTableEntry.setStatus(BaseTableEntry.Status.UNKNOWN, null);
			LOG.err("242 Bad lookup" + aIdentTableEntry.getIdent().getText());
		}
	}

	private void do_assign_normal_ident_deferred_FALI(final BaseGeneratedFunction generatedFunction, final IdentTableEntry aIdentTableEntry, final FormalArgListItem fali) {
		final GenType     genType = new GenType();
		final IInvocation invocation;
		if (generatedFunction.fi.getClassInvocation() != null) {
			invocation       = generatedFunction.fi.getClassInvocation();
			genType.resolved = ((ClassInvocation) invocation).getKlass().getOS_Type();
		} else {
			invocation        = generatedFunction.fi.getNamespaceInvocation();
			genType.resolvedn = ((NamespaceInvocation) invocation).getNamespace();
		}
		genType.ci = invocation;
		final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(fali.name());
		assert vte_ia != null;
		((IntegerIA) vte_ia).getEntry().typeResolvePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(final GenType result) {
				assert result.resolved != null;
				aIdentTableEntry.type.setAttached(result.resolved);
			}
		});
		generatedFunction.addDependentType(genType);
	}

	public void do_assign_normal_ident_deferred_VariableStatement(final @NotNull BaseGeneratedFunction generatedFunction, final @NotNull IdentTableEntry aIdentTableEntry, final @NotNull VariableStatement vs) {
		final IInvocation invocation;
		if (generatedFunction.fi.getClassInvocation() != null)
			invocation = generatedFunction.fi.getClassInvocation();
		else
			invocation = generatedFunction.fi.getNamespaceInvocation();
		@NotNull final DeferredMember dm = deferred_member(vs.getParent().getParent(), invocation, vs, aIdentTableEntry);
		dm.typePromise().done(new DoneCallback<GenType>() {
			@Override
			public void onDone(@NotNull final GenType result) {
				assert result.resolved != null;
				aIdentTableEntry.type.setAttached(result.resolved);
			}
		});
		final GenType genType = new GenType();
		genType.ci = dm.getInvocation();
		if (genType.ci instanceof NamespaceInvocation) {
			genType.resolvedn = ((NamespaceInvocation) genType.ci).getNamespace();
		} else if (genType.ci instanceof ClassInvocation) {
			genType.resolved = ((ClassInvocation) genType.ci).getKlass().getOS_Type();
		} else {
			throw new IllegalStateException();
		}
		generatedFunction.addDependentType(genType);
	}

	private void implement_is_a(final @NotNull BaseGeneratedFunction gf, final @NotNull Instruction instruction) {
		final IntegerIA testing_var_  = (IntegerIA) instruction.getArg(0);
		final IntegerIA testing_type_ = (IntegerIA) instruction.getArg(1);
		final Label     target_label  = ((LabelIA) instruction.getArg(2)).label;

		final VariableTableEntry testing_var    = gf.getVarTableEntry(testing_var_.getIndex());
		final TypeTableEntry     testing_type__ = gf.getTypeTableEntry(testing_type_.getIndex());

		final GenType genType = testing_type__.genType;
		if (genType.resolved == null) {
			try {
				genType.resolved = resolve_type(genType.typeName, gf.getFD().getContext()).resolved;
			} catch (final ResolveError aResolveError) {
//				aResolveError.printStackTrace();
				errSink.reportDiagnostic(aResolveError);
				return;
			}
		}
		if (genType.ci == null) {
			genType.genCI(genType.nonGenericTypeName, this, errSink, phase);
		}
		if (genType.node == null) {
			if (genType.ci instanceof ClassInvocation) {
				final WlGenerateClass gen = new WlGenerateClass(getGenerateFunctions(module), (ClassInvocation) genType.ci, phase.generatedClasses, phase.codeRegistrar);
				gen.run(null);
				genType.node = gen.getResult();
			} else if (genType.ci instanceof NamespaceInvocation) {
				final WlGenerateNamespace gen = new WlGenerateNamespace(getGenerateFunctions(module), (NamespaceInvocation) genType.ci, phase.generatedClasses, phase.codeRegistrar);
				gen.run(null);
				genType.node = gen.getResult();
			}
		}
		final GeneratedNode testing_type = testing_type__.resolved();
		assert testing_type != null;
	}

	public void onEnterFunction(final @NotNull BaseGeneratedFunction generatedFunction, final Context aContext) {
		for (final VariableTableEntry variableTableEntry : generatedFunction.vte_list) {
			variableTableEntry.setDeduceTypes2(this, aContext, generatedFunction);
		}
		for (final IdentTableEntry identTableEntry : generatedFunction.idte_list) {
			identTableEntry.setDeduceTypes2(this, aContext, generatedFunction);
		}
		for (final ProcTableEntry procTableEntry : generatedFunction.prte_list) {
			procTableEntry.setDeduceTypes2(this, aContext, generatedFunction, errSink);
		}
		//
		// resolve all cte expressions
		//
		for (final @NotNull ConstantTableEntry cte : generatedFunction.cte_list) {
			resolve_cte_expression(cte, aContext);
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
			resolve_ident_table_entry(ite, generatedFunction, aContext);
		}
		//
		// resolve arguments table
		//
		@NotNull final Resolve_Variable_Table_Entry    rvte = new Resolve_Variable_Table_Entry(generatedFunction, aContext, this);
		@NotNull final DeduceTypes2.IVariableConnector connector;
		if (generatedFunction instanceof GeneratedConstructor) {
			connector = new CtorConnector((GeneratedConstructor) generatedFunction);
		} else {
			connector = new NullConnector();
		}
		for (@NotNull final VariableTableEntry vte : generatedFunction.vte_list) {
			rvte.action(vte, connector);
		}
	}

	@NotNull
	public GenType resolve_type(final @Nullable OS_Type type, final Context ctx) throws ResolveError {
		return resolve_type(module, type, ctx);
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
			case IDENT: {
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
			default: {
				LOG.err("8192 " + initialValue.getKind());
				throw new NotImplementedException();
			}
		}
	}

	@NotNull GenType resolve_type(final OS_Module module, final @Nullable OS_Type type, final Context ctx) throws ResolveError {
		@NotNull final GenType R = new GenType();
		R.typeName = type;

		switch (type.getType()) {

		case BUILT_IN: {
			switch (type.getBType()) {
			case SystemInteger: {
				@NotNull final String typeName = type.getBType().name();
				assert typeName.equals("SystemInteger");
				OS_Module prelude = module.prelude;
				if (prelude == null) // README Assume `module' IS prelude
					prelude = module;
				final LookupResultList lrl  = prelude.getContext().lookup(typeName);
				@Nullable OS_Element   best = lrl.chooseBest(null);
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
				R.resolved = new OS_UserClassType((ClassStatement) best);
				break;
					}
					case String_: {
						@NotNull final String typeName = type.getBType().name();
						assert typeName.equals("String_");
						OS_Module prelude = module.prelude;
						if (prelude == null) // README Assume `module' IS prelude
							prelude = module;
						final LookupResultList lrl  = prelude.getContext().lookup("ConstString"); // TODO not sure about String
						@Nullable OS_Element   best = lrl.chooseBest(null);
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
						R.resolved = new OS_UserClassType((ClassStatement) best);
						break;
					}
					case SystemCharacter: {
						@NotNull final String typeName = type.getBType().name();
						assert typeName.equals("SystemCharacter");
						OS_Module prelude = module.prelude;
						if (prelude == null) { // README Assume `module' IS prelude
							prelude = module;
							assert module != null;
							assert prelude.getContext() != null;
						}
						final LookupResultList lrl  = prelude.getContext().lookup("SystemCharacter");
						@Nullable OS_Element   best = lrl.chooseBest(null);
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
						R.resolved = new OS_UserClassType((ClassStatement) best);
						break;
					}
					case Boolean: {
						OS_Module prelude = module.prelude;
						if (prelude == null) // README Assume `module' IS prelude
							prelude = module;
						final LookupResultList     lrl  = prelude.getContext().lookup("Boolean");
						final @Nullable OS_Element best = lrl.chooseBest(null);
						R.resolved = new OS_UserClassType((ClassStatement) best); // TODO might change to Type
						break;
					}
					default:
						throw new IllegalStateException("531 Unexpected value: " + type.getBType());
				}
				break;
			}
			case USER: {
				final TypeName tn1 = type.getTypeName();
				switch (tn1.kindOfType()) {
					case NORMAL: {
						final Qualident tn = ((NormalTypeName) tn1).getRealName();
						LOG.info("799 [resolving USER type named] " + tn);
						final LookupResultList lrl  = DeduceLookupUtils.lookupExpression(tn, tn1.getContext(), this);
						@Nullable OS_Element   best = lrl.chooseBest(null);
						while (best instanceof AliasStatement) {
							best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, this);
						}
						if (best == null) {
							if (tn.asSimpleString().equals("Any"))
								/*return*/ R.resolved = new OS_AnyType(); // TODO not a class
							throw new ResolveError(tn1, lrl);
						}

						if (best instanceof ClassContext.OS_TypeNameElement) {
							/*return*/
							R.resolved = new OS_GenericTypeNameType((ClassContext.OS_TypeNameElement) best); // TODO not a class
						} else
							R.resolved = new OS_UserClassType((ClassStatement) best);
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
		case FUNC_EXPR:
			final int y = 2;
			break;
		default:
			throw new IllegalStateException("565 Unexpected value: " + type.getType());
		}

		return R;
	}

	/*static*/
	@NotNull Promise<GenType, ResolveError, Void> resolve_type_p(final OS_Module module, final @NotNull OS_Type type, final Context ctx) {
		final Deduce_Type dt = new Deduce_Type(type);
		dt.doResolveType(module, ctx, this);
		return dt.getType();
	}

	private void resolve_cte_expression_builtin(@NotNull final ConstantTableEntry cte, final Context aContext, final BuiltInTypes aBuiltInType) {
		final OS_Type a = cte.getTypeTableEntry().getAttached();
		if (a == null || a.getType() != OS_Type.Type.USER_CLASS) {
			try {
				cte.getTypeTableEntry().setAttached(resolve_type(new OS_BuiltinType(aBuiltInType), aContext));
			} catch (final ResolveError resolveError) {
				tripleo.elijah.util.Stupidity.println2("117 Can't be here");
//				resolveError.printStackTrace(); // TODO print diagnostic
			}
		}
	}

	public void onExitFunction(final @NotNull BaseGeneratedFunction generatedFunction, final Context aFd_ctx, final Context aContext) {
		//
		// resolve var table. moved from `E'
		//
		for (@NotNull final VariableTableEntry vte : generatedFunction.vte_list) {
			final DeduceElement3_VariableTableEntry vte_de = (DeduceElement3_VariableTableEntry) vte.getDeduceElement3();
			vte_de.mvState(null, DeduceElement3_VariableTableEntry.ST.EXIT_RESOLVE);
		}
		for (@NotNull final IStateRunnable runnable : onRunnables) {
			runnable.mvState(null, IStateRunnable.ST.EXIT_RUN);
		}
//					LOG.info("167 "+generatedFunction);
		//
		// ATTACH A TYPE TO VTE'S
		// CONVERT USER TYPES TO USER_CLASS TYPES
		//
		for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
//						LOG.info("704 "+vte.type.attached+" "+vte.potentialTypes());
			final DeduceElement3_VariableTableEntry vte_de = (DeduceElement3_VariableTableEntry) vte.getDeduceElement3();
			vte_de.setDeduceTypes2(this, generatedFunction);
			vte_de.mvState(null, DeduceElement3_VariableTableEntry.ST.EXIT_CONVERT_USER_TYPES);
		}
		for (final @NotNull VariableTableEntry vte : generatedFunction.vte_list) {
			if (vte.vtt == VariableTableType.ARG) {
				final OS_Type attached = vte.type.getAttached();
				if (attached != null) {
					if (attached.getType() == OS_Type.Type.USER)
						//throw new AssertionError();
						errSink.reportError("369 ARG USER type (not deduced) " + vte);
				} else {
					errSink.reportError("457 ARG type not deduced/attached " + vte);
				}
			}
		}
		//
		// ATTACH A TYPE TO IDTE'S
		//
		for (@NotNull final IdentTableEntry ite : generatedFunction.idte_list) {
			final DeduceElement3_IdentTableEntry ite_de = (DeduceElement3_IdentTableEntry) ite.getDeduceElement3(this, generatedFunction);
			ite_de._ctxts(aFd_ctx, aContext);
			ite_de.mvState(null, DeduceElement3_IdentTableEntry.ST.EXIT_GET_TYPE);
		}
		{
			// TODO why are we doing this?
			final Resolve_each_typename ret = new Resolve_each_typename(phase, this, errSink);
			for (final TypeTableEntry typeTableEntry : generatedFunction.tte_list) {
				ret.action(typeTableEntry);
			}
		}
		{
			final @NotNull WorkManager  workManager = wm;//new WorkManager();
			@NotNull final Dependencies deps        = new Dependencies(this,/*phase, this, errSink*/workManager);
			deps.subscribeTypes(generatedFunction.dependentTypesSubject());
			deps.subscribeFunctions(generatedFunction.dependentFunctionSubject());
//						for (@NotNull GenType genType : generatedFunction.dependentTypes()) {
//							deps.action_type(genType, workManager);
//						}
//						for (@NotNull FunctionInvocation dependentFunction : generatedFunction.dependentFunctions()) {
//							deps.action_function(dependentFunction, workManager);
//						}
			final int x = workManager.totalSize();

			workManager.drain();
		}
		//
		// RESOLVE FUNCTION RETURN TYPES
		//
		resolve_function_return_type(generatedFunction);
		{
			for (final VariableTableEntry variableTableEntry : generatedFunction.vte_list) {
				final @NotNull Collection<TypeTableEntry> pot = variableTableEntry.potentialTypes();
				final int                                 y   = 2;
				if (pot.size() == 1 && variableTableEntry.genType.isNull()) {
					final OS_Type x = pot.iterator().next().getAttached();
					if (x != null)
						if (x.getType() == OS_Type.Type.USER_CLASS) {
							try {
								final @NotNull GenType yy = resolve_type(x, aFd_ctx);
								// HACK TIME
								if (yy.resolved == null && yy.typeName.getType() == OS_Type.Type.USER_CLASS) {
									yy.resolved = yy.typeName;
									yy.typeName = null;
								}

								yy.genCIForGenType2(this);
								variableTableEntry.resolveType(yy);
								variableTableEntry.resolveTypeToClass(yy.node);
//								variableTableEntry.dlv.type.resolve(yy);
							} catch (final ResolveError aResolveError) {
								aResolveError.printStackTrace();
							}
						}
				}
			}
		}
		//
		// LOOKUP FUNCTIONS
		//
		{
			@NotNull final DeduceTypes2.Lookup_function_on_exit lfoe = new Lookup_function_on_exit();
			for (@NotNull final ProcTableEntry pte : generatedFunction.prte_list) {
				lfoe.action(pte);
			}
			wm.drain();
		}


		for (final ProcTableEntry procTableEntry : generatedFunction.prte_list) {
			final DeduceElement3_ProcTableEntry de_pte = (DeduceElement3_ProcTableEntry) procTableEntry.getDeduceElement3(this, generatedFunction);

			de_pte.doFunctionInvocation(); // TODO mvState
		}

		expectations.check();
	}

	@NotNull DeferredMemberFunction deferred_member_function(final OS_Element aParent,
	                                                         @Nullable IInvocation aInvocation,
	                                                         final BaseFunctionDef aFunctionDef,
	                                                         final FunctionInvocation aFunctionInvocation) {
		if (aInvocation == null) {
			if (aParent instanceof NamespaceStatement)
				aInvocation = phase.registerNamespaceInvocation((NamespaceStatement) aParent);
			else if (aParent instanceof OS_SpecialVariable) {
				aInvocation = ((OS_SpecialVariable) aParent).getInvocation(this);
			}
		}
		final DeferredMemberFunction dm = new DeferredMemberFunction(aParent, aInvocation, aFunctionDef, this, aFunctionInvocation);
		phase.addDeferredMember(dm);
		return dm;
	}

	void resolve_function_return_type(@NotNull final BaseGeneratedFunction generatedFunction) {
		// MODERNIZATION Does this have any affinity with DeferredMember?
		@Nullable final InstructionArgument vte_index = generatedFunction.vte_lookup("Result");
		if (vte_index != null) {
			final @NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(to_int(vte_index));

			if (vte.type != null) {
				if (vte.type.getAttached() != null) {
					phase.typeDecided((GeneratedFunction) generatedFunction, vte.type.genType);
				} else {
					@NotNull final Collection<TypeTableEntry> pot1 = vte.potentialTypes();
					@NotNull final ArrayList<TypeTableEntry>  pot  = new ArrayList<TypeTableEntry>(pot1);
					if (pot.size() == 1) {
						phase.typeDecided((GeneratedFunction) generatedFunction, pot.get(0).genType);
					} else if (pot.size() == 0) {
						@NotNull final GenType unitType = new GenType();
						unitType.typeName = new OS_BuiltinType(BuiltInTypes.Unit);
						phase.typeDecided((GeneratedFunction) generatedFunction, unitType);
					} else {
						// TODO report some kind of error/diagnostic and/or let ForFunction know...
						errSink.reportWarning("Can't resolve type of `Result'. potentialTypes > 1 for " + vte);
					}
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
				unitType.typeName = new OS_BuiltinType(BuiltInTypes.Unit);
				phase.typeDecided((GeneratedFunction) generatedFunction, unitType);
			}
		}
	}

	/**
	 * See {@link Implement_construct#_implement_construct_type}
	 */
	private @Nullable ClassInvocation genCI(@NotNull final TypeTableEntry aType) {
		final GenType genType = aType.genType;
		if (genType.nonGenericTypeName != null) {
			@NotNull final NormalTypeName aTyn1           = (NormalTypeName) genType.nonGenericTypeName;
			@Nullable final String        constructorName = null; // TODO this comes from nowhere
			final ClassStatement          best            = genType.resolved.getClassOf();
			//
			@NotNull final List<TypeName> gp     = best.getGenericPart();
			@Nullable ClassInvocation     clsinv = new ClassInvocation(best, constructorName);
			if (gp.size() > 0) {
				final TypeNameList gp2 = aTyn1.getGenericPart();
				for (int i = 0; i < gp.size(); i++) {
					final TypeName         typeName = gp2.get(i);
					@NotNull final GenType genType1;
					try {
						genType1 = resolve_type(new OS_UserType(typeName), typeName.getContext());
						clsinv.set(i, gp.get(i), genType1.resolved);
					} catch (final ResolveError aResolveError) {
						aResolveError.printStackTrace();
						return null;
					}
				}
			}
			clsinv     = phase.registerClassInvocation(clsinv);
			genType.ci = clsinv;
			return clsinv;
		}
		if (genType.resolved != null) {
			final ClassStatement   best            = genType.resolved.getClassOf();
			@Nullable final String constructorName = null; // TODO what to do about this, nothing I guess

			@NotNull final List<TypeName> gp     = best.getGenericPart();
			@Nullable ClassInvocation     clsinv = new ClassInvocation(best, constructorName);
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
			clsinv     = phase.registerClassInvocation(clsinv);
			genType.ci = clsinv;
			return clsinv;
		}
		return null;
	}

	public void resolveIdentIA2_(@NotNull final Context context, @NotNull final IdentIA identIA, @NotNull final GeneratedFunction generatedFunction, @NotNull final FoundElement foundElement) {
		final @NotNull List<InstructionArgument> s = BaseGeneratedFunction._getIdentIAPathList(identIA);
		resolveIdentIA2_(context, identIA, s, generatedFunction, foundElement);
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

	@NotNull
	private ArrayList<TypeTableEntry> getPotentialTypesVte(@NotNull final GeneratedFunction generatedFunction, @NotNull final InstructionArgument vte_index) {
		return getPotentialTypesVte(generatedFunction.getVarTableEntry(to_int(vte_index)));
	}

	@NotNull ArrayList<TypeTableEntry> getPotentialTypesVte(@NotNull final VariableTableEntry vte) {
		return new ArrayList<TypeTableEntry>(vte.potentialTypes());
	}

	public void resolve_var_table_entry(@NotNull final VariableTableEntry vte, final BaseGeneratedFunction generatedFunction, final Context ctx) {
		if (vte.getResolvedElement() == null)
			return;
		{
			if (vte.type.getAttached() == null && vte.constructable_pte != null) {
				final ClassStatement   c        = vte.constructable_pte.getFunctionInvocation().getClassInvocation().getKlass();
				final @NotNull OS_Type attached = new OS_UserClassType(c);
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

	private void do_assign_call(final @NotNull BaseGeneratedFunction generatedFunction,
	                            final @NotNull Context ctx,
	                            final @NotNull VariableTableEntry vte,
	                            final @NotNull FnCallArgs fca,
	                            final @NotNull Instruction instruction) {
		final int                     instructionIndex = instruction.getIndex();
		final @NotNull ProcTableEntry pte              = generatedFunction.getProcTableEntry(to_int(fca.getArg(0)));
		@NotNull final IdentIA        identIA          = (IdentIA) pte.expression_num;

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

		if (identIA != null) {
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
									bgf.onType(new DoneCallback<GenType>() {
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
					tte.setAttached(new OS_BuiltinType(BuiltInTypes.SystemInteger));
					//vte.type = tte;
					break;
				case CHAR_LITERAL:
					tte.setAttached(new OS_BuiltinType(BuiltInTypes.SystemCharacter));
					break;
				case IDENT:
					do_assign_call_args_ident(generatedFunction, ctx, vte, instructionIndex, pte, i, tte, (IdentExpression) e);
					break;
				case PROCEDURE_CALL: {
					final @NotNull ProcedureCallExpression pce = (ProcedureCallExpression) e;
					try {
						final LookupResultList lrl  = DeduceLookupUtils.lookupExpression(pce.getLeft(), ctx, this);
						@Nullable OS_Element   best = lrl.chooseBest(null);
						if (best != null) {
							while (best instanceof AliasStatement) {
								best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, this);
							}
							if (best instanceof FunctionDef) {
								final OS_Element            parent = best.getParent();
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
								final int y = 2;
								throw new NotImplementedException();
							}
						} else {
							final int y = 2;
							throw new NotImplementedException();
						}
					} catch (final ResolveError aResolveError) {
						aResolveError.printStackTrace();
						final int y = 2;
						throw new NotImplementedException();
					}
				}
				break;
				case DOT_EXP: {
					final @NotNull DotExpression de = (DotExpression) e;
					try {
						final LookupResultList lrl  = DeduceLookupUtils.lookupExpression(de.getLeft(), ctx, this);
						@Nullable OS_Element   best = lrl.chooseBest(null);
						if (best != null) {
							while (best instanceof AliasStatement) {
								best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, this);
							}
							if (best instanceof FunctionDef) {
								tte.setAttached(new OS_FuncType((FunctionDef) best));
								//vte.addPotentialType(instructionIndex, tte);
							} else if (best instanceof ClassStatement) {
								tte.setAttached(new OS_UserClassType((ClassStatement) best));
							} else if (best instanceof final @NotNull VariableStatement vs) {
								@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(vs.getName());
								final TypeTableEntry                tte1   = ((IntegerIA) vte_ia).getEntry().type;
								tte.setAttached(tte1.getAttached());
							} else {
								final int y = 2;
								LOG.err(best.getClass().getName());
								throw new NotImplementedException();
							}
						} else {
							final int y = 2;
							throw new NotImplementedException();
						}
					} catch (final ResolveError aResolveError) {
						aResolveError.printStackTrace();
						final int y = 2;
						throw new NotImplementedException();
					}
				}
				break;

				case GET_ITEM: {
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
					final String           text = ((IdentExpression) pte.expression).getText();
					final LookupResultList lrl  = ctx.lookup(text);

					final @Nullable OS_Element best = lrl.chooseBest(null);
					if (best != null)
						pte.setResolvedElement(best); // TODO do we need to add a dependency for class?
					else {
						errSink.reportError("Cant resolve " + text);
					}
				} else {
					implement_calls(generatedFunction, ctx.getParent(), instruction.getArg(1), pte, instructionIndex);
				}
			} else {
				final int y = 2;
				resolveIdentIA_(ctx, identIA, generatedFunction, new FoundElement(phase) {

					final String x = generatedFunction.getIdentIAPathNormal(identIA);

					@Override
					public void foundElement(final OS_Element el) {
						if (pte.getResolvedElement() == null)
							pte.setResolvedElement(el);
						if (el instanceof @NotNull final FunctionDef fd) {
							final @Nullable IInvocation invocation;
							if (fd.getParent() == generatedFunction.getFD().getParent()) {
								invocation = getInvocation((GeneratedFunction) generatedFunction);
							} else {
								if (fd.getParent() instanceof NamespaceStatement) {
									final NamespaceInvocation ni = phase.registerNamespaceInvocation((NamespaceStatement) fd.getParent());
									invocation = ni;
								} else if (fd.getParent() instanceof final @NotNull ClassStatement classStatement) {
									@Nullable ClassInvocation     ci             = new ClassInvocation(classStatement, null);
									final @NotNull List<TypeName> genericPart    = classStatement.getGenericPart();
									if (genericPart.size() > 0) {
										// TODO handle generic parameters somehow (getInvocationFromBacklink?)

									}
									ci         = phase.registerClassInvocation(ci);
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
										LOG.err("2041 type already found " + vte);
										return; // type already found
									}
									// I'm not sure if below is ever called
									@NotNull final TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, gt(aType), pte.expression, pte);
									vte.addPotentialType(instructionIndex, tte);
								}
							});
						} else if (el instanceof @NotNull final ClassStatement kl) {
							@NotNull final OS_Type        type = new OS_UserClassType(kl);
							@NotNull final TypeTableEntry tte  = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, type, pte.expression, pte);
							vte.addPotentialType(instructionIndex, tte);
							vte.setConstructable(pte);

							register_and_resolve(vte, kl);
						} else {
							LOG.err("7890 " + el.getClass().getName());
						}
					}

					@Override
					public void noFoundElement() {
						LOG.err("IdentIA path cannot be resolved " + x);
					}
				});
			}
		}
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

	private void do_assign_constant(final @NotNull BaseGeneratedFunction generatedFunction, final @NotNull Instruction instruction, final @NotNull VariableTableEntry vte, final @NotNull ConstTableIA i2) {
		if (vte.type.getAttached() != null) {
			// TODO check types
		}
		final @NotNull ConstantTableEntry cte = generatedFunction.getConstTableEntry(i2.getIndex());
		if (cte.type.getAttached() == null) {
			LOG.info("Null type in CTE " + cte);
		}
//		vte.type = cte.type;
		vte.addPotentialType(instruction.getIndex(), cte.type);
	}

	private void __do_assign_call_GET_ITEM__VariableStatement(final @NotNull BaseGeneratedFunction generatedFunction,
	                                                          final @NotNull Context ctx,
	                                                          final @NotNull VariableStatement vs) throws NotImplementedException {
		final String                        s      = vs.getName();
		@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(s);
		if (vte_ia != null) {
			@NotNull final VariableTableEntry vte1 = generatedFunction.getVarTableEntry(to_int(vte_ia));
			throw new NotImplementedException();
		} else {
			final IdentTableEntry idte = generatedFunction.getIdentTableEntryFor(vs.getNameToken());
			assert idte != null;
			if (idte.type == null) return;

			@Nullable OS_Type ty = idte.type.getAttached();
			idte.onType(phase, new OnType() {
				@Override
				public void typeDeduced(final @NotNull OS_Type ty) {
					__do_assign_call_GET_ITEM__VariableStatement__idte__typeDeduced(ty, ctx, generatedFunction, idte);
				}

				@Override
				public void noTypeFound() {
					throw new NotImplementedException();
				}
			});
			if (ty == null) {
				@NotNull final TypeTableEntry tte3 = generatedFunction.newTypeTableEntry(
				  TypeTableEntry.Type.SPECIFIED, new OS_UserType(vs.typeName()), vs.getNameToken());
				idte.type = tte3;
				ty        = idte.type.getAttached();
			}
		}

		//				tte.attached = new OS_FuncType((FunctionDef) best); // TODO: what is this??
		//vte.addPotentialType(instructionIndex, tte);
	}

	public IInvocation getInvocation(@NotNull final GeneratedFunction generatedFunction) {
		final ClassInvocation     classInvocation = generatedFunction.fi.getClassInvocation();
		final NamespaceInvocation ni;
		if (classInvocation == null) {
			ni = generatedFunction.fi.getNamespaceInvocation();
			return ni;
		} else
			return classInvocation;
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

	void do_assign_call_args_ident(@NotNull final BaseGeneratedFunction generatedFunction,
	                               @NotNull final Context ctx,
	                               @NotNull final VariableTableEntry vte,
	                               final int aInstructionIndex,
	                               @NotNull final ProcTableEntry aPte,
	                               final int aI,
	                               @NotNull final TypeTableEntry aTte,
	                               @NotNull final IdentExpression aExpression) {
		final String                        e_text = aExpression.getText();
		final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(e_text);
//		LOG.info("10000 "+vte_ia);
		if (vte_ia != null) {
			final @NotNull VariableTableEntry  vte1 = generatedFunction.getVarTableEntry(to_int(vte_ia));
			final Promise<GenType, Void, Void> p    = vte1.typePromise();
			p.done(new DoneCallback<GenType>() {
				@Override
				public void onDone(final GenType result) {
//					assert vte != vte1;
//					aTte.setAttached(result.resolved != null ? result.resolved : result.typeName);
					aTte.genType.copy(result);
//					vte.addPotentialType(aInstructionIndex, result); // TODO!!
				}
			});
			onFinish(() -> {
				final DeduceElement3_VariableTableEntry vte_ = (DeduceElement3_VariableTableEntry) vte.getDeduceElement3();
				vte_.setDeduceTypes2(DeduceTypes2.this, generatedFunction);
				vte_.potentialTypesRunnableDo(vte_ia, LOG, vte1, errSink, ctx, e_text, vte);
			});
		} else {
			final int                      ia   = generatedFunction.addIdentTableEntry(aExpression, ctx);
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
					LOG.err("719 no type found " + generatedFunction.getIdentIAPathNormal(new IdentIA(ia, generatedFunction)));
				}
			});
		}
	}

	void implement_construct(final BaseGeneratedFunction generatedFunction, final Instruction instruction) {
		final @NotNull Implement_construct ic = newImplement_construct(generatedFunction, instruction);
		ic.action();
	}

	@NotNull
	public Implement_construct newImplement_construct(final BaseGeneratedFunction generatedFunction, final Instruction instruction) {
		return new Implement_construct(this, generatedFunction, instruction);
	}

	void do_assign_call_GET_ITEM(@NotNull final GetItemExpression gie, final TypeTableEntry tte, @NotNull final BaseGeneratedFunction generatedFunction, final Context ctx) {
		try {
			final LookupResultList     lrl  = DeduceLookupUtils.lookupExpression(gie.getLeft(), ctx, this);
			final @Nullable OS_Element best = lrl.chooseBest(null);
			if (best != null) {
				if (best instanceof @NotNull final VariableStatement vs) {
					__do_assign_call_GET_ITEM__VariableStatement(generatedFunction, ctx, vs);
					return;
				} else if (best instanceof final @Nullable FormalArgListItem fali) {
					__do_assign_call_GET_ITEM__FALI(generatedFunction, ctx, fali);
					return;
				} else if (best instanceof AliasStatement) {
					//
				}
			}
		} catch (final ResolveError aResolveError) {
//			aResolveError.printStackTrace();
//			NotImplementedException.raise();
			errSink.reportDiagnostic(aResolveError);
		}

		throw new NotImplementedException();
	}

	private void do_assign_call(final @NotNull BaseGeneratedFunction generatedFunction,
	                            final @NotNull Context ctx,
	                            final @NotNull IdentTableEntry idte,
	                            final @NotNull FnCallArgs fca,
	                            final int instructionIndex) {
		final @NotNull ProcTableEntry pte = generatedFunction.getProcTableEntry(to_int(fca.getArg(0)));
		for (final @NotNull TypeTableEntry tte : pte.getArgs()) {
			LOG.info("771 " + tte);
			final IExpression e = tte.expression;
			if (e == null) continue;
			switch (e.getKind()) {
			case NUMERIC: {
				tte.setAttached(new OS_BuiltinType(BuiltInTypes.SystemInteger));
				idte.type = tte; // TODO why not addPotentialType ? see below for example
			}
			break;
			case IDENT: {
				final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(((IdentExpression) e).getText());
				final @NotNull List<TypeTableEntry> ll     = getPotentialTypesVte((GeneratedFunction) generatedFunction, vte_ia);
				if (ll.size() == 1) {
					tte.setAttached(ll.get(0).getAttached());
					idte.addPotentialType(instructionIndex, ll.get(0));
				} else
					throw new NotImplementedException();
			}
			break;
			default: {
				throw new NotImplementedException();
			}
			}
		}
		{
			final String               s    = ((IdentExpression) pte.expression).getText();
			final LookupResultList     lrl  = ctx.lookup(s);
			final @Nullable OS_Element best = lrl.chooseBest(null);
			if (best != null) {
				pte.setResolvedElement(best); // TODO do we need to add a dependency for class?
			} else {
				throw new NotImplementedException();
			}
		}
	}

	private void __do_assign_call_GET_ITEM__FALI(final BaseGeneratedFunction generatedFunction,
	                                             final Context ctx,
	                                             final FormalArgListItem fali) {
		final String                        s      = fali.name();
		@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(s);
		if (vte_ia == null) {
			return;
		}


		@NotNull final VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(vte_ia));

		vte2.typePromise().done(vte2_gt -> {
			//assert false; // TODO this code is never reached
			final @Nullable OS_Type ty2 = vte2_gt.typeName;
			assert ty2 != null;
			@NotNull GenType rtype = null;
			try {
				rtype = resolve_type(ty2, ctx);
			} catch (final ResolveError resolveError) {
				//resolveError.printStackTrace();
				errSink.reportError("Cant resolve " + ty2); // TODO print better diagnostic
				return;
			}
			if (rtype.resolved != null && rtype.resolved.getType() == OS_Type.Type.USER_CLASS) {
				final LookupResultList     lrl2  = rtype.resolved.getClassOf().getContext().lookup("__getitem__");
				@Nullable final OS_Element best2 = lrl2.chooseBest(null);
				if (best2 != null) {
					if (best2 instanceof @Nullable final FunctionDef fd) {
						@Nullable final ProcTableEntry pte        = null;
						final IInvocation              invocation = getInvocation((GeneratedFunction) generatedFunction);
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
		});

/*
			if (ty2 == null) {
				@NotNull TypeTableEntry tte3 = generatedFunction.newTypeTableEntry(
						TypeTableEntry.Type.SPECIFIED, new OS_Type(fali.typeName()), fali.getNameToken());
				vte2.type = tte3;
//						ty2 = vte2.type.attached; // TODO this is final, but why assign anyway?
			}
*/
	}

	private void __do_assign_call_GET_ITEM__VariableStatement__idte__typeDeduced(final @NotNull OS_Type ty, final Context ctx, final BaseGeneratedFunction generatedFunction, final IdentTableEntry idte) {
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
			final LookupResultList     lrl2  = rtype.resolved.getClassOf().getContext().lookup("__getitem__");
			@Nullable final OS_Element best2 = lrl2.chooseBest(null);
			if (best2 != null) {
				if (best2 instanceof @NotNull final FunctionDef fd) {
					@Nullable final ProcTableEntry pte        = null;
					final IInvocation              invocation = getInvocation((GeneratedFunction) generatedFunction);
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

	private void do_assign_constant(final @NotNull BaseGeneratedFunction generatedFunction, final @NotNull Instruction instruction, final @NotNull IdentTableEntry idte, final @NotNull ConstTableIA i2) {
		if (idte.type != null && idte.type.getAttached() != null) {
			// TODO check types
		}
		final @NotNull ConstantTableEntry cte = generatedFunction.getConstTableEntry(i2.getIndex());
		if (cte.type.getAttached() == null) {
			LOG.err("*** ERROR: Null type in CTE " + cte);
		}
		// idte.type may be null, but we still addPotentialType here
		idte.addPotentialType(instruction.getIndex(), cte.type);
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

	public @NotNull ElLog _LOG() {
		return LOG;
	}

	interface IElementProcessor {
		void elementIsNull();

		void hasElement(OS_Element el);
	}

	static class Deduce_Type {
		private final OS_Type                                     type;
		private final DeferredObject<GenType, ResolveError, Void> typePromise = new DeferredObject<>();

		public Deduce_Type(final OS_Type aType) {
			type = aType;
		}

		public void doResolveType(final OS_Module module, final Context aCtx, final DeduceTypes2 aDeduceTypes2) {
			final ElLog LOG = aDeduceTypes2._LOG();

			@NotNull final GenType R = new GenType();
			R.typeName = type;

			try {
				switch (type.getType()) {
				case BUILT_IN:
					if (__doResolveType_BUILT_IN(module, aDeduceTypes2, R)) return;
					break;
				case USER:
					if (__doResolveType_USER(aDeduceTypes2, LOG, R)) return;
					break;
				case USER_CLASS:
					break;
				case FUNCTION:
					break;
				default:
					throw new IllegalStateException("565 Unexpected value: " + type.getType());
				}

				typePromise.resolve(R);
			} catch (final ResolveError e) {
				typePromise.reject(e);
			}
		}

		private boolean __doResolveType_BUILT_IN(final OS_Module module, final DeduceTypes2 aDeduceTypes2, final @NotNull GenType R) throws ResolveError {
			switch (type.getBType()) {
			case SystemInteger: {
				@NotNull final String typeName = type.getBType().name();
				assert typeName.equals("SystemInteger");
				OS_Module prelude = module.prelude;
				if (prelude == null) // README Assume `module' IS prelude
					prelude = module;
				final LookupResultList lrl  = prelude.getContext().lookup(typeName);
				@Nullable OS_Element   best = lrl.chooseBest(null);
				while (!(best instanceof ClassStatement)) {
					if (best instanceof AliasStatement) {
						best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, aDeduceTypes2);
					} else if (OS_Type.isConcreteType(best)) {
						throw new NotImplementedException();
					} else
						throw new NotImplementedException();
				}
				if (best == null) {
					typePromise.reject(new ResolveError(IdentExpression.forString(typeName), lrl));
					return true;
				}
				R.resolved = new OS_UserClassType((ClassStatement) best);
				break;
			}
			case String_: {
				@NotNull final String typeName = type.getBType().name();
				assert typeName.equals("String_");
				OS_Module prelude = module.prelude;
				if (prelude == null) // README Assume `module' IS prelude
					prelude = module;
				final LookupResultList lrl  = prelude.getContext().lookup("ConstString"); // TODO not sure about String
				@Nullable OS_Element   best = lrl.chooseBest(null);
				while (!(best instanceof ClassStatement)) {
					if (best instanceof AliasStatement) {
						best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, aDeduceTypes2);
					} else if (OS_Type.isConcreteType(best)) {
						throw new NotImplementedException();
					} else
						throw new NotImplementedException();
				}
				if (best == null) {
					typePromise.reject(new ResolveError(IdentExpression.forString(typeName), lrl));
					return true;
				}
				R.resolved = new OS_UserClassType((ClassStatement) best);
				break;
			}
			case SystemCharacter: {
				@NotNull final String typeName = type.getBType().name();
				assert typeName.equals("SystemCharacter");
				OS_Module prelude = module.prelude;
				if (prelude == null) { // README Assume `module' IS prelude
					prelude = module;
					assert module != null;
					assert prelude.getContext() != null;
				}
				final LookupResultList lrl  = prelude.getContext().lookup("SystemCharacter");
				@Nullable OS_Element   best = lrl.chooseBest(null);
				while (!(best instanceof ClassStatement)) {
					if (best instanceof AliasStatement) {
						best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, aDeduceTypes2);
					} else if (OS_Type.isConcreteType(best)) {
						throw new NotImplementedException();
					} else
						throw new NotImplementedException();
				}
				if (best == null) {
					typePromise.reject(new ResolveError(IdentExpression.forString(typeName), lrl));
					return true;
				}
				R.resolved = new OS_UserClassType((ClassStatement) best);
				break;
			}
			case Boolean: {
				OS_Module prelude = module.prelude;
				if (prelude == null) // README Assume `module' IS prelude
					prelude = module;
				final LookupResultList     lrl  = prelude.getContext().lookup("Boolean");
				final @Nullable OS_Element best = lrl.chooseBest(null);
				R.resolved = new OS_UserClassType((ClassStatement) best); // TODO might change to Type
				break;
			}
			default:
				throw new IllegalStateException("531 Unexpected value: " + type.getBType());
			}
			return false;
		}

		private boolean __doResolveType_USER(final DeduceTypes2 aDeduceTypes2, final ElLog LOG, final @NotNull GenType R) throws ResolveError {
			final TypeName tn1 = type.getTypeName();
			switch (tn1.kindOfType()) {
			case NORMAL: {
				final Qualident tn = ((NormalTypeName) tn1).getRealName();
				LOG.info("799 [resolving USER type named] " + tn);
				final LookupResultList lrl  = DeduceLookupUtils.lookupExpression(tn, tn1.getContext(), aDeduceTypes2);
				@Nullable OS_Element   best = lrl.chooseBest(null);
				while (best instanceof AliasStatement) {
					best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, aDeduceTypes2);
				}
				if (best == null) {
					if (tn.asSimpleString().equals("Any"))
						/*return*/ R.resolved = new OS_AnyType(); // TODO not a class
					typePromise.reject(new ResolveError(tn1, lrl));
					return true;
				}

				if (best instanceof ClassContext.OS_TypeNameElement) {
					/*return*/
					R.resolved = new OS_GenericTypeNameType((ClassContext.OS_TypeNameElement) best); // TODO not a class
				} else
					R.resolved = new OS_UserClassType((ClassStatement) best);
				break;
			}
			case FUNCTION:
			case GENERIC:
			case TYPE_OF:
				throw new NotImplementedException();
			default:
				throw new IllegalStateException("414 Unexpected value: " + tn1.kindOfType());
			}
			return false;
		}

		public Promise<GenType, ResolveError, Void> getType() {
			return typePromise;
		}
	}

	void implement_calls(final @NotNull BaseGeneratedFunction gf, final @NotNull Context context, final InstructionArgument i2, final @NotNull ProcTableEntry fn1, final int pc) {
		if (gf.deferred_calls.contains(pc)) {
			LOG.err("Call is deferred "/*+gf.getInstruction(pc)*/ + " " + fn1);
			return;
		}
		implement_calls_(gf, context, i2, fn1, pc);
	}

	private void implement_calls_(final @NotNull BaseGeneratedFunction gf,
	                              final @NotNull Context context,
	                              final InstructionArgument i2,
	                              final @NotNull ProcTableEntry pte,
	                              final int pc) {
		final Implement_Calls_ ic = new Implement_Calls_(this, gf, context, i2, pte, pc);
		ic.action();
	}

	interface df_helper_i<T> {
		@Nullable df_helper<T> get(GeneratedContainerNC generatedClass);
	}

	interface df_helper<T> {
		@NotNull Collection<T> collection();

		boolean deduce(T generatedConstructor);
	}

	interface IVariableConnector {
		void connect(VariableTableEntry aVte, String aName);
	}

	boolean lookup_name_calls(final @NotNull Context ctx, final @NotNull String pn, final @NotNull ProcTableEntry pte) {
		final LookupResultList     lrl  = ctx.lookup(pn);
		final @Nullable OS_Element best = lrl.chooseBest(null); // TODO check arity and arg matching
		if (best != null) {
			pte.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(best, null)); // TODO why include if only to be null?
			return true;
		}
		return false;
	}

	public <B> @NotNull PromiseExpectation<B> promiseExpectation(final ExpectationBase base, final String desc) {
		final @NotNull PromiseExpectation<B> promiseExpectation = new PromiseExpectation<>(base, desc);
		expectations.add(promiseExpectation);
		return promiseExpectation;
	}

	public void resolveIdentIA_(@NotNull final Context context, @NotNull final IdentIA identIA, final BaseGeneratedFunction generatedFunction, @NotNull final FoundElement foundElement) {
		@NotNull final Resolve_Ident_IA ria = new Resolve_Ident_IA(new DeduceClient3(this), context, identIA, generatedFunction, foundElement, errSink);
		try {
			ria.action();
		} catch (final ResolveError aE) {
			//throw new RuntimeException(aE);
			System.err.printf("** ResolveError: %s not found!%n", aE.ident.getText());
		}
	}

	public interface ExpectationBase {
		String expectationString();
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

	void forFunction(@NotNull final FunctionInvocation gf, @NotNull final ForFunction forFunction) {
		phase.forFunction(this, gf, forFunction);
	}

	static class ProcessElement {
		static void processElement(final OS_Element el, final IElementProcessor ep) {
			if (el == null)
				ep.elementIsNull();
			else
				ep.hasElement(el);
		}
	}

	public Zero_FuncExprType getZero(final OS_FuncExprType aFuncExprType) {
		if (!(_zeros.containsKey(aFuncExprType)))
			_zeros.put(aFuncExprType, new Zero_FuncExprType(aFuncExprType));

		return (Zero_FuncExprType) _zeros.get(aFuncExprType);
	}

	static class CtorConnector implements IVariableConnector {
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
					break;
				}
			}
		}
	}

	static class NullConnector implements IVariableConnector {
		@Override
		public void connect(final VariableTableEntry aVte, final String aName) {
		}
	}

	public static class DeduceClient1 {
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

		public void genCIForGenType2(final GenType aGenType) {
			aGenType.genCIForGenType2(dt2);
		}

		public void LOG_err(final String string) {
			dt2.LOG.err(string);
		}

		public @NotNull ArrayList<TypeTableEntry> getPotentialTypesVte(final VariableTableEntry aVte) {
			return dt2.getPotentialTypesVte(aVte);
		}
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

		public @NotNull ClassInvocation genCI(@NotNull final GenType genType, final TypeName typeName) {
			return genType.genCI(typeName, deduceTypes2, deduceTypes2.errSink, deduceTypes2.phase);
		}

		public @NotNull ElLog getLOG() {
			return deduceTypes2.LOG;
		}
	}

	static class GenericPart {
		private final ClassStatement classStatement;
		private final TypeName       genericTypeName;

		@Contract(pure = true)
		public GenericPart(final ClassStatement aClassStatement, final TypeName aGenericTypeName) {
			classStatement  = aClassStatement;
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

	class dfhi_constructors implements df_helper_i<GeneratedConstructor> {
		@Override
		public @Nullable df_helper_Constructors get(final GeneratedContainerNC aGeneratedContainerNC) {
			if (aGeneratedContainerNC instanceof GeneratedClass) // TODO namespace constructors
				return new df_helper_Constructors((GeneratedClass) aGeneratedContainerNC);
			else
				return null;
		}
	}

	public static class ClassInvocationMake {
		public static ClassInvocation withGenericPart(final ClassStatement best,
		                                              final String constructorName,
		                                              final NormalTypeName aTyn1,
		                                              final DeduceTypes2 dt2,
		                                              final ErrSink aErrSink) {
			@NotNull final GenericPart genericPart = new GenericPart(best, aTyn1);

			@Nullable final ClassInvocation clsinv = new ClassInvocation(best, constructorName);

			if (genericPart.hasGenericPart()) {
				final @NotNull List<TypeName> gp  = best.getGenericPart();
				final @NotNull TypeNameList   gp2 = genericPart.getGenericPartFromTypeName();

				for (int i = 0; i < gp.size(); i++) {
					final TypeName         typeName = gp2.get(i);
					@NotNull final GenType typeName2;
					try {
						typeName2 = dt2.resolve_type(new OS_UserType(typeName), typeName.getContext());
						// TODO transition to GenType
						clsinv.set(i, gp.get(i), typeName2.resolved);
					} catch (final ResolveError aResolveError) {
//						aResolveError.printStackTrace();
						aErrSink.reportDiagnostic(aResolveError);
					}
				}
			}
			return clsinv;
		}
	}

	class dfhi_functions implements df_helper_i<GeneratedFunction> {
		@Override
		public @NotNull df_helper_Functions get(final GeneratedContainerNC aGeneratedContainerNC) {
			return new df_helper_Functions(aGeneratedContainerNC);
		}
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

	public static class DeduceClient3 {
		private final DeduceTypes2 deduceTypes2;

		public DeduceClient3(final DeduceTypes2 aDeduceTypes2) {
			deduceTypes2 = aDeduceTypes2;
		}

		public ElLog getLOG() {
			return deduceTypes2.LOG;
		}

		public LookupResultList lookupExpression(final IExpression aExp, final Context aContext) throws ResolveError {
			return DeduceLookupUtils.lookupExpression(aExp, aContext, deduceTypes2);
		}

		public GenerateFunctions getGenerateFunctions(final OS_Module aModule) {
			return deduceTypes2.getGenerateFunctions(aModule);
		}

		public void resolveIdentIA2_(final Context aEctx,
		                             final IdentIA aIdentIA,
		                             final @Nullable List<InstructionArgument> aInstructionArgumentList,
		                             final BaseGeneratedFunction aGeneratedFunction,
		                             final FoundElement aFoundElement) {
			deduceTypes2.resolveIdentIA2_(aEctx, aIdentIA, aInstructionArgumentList, aGeneratedFunction, aFoundElement);
		}

		public List<TypeTableEntry> getPotentialTypesVte(final VariableTableEntry aVte) {
			return deduceTypes2.getPotentialTypesVte(aVte);
		}

		public IInvocation getInvocation(final GeneratedFunction aGeneratedFunction) {
			return deduceTypes2.getInvocation(aGeneratedFunction);
		}

		public GenType resolve_type(final OS_Type aType, final Context aContext) throws ResolveError {
			return deduceTypes2.resolve_type(aType, aContext);
		}

		public DeducePhase getPhase() {
			return deduceTypes2.phase;
		}

		public void addJobs(final WorkJob j) {
			final @NotNull WorkList wl = new WorkList();
			wl.addJob(j);
			deduceTypes2.wm.addJobs(wl);
		}

		public IElementHolder newGenericElementHolderWithType(final OS_Element aElement, final TypeName aTypeName) {
			final OS_Type typeName;
			if (aTypeName.isNull())
				typeName = null;
			else
				typeName = new OS_UserType(aTypeName);
			return new GenericElementHolderWithType(aElement, typeName, deduceTypes2);
		}

		public void found_element_for_ite(final BaseGeneratedFunction generatedFunction,
		                                  final @NotNull IdentTableEntry ite,
		                                  final @Nullable OS_Element y,
		                                  final Context ctx) {
			deduceTypes2.found_element_for_ite(generatedFunction, ite, y, ctx);
		}

		public void genCIForGenType2(final @NotNull GenType genType) {
			genType.genCIForGenType2(deduceTypes2);
		}

		public @NotNull FunctionInvocation newFunctionInvocation(final BaseFunctionDef aFunctionDef, final ProcTableEntry aPte, final @NotNull IInvocation aInvocation) {
			return deduceTypes2.newFunctionInvocation(aFunctionDef, aPte, aInvocation, deduceTypes2.phase);
		}

		public OS_Element resolveAlias(final AliasStatement aAliasStatement) {
			try {
				final OS_Element el = DeduceLookupUtils._resolveAlias2(aAliasStatement, deduceTypes2);
				return el;
			} catch (final ResolveError aE) {
				return null;
//				throw new RuntimeException(aE);
			}
		}

		public DeduceTypes2 _dt2() {
			return deduceTypes2;
		}
	}

	public static class OS_SpecialVariable implements OS_Element {
		private final VariableTableEntry                   variableTableEntry;
		private final VariableTableType                    type;
		private final BaseGeneratedFunction                generatedFunction;
		public        DeduceLocalVariable.MemberInvocation memberInvocation;

		public OS_SpecialVariable(final VariableTableEntry aVariableTableEntry, final VariableTableType aType, final BaseGeneratedFunction aGeneratedFunction) {
			variableTableEntry = aVariableTableEntry;
			type               = aType;
			generatedFunction  = aGeneratedFunction;
		}

		@Override
		public void visitGen(final ElElementVisitor visit) {
			throw new IllegalArgumentException("not implemented");
		}

		@Override
		public Context getContext() {
			return generatedFunction.getFD().getContext();
		}

		@Override
		public OS_Element getParent() {
			return generatedFunction.getFD();
		}

		@Nullable
		public IInvocation getInvocation(final DeduceTypes2 aDeduceTypes2) {
			final @Nullable IInvocation aInvocation;
			final OS_SpecialVariable    specialVariable = this;
			assert specialVariable.type == VariableTableType.SELF;
			// first parent is always a function
			switch (DecideElObjectType.getElObjectType(specialVariable.getParent().getParent())) {
				case CLASS:
					final ClassStatement classStatement = (ClassStatement) specialVariable.getParent().getParent();
					aInvocation = aDeduceTypes2.phase.registerClassInvocation(classStatement, null); // TODO generics
//				ClassInvocationMake.withGenericPart(classStatement, null, null, this);
					break;
				case NAMESPACE:
					throw new NotImplementedException(); // README ha! implemented in
				default:
					throw new IllegalArgumentException("Illegal object type for parent");
			}
			return aInvocation;
		}
	}

//	public void register_and_resolve(@NotNull final VariableTableEntry aVte, @NotNull final ClassStatement aKlass) {
//		@Nullable ClassInvocation ci = new ClassInvocation(aKlass, null);
//		ci = phase.registerClassInvocation(ci);
//		ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
//			@Override
//			public void onDone(final GeneratedClass result) {
//				aVte.resolveTypeToClass(result);
//			}
//		});
//	}

	class Resolve_each_typename {

		private final DeducePhase  phase;
		private final DeduceTypes2 dt2;
		private final ErrSink      errSink;

		public Resolve_each_typename(final DeducePhase aPhase, final DeduceTypes2 aDeduceTypes2, final ErrSink aErrSink) {
			phase   = aPhase;
			dt2     = aDeduceTypes2;
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
				LOG.err("288 Failed to resolve type " + aAttached);
				errSink.reportDiagnostic(aResolveError);
			}
		}
	}

	class Lookup_function_on_exit {
		@NotNull
		final WorkList wl = new WorkList();

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

			@Nullable ClassInvocation ci  = fi.getClassInvocation();
			BaseFunctionDef           fd3 = fi.getFunction();
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
					if (ci != pte.getClassInvocation()) {
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
						int                          i  = 0;
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

			final BaseFunctionDef fd2   = fi.getFunction();
			int                   state = 0;

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
					wl.addJob(new WlGenerateDefaultCtor(phase.generatePhase.getGenerateFunctions(module), fi, phase.codeRegistrar));
					break;
				case 2:
					wl.addJob(new WlGenerateCtor(phase.generatePhase.getGenerateFunctions(module), fi, fd2.getNameNode(), phase.codeRegistrar));
					break;
				case 3:
					// README this is a special case to generate constructor
					// TODO should it be GenerateDefaultCtor? (check args size and ctor-name)
					final String constructorName = fi.getClassInvocation().getConstructorName();
					final @NotNull IdentExpression constructorName1 = constructorName != null ? IdentExpression.forString(constructorName) : null;
					wl.addJob(new WlGenerateCtor(phase.generatePhase.getGenerateFunctions(module), fi, constructorName1, phase.codeRegistrar));
					break;
				case 4:
					wl.addJob(new WlGenerateFunction(phase.generatePhase.getGenerateFunctions(module), fi, phase.codeRegistrar));
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

			wl.addJob(new WlGenerateNamespace(phase.generatePhase.getGenerateFunctions(module1), nsi, phase.generatedClasses, phase.codeRegistrar));
			wl.addJob(new WlGenerateFunction(phase.generatePhase.getGenerateFunctions(module1), fi, phase.codeRegistrar));

			wm.addJobs(wl);
		}
	}

	public class PromiseExpectation<B> {

		private final ExpectationBase base;
		private final String          desc;
		private       B               result;
		private       long            counter;
		private       boolean         satisfied;
		private       boolean         _printed;

		public PromiseExpectation(final ExpectationBase aBase, final String aDesc) {
			base = aBase;
			desc = aDesc;
		}

		public void satisfy(final B aResult) {
			result    = aResult;
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

//			LOG.info(String.format("Expectation (%s, %d) set: %s %s", DeduceTypes2.this, counter, desc, base.expectationString()));
		}
	}

	class DeduceClient4 {
		private final DeduceTypes2 deduceTypes2;

		public DeduceClient4(final DeduceTypes2 aDeduceTypes2) {
			deduceTypes2 = aDeduceTypes2;
		}

		public OS_Element lookup(final @NotNull IdentExpression aElement, final @NotNull Context aContext) throws ResolveError {
			return DeduceLookupUtils.lookup(aElement, aContext, deduceTypes2);
		}

		public void reportDiagnostic(final ResolveError aResolveError) {
			deduceTypes2.errSink.reportDiagnostic(aResolveError);
		}

		public @Nullable ClassInvocation registerClassInvocation(final ClassStatement aClassStatement, final String constructorName) {
			return deduceTypes2.phase.registerClassInvocation(aClassStatement, constructorName);
		}

		public FunctionInvocation newFunctionInvocation(final FunctionDef aElement, final ProcTableEntry aPte, final @NotNull IInvocation aInvocation) {
			return deduceTypes2.newFunctionInvocation(aElement, aPte, aInvocation, deduceTypes2.phase);
		}

		public DeferredMemberFunction deferred_member_function(final OS_Element aParent, final IInvocation aInvocation, final FunctionDef aFunctionDef, final FunctionInvocation aFunctionInvocation) {
			return deduceTypes2.deferred_member_function(aParent, aInvocation, aFunctionDef, aFunctionInvocation);
		}

		public @NotNull OS_Module getModule() {
			return module;
		}

		public @NotNull ElLog getLOG() {
			return LOG;
		}

		public @NotNull DeducePhase getPhase() {
			return deduceTypes2.phase;
		}

		public OS_Element _resolveAlias(final AliasStatement aAliasStatement) {
			return DeduceLookupUtils._resolveAlias(aAliasStatement, deduceTypes2);
		}

		public void found_element_for_ite(final BaseGeneratedFunction aGeneratedFunction, final IdentTableEntry aEntry, final OS_Element aE, final Context aCtx) {
			deduceTypes2.found_element_for_ite(aGeneratedFunction, aEntry, aE, aCtx);
		}

		public <T> @NotNull PromiseExpectation<T> promiseExpectation(final BaseGeneratedFunction aGeneratedFunction, final String aName) {
			return deduceTypes2.promiseExpectation(aGeneratedFunction, aName);
		}

		public OS_Element _resolveAlias2(final AliasStatement aAliasStatement) throws ResolveError {
			return DeduceLookupUtils._resolveAlias2(aAliasStatement, deduceTypes2);
		}

		public LookupResultList lookupExpression(final IExpression aExpression, final @NotNull Context aContext) throws ResolveError {
			return DeduceLookupUtils.lookupExpression(aExpression, aContext, deduceTypes2);
		}

		public ClassInvocation registerClassInvocation(final ClassInvocation aCi) {
			return deduceTypes2.phase.registerClassInvocation(aCi);
		}

		public NamespaceInvocation registerNamespaceInvocation(final NamespaceStatement aNamespaceStatement) {
			return deduceTypes2.phase.registerNamespaceInvocation(aNamespaceStatement);
		}

		public void forFunction(final FunctionInvocation aFunctionInvocation, final ForFunction aForFunction) {
			deduceTypes2.forFunction(aFunctionInvocation, aForFunction);
		}

		public void implement_calls(final BaseGeneratedFunction aGeneratedFunction, final Context aParent, final InstructionArgument aArg, final ProcTableEntry aPte, final int aInstructionIndex) {
			deduceTypes2.implement_calls(aGeneratedFunction, aParent, aArg, aPte, aInstructionIndex);
		}

		public void resolveIdentIA_(final @NotNull Context aCtx, final IdentIA aIdentIA, final BaseGeneratedFunction aGeneratedFunction, final FoundElement aFoundElement) {
			deduceTypes2.resolveIdentIA_(aCtx, aIdentIA, aGeneratedFunction, aFoundElement);
		}

		public IInvocation getInvocation(final GeneratedFunction aGeneratedFunction) {
			return deduceTypes2.getInvocation(aGeneratedFunction);
		}

		public ClassInvocation genCI(final GenType aType, final TypeName aGenericTypeName) {
			return aType.genCI(aGenericTypeName, deduceTypes2, deduceTypes2.errSink, deduceTypes2.phase);
		}

		public OS_Type gt(final GenType aType) {
			return deduceTypes2.gt(aType);
		}

		public void register_and_resolve(final VariableTableEntry aVte, final ClassStatement aClassStatement) {
			deduceTypes2.register_and_resolve(aVte, aClassStatement);
		}

		public ErrSink getErrSink() {
			return deduceTypes2.errSink;
		}

		public void onFinish(final Runnable aRunnable) {
			deduceTypes2.onFinish(aRunnable);
		}

		public @NotNull List<TypeTableEntry> getPotentialTypesVte(final GeneratedFunction aGeneratedFunction, final InstructionArgument aVte_ia) {
			return deduceTypes2.getPotentialTypesVte(aGeneratedFunction, aVte_ia);
		}

		public DeduceTypes2 get() {
			return deduceTypes2;
		}

		public GenType resolve_type(final OS_Type aTy, final Context aCtx) throws ResolveError {
			return deduceTypes2.resolve_type(aTy, aCtx);
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
