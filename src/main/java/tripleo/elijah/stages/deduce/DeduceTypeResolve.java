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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;

import java.util.Map;

/**
 * Created 11/18/21 12:02 PM
 */
public class DeduceTypeResolve {
	private final BaseTableEntry bte;
	private BaseTableEntry backlink;
	private DeferredObject<GenType, ResolveError, Void> typeResolution = new DeferredObject<GenType, ResolveError, Void>();

	public DeduceTypeResolve(BaseTableEntry aBte) {
		bte = aBte;
		if (bte instanceof IdentTableEntry) {
			((IdentTableEntry) bte).backlinkSet().then(new DoneCallback<InstructionArgument>() {
				@Override
				public void onDone(final InstructionArgument backlink0) {
					if (backlink0 instanceof IdentIA) {
						backlink = ((IdentIA)backlink0).getEntry();
						setBacklinkCallback();
					} else if (backlink0 instanceof IntegerIA) {
						backlink = ((IntegerIA)backlink0).getEntry();
						setBacklinkCallback();
					} else if (backlink0 instanceof ProcIA) {
						backlink = ((ProcIA)backlink0).getEntry();
						setBacklinkCallback();
					} else
						backlink = null;
				}
			});
		} else if (bte instanceof VariableTableEntry) {
			backlink = null;
		} else if (bte instanceof ProcTableEntry) {
			backlink = null;
		} else
			throw new IllegalStateException();

		if (backlink != null) {
		} else {
			bte.addStatusListener(new BaseTableEntry.StatusListener() {
				@Override
				public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
					if (newStatus != BaseTableEntry.Status.KNOWN) return;

					GenType genType = new GenType();
					eh.getElement().visitGen(new AbstractCodeGen() {
						@Override
						public void addClass(final ClassStatement klass) {
							genType.resolved = klass.getOS_Type();
						}

						@Override
						public void visitFunctionDef(final FunctionDef aFunctionDef) {
							genType.resolved = aFunctionDef.getOS_Type();
						}

						@Override
						public void visitVariableStatement(final VariableStatement variableStatement) {
//							final VariableStatement variableStatement = (VariableStatement) eh.getElement();
							if (variableStatement.typeName() instanceof NormalTypeName) {
								final NormalTypeName normalTypeName = (NormalTypeName) variableStatement.typeName();
								if (normalTypeName.getGenericPart() != null) {
									final TypeNameList genericPart = normalTypeName.getGenericPart();
									if (eh instanceof GenericElementHolderWithType) {
										final GenericElementHolderWithType eh1 = (GenericElementHolderWithType) eh;
										final DeduceTypes2 dt2 = eh1.getDeduceTypes2();
										final OS_Type type = eh1.getType();

										genType.nonGenericTypeName = normalTypeName;

										assert normalTypeName == type.getTypeName();

										OS_Type typeName = new OS_Type(normalTypeName);
										try {
											final @NotNull GenType resolved = dt2.resolve_type(typeName, variableStatement.getContext());
											genType.resolved = resolved.resolved;
										} catch (ResolveError aResolveError) {
											aResolveError.printStackTrace();
											assert false;
										}
									} else
										genType.nonGenericTypeName = normalTypeName;
								} else {
									if (!normalTypeName.isNull()) {
										if (eh instanceof GenericElementHolderWithType) {
											final GenericElementHolderWithType eh1 = (GenericElementHolderWithType) eh;
											final DeduceTypes2 dt2 = eh1.getDeduceTypes2();
											final OS_Type type = eh1.getType();

											genType.typeName = new OS_Type(normalTypeName);
											try {
												final @NotNull GenType resolved = dt2.resolve_type(genType.typeName, variableStatement.getContext());
												if (resolved.resolved.getType() == OS_Type.Type.GENERIC_TYPENAME) {
													backlink.typeResolvePromise().then(new DoneCallback<GenType>() {
														@Override
														public void onDone(final GenType result_gt) {
															((Constructable) backlink).constructablePromise().then(new DoneCallback<ProcTableEntry>() {
																@Override
																public void onDone(final ProcTableEntry result_pte) {
																	final ClassInvocation ci = result_pte.getClassInvocation();
																	assert ci != null;
																	final @Nullable Map<TypeName, OS_Type> gp = ci.genericPart;
																	final TypeName sch = resolved.typeName.getTypeName();
																	for (Map.Entry<TypeName, OS_Type> entrySet : gp.entrySet()) {
																		if (entrySet.getKey().equals(sch)) {
																			genType.resolved = entrySet.getValue();
																			break;
																		}
																	}
																}
															});
														}
													});
												} else {
													genType.resolved = resolved.resolved;
												}
											} catch (ResolveError aResolveError) {
												aResolveError.printStackTrace();
												assert false;
											}
										} else
											genType.typeName = new OS_Type(normalTypeName);
									}
								}
							} else {
								throw new IllegalStateException();
							}
						}

						@Override
						public void visitFormalArgListItem(final FormalArgListItem aFormalArgListItem) {
							final OS_Type attached;
							if (bte instanceof VariableTableEntry)
								attached = ((VariableTableEntry) bte).type.getAttached();
							else if (bte instanceof IdentTableEntry) {
								final IdentTableEntry identTableEntry = (IdentTableEntry) DeduceTypeResolve.this.bte;
								if (identTableEntry.type == null)
									return;
								attached = identTableEntry.type.getAttached();
							} else
								throw new IllegalStateException("invalid entry (bte) " + bte);

							if (attached != null)
								System.err.println(
										String.format("** FormalArgListItem %s attached is not null. Type is %s. Points to %s",
												aFormalArgListItem.name(), aFormalArgListItem.typeName(), attached));
							else
								System.err.println(
										String.format("** FormalArgListItem %s attached is null. Type is %s.",
												aFormalArgListItem.name(), aFormalArgListItem.typeName()));
						}

						@Override
						public void visitAliasStatement(final AliasStatement aAliasStatement) {
							System.err.println(String.format("** AliasStatement %s points to %s", aAliasStatement.name(), aAliasStatement.getExpression()));
						}

						@Override
						public void visitDefFunction(final DefFunctionDef aDefFunctionDef) {
							System.err.println(String.format("** DefFunctionDef %s is %s", aDefFunctionDef.name(), ((StatementWrapper) aDefFunctionDef.getItems().iterator().next()).getExpr()));
						}

						@Override
						public void visitIdentExpression(final IdentExpression aIdentExpression) {
							if (eh instanceof GenericElementHolderWithIntegerIA) {
								final GenericElementHolderWithIntegerIA eh1 = (GenericElementHolderWithIntegerIA) eh;
								final IntegerIA integerIA = eh1.getIntegerIA();
								final @NotNull VariableTableEntry variableTableEntry = integerIA.getEntry();
								assert variableTableEntry == bte;
								variableTableEntry.typeResolvePromise().then(
										new DoneCallback<GenType>() {
											@Override
											public void onDone(final GenType result) {
												genType.copy(result); // TODO genType = result?? because we want updates...
											}
										});
							} else {
								final DeduceLocalVariable dlv = ((VariableTableEntry) bte).dlv;
								int y=2;
							}
						}

						@Override
						public void visitPropertyStatement(final PropertyStatement aPropertyStatement) {
							genType.typeName = new OS_Type(aPropertyStatement.getTypeName());
							// TODO resolve??
						}

						@Override
						public void visitConstructorDef(final ConstructorDef aConstructorDef) {
							int y=2;
						}

						@Override
						public void visitMC1(final MatchConditional.MC1 aMC1) {
							if (aMC1 instanceof MatchConditional.MatchArm_TypeMatch) {
								final MatchConditional.MatchArm_TypeMatch typeMatch = (MatchConditional.MatchArm_TypeMatch) aMC1;
								int yy=2;
							}
						}

						@Override
						public void defaultAction(final OS_Element anElement) {
							System.err.println("158 "+anElement);
							throw new IllegalStateException();
						}

					});

					if (!typeResolution.isPending()) {
						int y=2;
					} else {
						if (!genType.isNull())
							typeResolution.resolve(genType);
					}
				}
			});
		}


	}

	protected void setBacklinkCallback() {
		backlink.addStatusListener(new BaseTableEntry.StatusListener() {
			@Override
			public void onChange(final IElementHolder eh, final BaseTableEntry.Status newStatus) {
				if (newStatus != BaseTableEntry.Status.KNOWN) return;

				if (backlink instanceof IdentTableEntry){
					final IdentTableEntry identTableEntry = (IdentTableEntry) backlink;
					identTableEntry.typeResolvePromise().done(new DoneCallback<GenType>() {
						@Override
						public void onDone(final GenType result) {
							identTableEntry.type.setAttached(result);
						}
					});
				} else if (backlink instanceof VariableTableEntry) {
					final VariableTableEntry variableTableEntry = (VariableTableEntry) backlink;
					variableTableEntry.typeResolvePromise().done(new DoneCallback<GenType>() {
						@Override
						public void onDone(final GenType result) {
							if (eh instanceof Resolve_Ident_IA.GenericElementHolderWithDC) {
								final Resolve_Ident_IA.GenericElementHolderWithDC eh1 = (Resolve_Ident_IA.GenericElementHolderWithDC) eh;
								final DeduceTypes2.DeduceClient3 dc = eh1.getDC();
								dc.genCIForGenType2(result);
							}
							// maybe set something in ci to INHERITED, but thats what DeduceProcCall is for
							if (eh.getElement() instanceof FunctionDef) {
								if (result.node instanceof GeneratedClass) {
									final GeneratedClass generatedClass = (GeneratedClass) result.node;
									generatedClass.functionMapDeferred((FunctionDef) eh.getElement(), new FunctionMapDeferred() {
										@Override
										public void onNotify(final GeneratedFunction aGeneratedFunction) {
											result.node = aGeneratedFunction;
										}
									});
								}
							}
							variableTableEntry.type.setAttached(result);
						}
					});
				} else if (backlink instanceof ProcTableEntry) {
					final ProcTableEntry procTableEntry = (ProcTableEntry) backlink;
					procTableEntry.typeResolvePromise().done(new DoneCallback<GenType>() {
						@Override
						public void onDone(final GenType result) {
//							procTableEntry.type.setAttached(result);
							int y=2;
							final ClassStatement classStatement = result.resolved.getClassOf();

							assert bte instanceof IdentTableEntry;

							final String text = ((IdentTableEntry) bte).getIdent().getText();
							final LookupResultList lrl = classStatement.getContext().lookup(text);
							final @Nullable OS_Element e = lrl.chooseBest(null);

//							procTableEntry.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(classStatement));  // infinite recursion
							final ProcTableEntry callablePTE = ((IdentTableEntry) bte).getCallablePTE();
							if (callablePTE != null && e != null) {
								assert e instanceof BaseFunctionDef;  // sholud fail for constructor and destructor
								callablePTE.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(e));
							}
						}
					});
				}
			}
		});

	}
	public Promise<GenType, ResolveError, Void> typeResolution() {
		return typeResolution.promise();
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
