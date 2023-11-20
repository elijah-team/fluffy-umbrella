package tripleo.elijah.stages.deduce;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.lang.types.OS_UserClassType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenericElementHolder;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.FnCallArgs;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.InstructionName;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;

class Do_assign_call {
	private final DeduceTypes2 deduceTypes2;

	public Do_assign_call(final DeduceTypes2 aDeduceTypes2) {
		deduceTypes2 = aDeduceTypes2;
	}

	public void do_assign_call(final @NotNull BaseGeneratedFunction generatedFunction,
	                           final @NotNull Context ctx,
	                           final @NotNull VariableTableEntry vte,
	                           final @NotNull FnCallArgs fca,
	                           final @NotNull Instruction instruction) {
		final int                     instructionIndex = instruction.getIndex();
		final @NotNull ProcTableEntry pte              = generatedFunction.getProcTableEntry(DeduceTypes2.to_int(fca.getArg(0)));
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
						el = DeduceLookupUtils.lookup((IdentExpression) vte.getResolvedElement(), ctx, deduceTypes2);
					else
						el = DeduceLookupUtils.lookup(((VariableStatement) vte.getResolvedElement()).getNameToken(), ctx, deduceTypes2);
					vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));
				} catch (final ResolveError aResolveError) {
					deduceTypes2.errSink.reportDiagnostic(aResolveError);
					return;
				}
			}
		}

		if (identIA != null) {
//			LOG.info("594 "+identIA.getEntry().getStatus());

			deduceTypes2.resolveIdentIA_(ctx, identIA, generatedFunction, new FoundElement(deduceTypes2.phase) {

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
									@NotNull final DeduceTypes2.PromiseExpectation<GenType> pe = deduceTypes2.promiseExpectation(bgf, "Function Result type");
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
					deduceTypes2.LOG.info("1005 Can't find element for " + xx);
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
					deduceTypes2.do_assign_call_args_ident(generatedFunction, ctx, vte, instructionIndex, pte, i, tte, (IdentExpression) e);
					break;
				case PROCEDURE_CALL: {
					final @NotNull ProcedureCallExpression pce = (ProcedureCallExpression) e;
					try {
						final LookupResultList lrl  = DeduceLookupUtils.lookupExpression(pce.getLeft(), ctx, deduceTypes2);
						@Nullable OS_Element   best = lrl.chooseBest(null);
						if (best != null) {
							while (best instanceof AliasStatement) {
								best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, deduceTypes2);
							}
							if (best instanceof FunctionDef) {
								final OS_Element            parent = best.getParent();
								@Nullable final IInvocation invocation;
								if (parent instanceof NamespaceStatement) {
									invocation = deduceTypes2.phase.registerNamespaceInvocation((NamespaceStatement) parent);
								} else if (parent instanceof ClassStatement) {
									@NotNull final ClassInvocation ci = new ClassInvocation((ClassStatement) parent, null);
									invocation = deduceTypes2.phase.registerClassInvocation(ci);
								} else
									throw new NotImplementedException(); // TODO implement me

								deduceTypes2.forFunction(deduceTypes2.newFunctionInvocation((FunctionDef) best, pte, invocation, deduceTypes2.phase), new ForFunction() {
									@Override
									public void typeDecided(@NotNull final GenType aType) {
										tte.setAttached(deduceTypes2.gt(aType)); // TODO stop setting attached!
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
						final LookupResultList lrl  = DeduceLookupUtils.lookupExpression(de.getLeft(), ctx, deduceTypes2);
						@Nullable OS_Element   best = lrl.chooseBest(null);
						if (best != null) {
							while (best instanceof AliasStatement) {
								best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, deduceTypes2);
							}
							if (best instanceof FunctionDef) {
								tte.setAttached(new OS_FuncType((FunctionDef) best));
								//vte.addPotentialType(instructionIndex, tte);
							} else if (best instanceof ClassStatement) {
								tte.setAttached(new OS_UserClassType((ClassStatement) best));
							} else if (best instanceof VariableStatement) {
								final @NotNull VariableStatement    vs     = (VariableStatement) best;
								@Nullable final InstructionArgument vte_ia = generatedFunction.vte_lookup(vs.getName());
								final TypeTableEntry                tte1   = ((IntegerIA) vte_ia).getEntry().type;
								tte.setAttached(tte1.getAttached());
							} else {
								final int y = 2;
								deduceTypes2.LOG.err(best.getClass().getName());
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
					deduceTypes2.do_assign_call_GET_ITEM(gie, tte, generatedFunction, ctx);
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
						deduceTypes2.errSink.reportError("Cant resolve " + text);
					}
				} else {
					deduceTypes2.implement_calls(generatedFunction, ctx.getParent(), instruction.getArg(1), pte, instructionIndex);
				}
			} else {
				final int y = 2;
				deduceTypes2.resolveIdentIA_(ctx, identIA, generatedFunction, new FoundElement(deduceTypes2.phase) {

					final String x = generatedFunction.getIdentIAPathNormal(identIA);

					@Override
					public void foundElement(final OS_Element el) {
						if (pte.getResolvedElement() == null)
							pte.setResolvedElement(el);
						if (el instanceof FunctionDef) {
							@NotNull final FunctionDef  fd = (FunctionDef) el;
							final @Nullable IInvocation invocation;
							if (fd.getParent() == generatedFunction.getFD().getParent()) {
								invocation = deduceTypes2.getInvocation((GeneratedFunction) generatedFunction);
							} else {
								if (fd.getParent() instanceof NamespaceStatement) {
									final NamespaceInvocation ni = deduceTypes2.phase.registerNamespaceInvocation((NamespaceStatement) fd.getParent());
									invocation = ni;
								} else if (fd.getParent() instanceof ClassStatement) {
									final @NotNull ClassStatement classStatement = (ClassStatement) fd.getParent();
									@Nullable ClassInvocation     ci             = new ClassInvocation(classStatement, null);
									final @NotNull List<TypeName> genericPart    = classStatement.getGenericPart();
									if (genericPart.size() > 0) {
										// TODO handle generic parameters somehow (getInvocationFromBacklink?)

									}
									ci         = deduceTypes2.phase.registerClassInvocation(ci);
									invocation = ci;
								} else
									throw new NotImplementedException();
							}
							deduceTypes2.forFunction(deduceTypes2.newFunctionInvocation(fd, pte, invocation, deduceTypes2.phase), new ForFunction() {
								@Override
								public void typeDecided(@NotNull final GenType aType) {
									if (!vte.typeDeferred_isPending()) {
										if (vte.resolvedType() == null) {
											final @Nullable ClassInvocation ci = deduceTypes2.genCI(aType, null);
											vte.type.genTypeCI(ci);
											ci.resolvePromise().then(new DoneCallback<GeneratedClass>() {
												@Override
												public void onDone(final GeneratedClass result) {
													vte.resolveTypeToClass(result);
												}
											});
										}
										deduceTypes2.LOG.err("2041 type already found " + vte);
										return; // type already found
									}
									// I'm not sure if below is ever called
									@NotNull final TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, deduceTypes2.gt(aType), pte.expression, pte);
									vte.addPotentialType(instructionIndex, tte);
								}
							});
						} else if (el instanceof ClassStatement) {
							@NotNull final ClassStatement kl   = (ClassStatement) el;
							@NotNull final OS_Type        type = new OS_UserClassType(kl);
							@NotNull final TypeTableEntry tte  = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, type, pte.expression, pte);
							vte.addPotentialType(instructionIndex, tte);
							vte.setConstructable(pte);

							deduceTypes2.register_and_resolve(vte, kl);
						} else {
							deduceTypes2.LOG.err("7890 " + el.getClass().getName());
						}
					}

					@Override
					public void noFoundElement() {
//						deduceTypes2.LOG.err("IdentIA path cannot be resolved " + x);
					}
				});
			}
		}
	}
}
