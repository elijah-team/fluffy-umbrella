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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_UnknownType;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.stages.deduce.declarations.DeferredMemberFunction;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenericElementHolder;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.FnCallArgs;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.InstructionName;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

/**
 * Created 12/12/21 12:30 AM
 */
public class DoAssignCall {
	private final ElLog                      LOG;
	private final DeduceTypes2.DeduceClient4 dc;
	private final BaseGeneratedFunction      generatedFunction;
	private final OS_Module                  module;
	private final ErrSink                    errSink;

	public DoAssignCall(final DeduceTypes2.DeduceClient4 aDeduceClient4, final @NotNull BaseGeneratedFunction aGeneratedFunction) {
		dc                = aDeduceClient4;
		generatedFunction = aGeneratedFunction;
		//
		module  = dc.getModule();
		LOG     = dc.getLOG();
		errSink = dc.getErrSink();
	}

	void do_assign_call(final @NotNull Instruction instruction,
	                    final @NotNull VariableTableEntry vte,
	                    final @NotNull FnCallArgs fca,
	                    final @NotNull Context ctx) {
		final int                     instructionIndex = instruction.getIndex();
		final @NotNull ProcTableEntry pte              = ((ProcIA) fca.getArg(0)).getEntry();
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
						el = dc.lookup((IdentExpression) vte.getResolvedElement(), ctx);
					else
						el = dc.lookup(((VariableStatement) vte.getResolvedElement()).getNameToken(), ctx);
					vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));
				} catch (final ResolveError aResolveError) {
					dc.reportDiagnostic(aResolveError);
					return;
				}
			}
		}

		if (identIA != null) {
//			LOG.info("594 "+identIA.getEntry().getStatus());

			{
				final @NotNull IdentTableEntry ite              = identIA.getEntry();
				final OS_Element               resolved_element = ite.getResolvedElement();

				if (resolved_element != null) {
					final @NotNull OS_Module mod1 = resolved_element.getContext().module();

					if (mod1 != module) {
						if (resolved_element instanceof FunctionDef) {
							final OS_Element parent = resolved_element.getParent();

							final @Nullable DeduceElement target = pte.dpc.target();
							assert target != null;
							IInvocation invocation2 = target.declAnchor().getInvocation();
							if (invocation2 instanceof ClassInvocation)
								invocation2 = dc.getPhase().registerClassInvocation((ClassInvocation) invocation2);

//							final @Nullable ClassInvocation invocation = dc.registerClassInvocation((ClassStatement) parent, null);
							final @NotNull FunctionInvocation fi  = dc.newFunctionInvocation((FunctionDef) resolved_element, pte, invocation2);
							final DeferredMemberFunction      dmf = dc.deferred_member_function(parent, invocation2, (FunctionDef) resolved_element, fi);

							dmf.typeResolved().then(new DoneCallback<GenType>() {
								@Override
								public void onDone(final GenType result) {
									LOG.info("2717 " + dmf.getFunctionDef() + " " + result);
									pte.typeDeferred().resolve(result);
								}
							});
						}
					}
				}
			}

			dc.resolveIdentIA_(ctx, identIA, generatedFunction, new FoundElement(dc.getPhase()) {

				final String xx = generatedFunction.getIdentIAPathNormal(identIA);

				@Override
				public void foundElement(OS_Element e) {
//					LOG.info(String.format("600 %s %s", xx ,e));
//					LOG.info("601 "+identIA.getEntry().getStatus());
					dc.found_element_for_ite(generatedFunction, identIA.getEntry(), e, ctx);

					final OS_Element resolved_element = identIA.getEntry().getResolvedElement();

					while (e instanceof AliasStatement)
						e = dc._resolveAlias((AliasStatement) e);

					assert e == resolved_element || /*HACK*/ resolved_element instanceof AliasStatement || resolved_element == null;

//					set_resolved_element_pte(identIA, e, pte);
					pte.setStatus(BaseTableEntry.Status.KNOWN, new ConstructableElementHolder(e, identIA));
					pte.onFunctionInvocation(new DoneCallback<FunctionInvocation>() {
						@Override
						public void onDone(@NotNull final FunctionInvocation result) {
							result.generateDeferred().done(new DoneCallback<BaseGeneratedFunction>() {
								@Override
								public void onDone(@NotNull final BaseGeneratedFunction bgf) {
									@NotNull final DeduceTypes2.PromiseExpectation<GenType> pe = dc.promiseExpectation(bgf, "Function Result type");
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
			IExpression e = tte.expression;
			if (e == null) continue;
			if (e instanceof SubExpression) e = ((SubExpression) e).getExpression();
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
						final LookupResultList lrl  = dc.lookupExpression(pce.getLeft(), ctx);
						@Nullable OS_Element   best = lrl.chooseBest(null);
						if (best != null) {
							while (best instanceof AliasStatement) {
								best = dc._resolveAlias2((AliasStatement) best);
							}
							if (best instanceof FunctionDef) {
								final OS_Element            parent = best.getParent();
								@Nullable final IInvocation invocation;
								if (parent instanceof NamespaceStatement) {
									invocation = dc.registerNamespaceInvocation((NamespaceStatement) parent);
								} else if (parent instanceof ClassStatement) {
									@NotNull final ClassInvocation ci = new ClassInvocation((ClassStatement) parent, null);
									invocation = dc.registerClassInvocation(ci);
								} else
									throw new NotImplementedException(); // TODO implement me

								dc.forFunction(dc.newFunctionInvocation((FunctionDef) best, pte, invocation), new ForFunction() {
									@Override
									public void typeDecided(@NotNull final GenType aType) {
										tte.setAttached(aType);
//									vte.addPotentialType(instructionIndex, tte);
									}
								});
//							tte.setAttached(new OS_FuncType((FunctionDef) best));

							} else {
								final int y = 2;
								throw new NotImplementedException();
							}
						} else {
							final int y = 2;
							throw new NotImplementedException();
						}
					} catch (final ResolveError aResolveError) {
//					aResolveError.printStackTrace();
//					int y=2;
//					throw new NotImplementedException();
						dc.reportDiagnostic(aResolveError);
						tte.setAttached(new OS_UnknownType(new StatementWrapper(pce.getLeft(), null, null)));
					}
				}
				break;
				case DOT_EXP: {
					final @NotNull DotExpression de = (DotExpression) e;
					try {
						final LookupResultList lrl  = dc.lookupExpression(de.getLeft(), ctx);
						@Nullable OS_Element   best = lrl.chooseBest(null);
						if (best != null) {
							while (best instanceof AliasStatement) {
								best = dc._resolveAlias2((AliasStatement) best);
							}
							if (best instanceof FunctionDef) {
								tte.setAttached(((FunctionDef) best).getOS_Type());
								//vte.addPotentialType(instructionIndex, tte);
							} else if (best instanceof ClassStatement) {
								tte.setAttached(((ClassStatement) best).getOS_Type());
							} else if (best instanceof VariableStatement) {
								final @NotNull VariableStatement    vs     = (VariableStatement) best;
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
				case ADDITION:
				case MODULO:
				case SUBTRACTION:
					final int y = 2;
					tripleo.elijah.util.Stupidity.println_err2("2363");
					break;
				case GET_ITEM: {
					final @NotNull GetItemExpression gie = (GetItemExpression) e;
					do_assign_call_GET_ITEM(gie, tte, generatedFunction, ctx);
					continue;
				}
//			break;
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
					dc.implement_calls(generatedFunction, ctx.getParent(), instruction.getArg(1), pte, instructionIndex);
				}
			} else {
				final int y = 2;
				dc.resolveIdentIA_(ctx, identIA, generatedFunction, new FoundElement(dc.getPhase()) {

					final String x = generatedFunction.getIdentIAPathNormal(identIA);

					@Override
					public void foundElement(final OS_Element el) {
						if (pte.getResolvedElement() == null)
							pte.setResolvedElement(el);
						if (el instanceof FunctionDef) {
							@NotNull final FunctionDef  fd = (FunctionDef) el;
							final @Nullable IInvocation invocation;
							if (fd.getParent() == generatedFunction.getFD().getParent()) {
								invocation = dc.getInvocation((GeneratedFunction) generatedFunction);
							} else {
								if (fd.getParent() instanceof NamespaceStatement) {
									final NamespaceInvocation ni = dc.registerNamespaceInvocation((NamespaceStatement) fd.getParent());
									invocation = ni;
								} else if (fd.getParent() instanceof ClassStatement) {
									final @NotNull ClassStatement classStatement = (ClassStatement) fd.getParent();
									@Nullable ClassInvocation     ci             = new ClassInvocation(classStatement, null);
									final @NotNull List<TypeName> genericPart    = classStatement.getGenericPart();
									if (genericPart.size() > 0) {
										// TODO handle generic parameters somehow (getInvocationFromBacklink?)

									}
									ci         = dc.registerClassInvocation(ci);
									invocation = ci;
								} else
									throw new NotImplementedException();
							}
							dc.forFunction(dc.newFunctionInvocation(fd, pte, invocation), new ForFunction() {
								@Override
								public void typeDecided(@NotNull final GenType aType) {
									if (!vte.typeDeferred_isPending()) {
										if (vte.resolvedType() == null) {
											final @Nullable ClassInvocation ci = dc.genCI(aType, null);
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
									@NotNull final TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, dc.gt(aType), pte.expression, pte);
									vte.addPotentialType(instructionIndex, tte);
								}
							});
						} else if (el instanceof ClassStatement) {
							@NotNull final ClassStatement kl   = (ClassStatement) el;
							@NotNull final OS_Type        type = kl.getOS_Type();
							@NotNull final TypeTableEntry tte  = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, type, pte.expression, pte);
							vte.addPotentialType(instructionIndex, tte);
							vte.setConstructable(pte);

							dc.register_and_resolve(vte, kl);
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

	private void do_assign_call_args_ident(@NotNull final BaseGeneratedFunction generatedFunction,
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
			final Promise<GenType, Void, Void> p    = VTE_TypePromises.do_assign_call_args_ident_vte_promise(aTte, vte1);
			@NotNull final Runnable runnable = new Runnable() {
				boolean isDone;

				@Override
				public void run() {
					if (isDone) return;
					final @NotNull List<TypeTableEntry> ll = dc.getPotentialTypesVte((GeneratedFunction) generatedFunction, vte_ia);
					doLogic(ll);
					isDone = true;
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
								@NotNull final FormalArgListItem fali   = (FormalArgListItem) best;
								final @NotNull OS_Type           osType = new OS_UserType(fali.typeName());
								if (!osType.equals(vte.type.getAttached())) {
									@NotNull final TypeTableEntry tte1 = generatedFunction.newTypeTableEntry(
									  TypeTableEntry.Type.SPECIFIED, osType, fali.getNameToken(), vte1);
									/*if (p.isResolved())
										System.out.printf("890 Already resolved type: vte1.type = %s, gf = %s, tte1 = %s %n", vte1.type, generatedFunction, tte1);
									else*/
									{
										final @Nullable OS_Type attached = tte1.getAttached();
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
								@NotNull final VariableTableEntry   vte2    = generatedFunction.getVarTableEntry(to_int(vte2_ia));
								if (p.isResolved())
									System.out.printf("915 Already resolved type: vte2.type = %s, gf = %s %n", vte1.type, generatedFunction);
								else {
									final GenType gt       = vte1.genType;
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
								  public boolean apply(@org.jetbrains.annotations.Nullable final TypeTableEntry input) {
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
			dc.onFinish(runnable);
		} else {
			final int                      ia   = generatedFunction.addIdentTableEntry(aExpression, ctx);
			@NotNull final IdentTableEntry idte = generatedFunction.getIdentTableEntry(ia);
			idte.addPotentialType(aInstructionIndex, aTte); // TODO DotExpression??
			final int ii = aI;
			idte.onType(dc.getPhase(), new OnType() {
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

	private void do_assign_call_GET_ITEM(@NotNull final GetItemExpression gie, final TypeTableEntry tte, @NotNull final BaseGeneratedFunction generatedFunction, final Context ctx) {
		try {
			final LookupResultList     lrl  = dc.lookupExpression(gie.getLeft(), ctx);
			final @Nullable OS_Element best = lrl.chooseBest(null);
			if (best != null) {
				if (best instanceof VariableStatement) { // TODO what about alias?
					@NotNull final VariableStatement    vs     = (VariableStatement) best;
					final String                        s      = vs.getName();
					@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(s);
					if (vte_ia != null) {
						@NotNull final VariableTableEntry vte1 = generatedFunction.getVarTableEntry(to_int(vte_ia));
						throw new NotImplementedException();
					} else {
						final IdentTableEntry idte = generatedFunction.getIdentTableEntryFor(vs.getNameToken());
						assert idte != null;
						if (idte.type == null) {
							final IdentIA identIA = new IdentIA(idte.getIndex(), generatedFunction);
							dc.resolveIdentIA_(ctx, identIA, generatedFunction, new NullFoundElement());
						}
						@Nullable final OS_Type ty;
						if (idte.type == null) ty = null;
						else ty = idte.type.getAttached();
						idte.onType(dc.getPhase(), new OnType() {
							@Override
							public void typeDeduced(final @NotNull OS_Type ty) {
								assert ty != null;
								@NotNull GenType rtype = null;
								try {
									rtype = dc.resolve_type(ty, ctx);
								} catch (final ResolveError resolveError) {
									//								resolveError.printStackTrace();
									errSink.reportError("Cant resolve " + ty); // TODO print better diagnostic
									return;
								}
								if (rtype.resolved != null && rtype.resolved.getType() == OS_Type.Type.USER_CLASS) {
									final LookupResultList     lrl2  = rtype.resolved.getClassOf().getContext().lookup("__getitem__");
									@Nullable final OS_Element best2 = lrl2.chooseBest(null);
									if (best2 != null) {
										if (best2 instanceof FunctionDef) {
											@NotNull final FunctionDef     fd         = (FunctionDef) best2;
											@Nullable final ProcTableEntry pte        = null;
											final IInvocation              invocation = dc.getInvocation((GeneratedFunction) generatedFunction);
											dc.forFunction(dc.newFunctionInvocation(fd, pte, invocation), new ForFunction() {
												@Override
												public void typeDecided(final @NotNull GenType aType) {
													assert fd == generatedFunction.getFD();
													//
													if (idte.type == null) {
														idte.makeType(generatedFunction, TypeTableEntry.Type.TRANSIENT, dc.gt(aType));  // TODO expression?
													} else
														idte.type.setAttached(dc.gt(aType));
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
							  TypeTableEntry.Type.SPECIFIED, new OS_UserType(vs.typeName()), vs.getNameToken());
							idte.type = tte3;
//							ty = idte.type.getAttached();
						}
					}

					//				tte.attached = new OS_FuncType((FunctionDef) best); // TODO: what is this??
					//vte.addPotentialType(instructionIndex, tte);
				} else if (best instanceof FormalArgListItem) {
					final @Nullable FormalArgListItem   fali   = (FormalArgListItem) best;
					final String                        s      = fali.name();
					@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(s);
					if (vte_ia != null) {
						@NotNull final VariableTableEntry vte2 = generatedFunction.getVarTableEntry(to_int(vte_ia));

//						final @Nullable OS_Type ty2 = vte2.type.attached;
						VTE_TypePromises.getItemFali(generatedFunction, ctx, vte2, dc.get());
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

	class NullFoundElement extends FoundElement {
		public NullFoundElement() {
			super(dc.getPhase());
		}

		@Override
		public void foundElement(final OS_Element e) {
		}

		@Override
		public void noFoundElement() {

		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
