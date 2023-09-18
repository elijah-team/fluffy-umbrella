package tripleo.elijah.stages.deduce;

import java.util.List;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.LookupResultList;
import tripleo.elijah.lang.NormalTypeName;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.lang.TypeNameList;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaConstructor;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.IElementHolder;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.IdentTableEntry.ITE_Resolver_Result;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.util.Operation;

class Unnamed_ITE_Resolver1 implements ITE_Resolver {
	public class FoundParent implements BaseTableEntry.StatusListener {
		interface DT_Rule {
			String ruleName();
		}
		record UIR1_Env() {
		}
		class UIR1_Rule implements DT_Rule {
			private final OS_Type aTy;

			UIR1_Rule(final OS_Type aATy, final @NotNull VariableStatement vs) {
				@NotNull TypeName typ = vs.typeName();

				var aTypeName = vs.typeName();
				assert (!aTypeName.isNull());

				aTy = aATy;
			}

			public @NotNull Operation<GenType> product() {
				GenType ty2;
				assert aTy.getTypeName() != null;
				try {
					ty2 = dt2.resolve_type(aTy, aTy.getTypeName().getContext());
					return Operation.success(ty2);
				} catch (ResolveError aE) {
					return Operation.failure(aE);
				}
			}

			@Override
			public @NotNull String ruleName() {
				return "Unnamed_ITE_Resolver1::getTY2";
			}
		}
		private final BaseTableEntry  bte;

		private final Context         ctx;

		private final BaseEvaFunction generatedFunction;

		private final IdentTableEntry ite;

		@Contract(pure = true)
		public FoundParent(BaseTableEntry aBte, IdentTableEntry aIte, Context aCtx, BaseEvaFunction aGeneratedFunction) {
			bte               = aBte;
			ite               = aIte;
			ctx               = aCtx;
			generatedFunction = aGeneratedFunction;
		}

		private @Nullable GenType getTY2(final @NotNull VariableStatement aVariableStatement, @NotNull TypeName aTypeName, @NotNull OS_Type aTy) throws ResolveError {
			if (aTy.getType() != OS_Type.Type.USER) {
				assert false;
				@NotNull GenType genType = _inj().new_GenTypeImpl();
				genType.set(aTy);
				return genType;
			}

			if (!aTypeName.isNull()) {
				final UIR1_Rule r = new UIR1_Rule(aTy, aVariableStatement);
				assert r.product().mode() == Mode.SUCCESS;
				return r.product().success();
			}

			@Nullable GenType ty2 = null;
			if (bte instanceof VariableTableEntry vte) {
				final TypeTableEntry vte_tte = vte.getType();
				ty2 = _inj().new_GenTypeImpl();
				ty2.copy(vte_tte.genType);

				// TODO why not just return this ^^? instead of copying

/*
				final OS_Type        attached = vte_tte.getAttached();
				if (attached == null) {
					type_is_null_and_attached_is_null_vte();
					// ty2 will probably be null here
				} else {
					ty2 = _inj().new_GenTypeImpl();
					ty2.set(attached);
				}
*/
			} else if (bte instanceof IdentTableEntry) {
				final TypeTableEntry tte = ((IdentTableEntry) bte).getType();
				if (tte != null) {
					final OS_Type attached = tte.getAttached();

					if (attached == null) {
						type_is_null_and_attached_is_null_ite((IdentTableEntry) bte);
						// ty2 will be null here
					} else {
						ty2 = _inj().new_GenTypeImpl();
						ty2.set(attached);
					}
				}
			}

			return ty2;
		}

		@Override
		public void onChange(@NotNull IElementHolder eh, BaseTableEntry.Status newStatus) {
			if (newStatus == BaseTableEntry.Status.KNOWN) {
				if (bte instanceof final @NotNull VariableTableEntry vte) {
					onChangeVTE(vte);
				} else if (bte instanceof final @NotNull ProcTableEntry pte) {
					onChangePTE(pte);
				} else if (bte instanceof final @NotNull IdentTableEntry ite) {
					onChangeITE(ite);
				}
				postOnChange(eh);
			}
		}

		private void onChangeITE(@NotNull IdentTableEntry identTableEntry) {
			final DT_Env         env      = _inj().new_DT_Env(dt2.LOG, dt2._errSink(), dt2.central());
			final TypeTableEntry ite_type = identTableEntry.getType();

			if (ite_type != null) {
				final OS_Type ty = ite_type.getAttached();

				@Nullable OS_Element ele2 = null;

				try {
					if (ty.getType() == OS_Type.Type.USER) {
						@NotNull GenType ty2 = dt2.resolve_type(ty, ty.getTypeName().getContext());
						OS_Element       ele;
						if (ite_type.genType.getResolved() == null) {
							if (ty2.getResolved().getType() == OS_Type.Type.USER_CLASS) {
								ite_type.genType.copy(ty2);
							}
						}
						ele = ty2.getResolved().getElement();
						LookupResultList lrl = DeduceLookupUtils.lookupExpression(this.ite.getIdent(), ele.getContext(), dt2);
						ele2 = lrl.chooseBest(null);
					} else
						ele2 = ty.getClassOf(); // TODO might fail later (use getElement?)

					@Nullable LookupResultList lrl = null;

					lrl = DeduceLookupUtils.lookupExpression(this.ite.getIdent(), ele2.getContext(), dt2);
					@Nullable OS_Element best = lrl.chooseBest(null);
					// README commented out because only firing for dir.listFiles, and we always use `best'
//					if (best != ele2) LOG.err(String.format("2824 Divergent for %s, %s and %s", identTableEntry, best, ele2));;
					this.ite.setStatus(BaseTableEntry.Status.KNOWN, dt2._inj().new_GenericElementHolder(best));
				} catch (ResolveError aResolveError) {
					aResolveError.printStackTrace();
					dt2._errSink().reportDiagnostic(aResolveError);
				}
			} else {
				if (!identTableEntry.isFefi()) {

					final Found_Element_For_ITE fefi = dt2._inj().new_Found_Element_For_ITE(generatedFunction, ctx, env, dt2._inj().new_DeduceClient1(dt2));
					fefi.action(identTableEntry);
					identTableEntry.setFefi(true);
					identTableEntry.onFefiDone(new DoneCallback<GenType>() {
						@Override
						public void onDone(final @NotNull GenType result) {
							LookupResultList lrl = null;
							OS_Element       ele2;
							try {
								lrl  = DeduceLookupUtils.lookupExpression(ite.getIdent(), result.getResolved().getClassOf().getContext(), dt2);
								ele2 = lrl.chooseBest(null);

								if (ele2 != null) {
									ite.setStatus(BaseTableEntry.Status.KNOWN, dt2._inj().new_GenericElementHolder(ele2));
									ite.resolveTypeToClass(result.getNode());

									if (ite.getCallablePTE() != null) {
										final @Nullable ProcTableEntry    pte        = ite.getCallablePTE();
										final @NotNull IInvocation        invocation = result.getCi();
										final @NotNull FunctionInvocation fi         = dt2.newFunctionInvocation((BaseFunctionDef) ele2, pte, invocation, dt2.phase);

										generatedFunction.addDependentFunction(fi);
									}
								}
							} catch (ResolveError aResolveError) {
								aResolveError.printStackTrace();
							}
						}
					});
				}
				// TODO we want to setStatus but have no USER or USER_CLASS to perform lookup with
			}
		}

		private void onChangePTE(@NotNull ProcTableEntry aPte) {
			if (aPte.getStatus() == BaseTableEntry.Status.KNOWN) { // TODO might be obvious
				if (aPte.getFunctionInvocation() != null) {
					FunctionInvocation fi = aPte.getFunctionInvocation();
					BaseFunctionDef        fd = fi.getFunction();
					if (fd instanceof ConstructorDef) {
						fi.generateDeferred().done(new DoneCallback<BaseEvaFunction>() {
							@Override
							public void onDone(BaseEvaFunction result) {
								@NotNull EvaConstructor constructorDef = (EvaConstructor) result;

								@NotNull BaseFunctionDef ele = constructorDef.getFD();

								try {
									LookupResultList     lrl  = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), dt2);
									@Nullable OS_Element best = lrl.chooseBest(null);
									assert best != null;
									ite.setStatus(BaseTableEntry.Status.KNOWN, _inj().new_GenericElementHolder(best));
								} catch (ResolveError aResolveError) {
									aResolveError.printStackTrace();
									dt2._errSink().reportDiagnostic(aResolveError);
								}
							}
						});
					}
				} else
					throw new NotImplementedException();
			} else {
				dt2.LOG.info("1621");
				@Nullable LookupResultList lrl = null;
				try {
					lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ctx, dt2);
					@Nullable OS_Element best = lrl.chooseBest(null);
					assert best != null;
					ite.setResolvedElement(best);
					dt2.found_element_for_ite(null, ite, best, ctx, dt2.central());
//						ite.setStatus(BaseTableEntry.Status.KNOWN, best);
				} catch (ResolveError aResolveError) {
					aResolveError.printStackTrace();
				}
			}
		}

		private void onChangeVTE(@NotNull VariableTableEntry vte) {
			@NotNull List<TypeTableEntry> pot = dt2.getPotentialTypesVte(vte);
			if (vte.getStatus() == BaseTableEntry.Status.KNOWN && vte.getType().getAttached() != null && vte.getResolvedElement() != null) {

				final OS_Type ty = vte.getType().getAttached();

				@Nullable OS_Element ele2 = null;

				try {
					if (ty.getType() == OS_Type.Type.USER) {
						@NotNull GenType ty2 = dt2.resolve_type(ty, ty.getTypeName().getContext());
						OS_Element       ele;
						if (vte.getType().genType.getResolved() == null) {
							if (ty2.getResolved().getType() == OS_Type.Type.USER_CLASS) {
								vte.getType().genType.copy(ty2);
							}
						}
						ele = ty2.getResolved().getElement();
						LookupResultList lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), dt2);
						ele2 = lrl.chooseBest(null);
					} else
						ele2 = ty.getElement();

					if (ty instanceof OS_FuncType) {
						vte.typePromise().then(new DoneCallback<GenType>() {
							@Override
							public void onDone(final @NotNull GenType result) {
								OS_Element                 ele3 = result.getResolved().getClassOf();
								@Nullable LookupResultList lrl  = null;

								try {
									lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele3.getContext(), dt2);

									@Nullable OS_Element best = lrl.chooseBest(null);
									// README commented out because only firing for dir.listFiles, and we always use `best'
									//if (best != ele2) LOG.err(String.format("2824 Divergent for %s, %s and %s", ite, best, ele2));;
									ite.setStatus(BaseTableEntry.Status.KNOWN, _inj().new_GenericElementHolderWithType(best, ty, dt2));
								} catch (ResolveError aResolveError) {
									aResolveError.printStackTrace();
									dt2._errSink().reportDiagnostic(aResolveError);
								}
							}
						});
					} else {
						@Nullable LookupResultList lrl  = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele2.getContext(), dt2);
						@Nullable OS_Element       best = lrl.chooseBest(null);
						// README commented out because only firing for dir.listFiles, and we always use `best'
//					if (best != ele2) LOG.err(String.format("2824 Divergent for %s, %s and %s", ite, best, ele2));;
						ite.setStatus(BaseTableEntry.Status.KNOWN, _inj().new_GenericElementHolderWithType(best, ty, dt2));
					}
				} catch (ResolveError aResolveError) {
					aResolveError.printStackTrace();
					dt2._errSink().reportDiagnostic(aResolveError);
				}
			} else if (pot.size() == 1) {
				TypeTableEntry    tte = pot.get(0);
				@Nullable OS_Type ty  = tte.getAttached();
				if (ty != null) {
					switch (ty.getType()) {
					case USER:
						vte_pot_size_is_1_USER_TYPE(vte, ty);
						break;
					case USER_CLASS:
						vte_pot_size_is_1_USER_CLASS_TYPE(vte, ty);
						break;
					default:
						throw new IllegalStateException("Error");
					}
				}
			}
		}

		/* @ensures ite.type != null; */
		private void postOnChange(@NotNull IElementHolder eh) {
			if (ite.getType() == null && eh.getElement() instanceof final @NotNull VariableStatement variableStatement) {
				@NotNull TypeName typ = variableStatement.typeName();
				@NotNull OS_Type  ty  = dt2._inj().new_OS_UserType(typ);

				try {
					@Nullable GenType ty2 = getTY2(variableStatement, typ, ty);

					// no expression or TableEntryIV below
					if (ty2 != null) {
						final @NotNull TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null);
						// trying to keep genType up to date

						if (!ty.getTypeName().isNull())
							tte.setAttached(ty);
						tte.setAttached(ty2);

						ite.setType(tte);
						if (/*!ty.getTypeName().isNull() &&*/ !ty2.isNull()) {
							boolean skip = false;

							if (!ty.getTypeName().isNull()) {
								final TypeNameList gp = ((NormalTypeName) ty.getTypeName()).getGenericPart();
								if (gp != null) {
									if (gp.size() > 0 && ite.getType().genType.getNonGenericTypeName() == null) {
										skip = true;
									}
								}
							}
							if (!skip)
								ite.getType().genType.genCIForGenType2(dt2);
						}
					}
				} catch (ResolveError aResolveError) {
					dt2._errSink().reportDiagnostic(aResolveError);
				}
			}
		}

		private void type_is_null_and_attached_is_null_ite(final IdentTableEntry ite) {
			int                         y  = 2;
			PromiseExpectation<GenType> pe = dt2.promiseExpectation(ite, "Null USER type attached resolved");
//			ite.onType(phase, _inj().new_OnType() {
//
//				@Override
//				public void typeDeduced(@NotNull OS_Type aType) {
//					// TODO Auto-generated method stub
//					pe.satisfy(aType);
//				}
//
//				@Override
//				public void noTypeFound() {
//					// TODO Auto-generated method stub
//
//				}
//			})
			//;.done(new DoneCallback<GenType>() {
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

		private void type_is_null_and_attached_is_null_vte() {
			//LOG.err("2842 attached == null for "+((VariableTableEntry) bte).type);
			@NotNull PromiseExpectation<GenType> pe = dt2.promiseExpectation((VariableTableEntry) bte, "Null USER type attached resolved");
			VTE_TypePromises.found_parent(pe, generatedFunction, ((VariableTableEntry) bte), ite, dt2);
		}

		private void vte_pot_size_is_1_USER_CLASS_TYPE(@NotNull VariableTableEntry vte, @Nullable OS_Type aTy) {
			ClassStatement             klass = aTy.getClassOf();
			@Nullable LookupResultList lrl   = null;
			try {
				lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), klass.getContext(), dt2);
				@Nullable OS_Element best = lrl.chooseBest(null);
//							ite.setStatus(BaseTableEntry.Status.KNOWN, best);
				assert best != null;
				ite.setResolvedElement(best);

				final @NotNull GenType          genType  = dt2._inj().new_GenTypeImpl(klass);
				final TypeName                  typeName = vte.getType().genType.getNonGenericTypeName();
				final @Nullable ClassInvocation ci       = genType.genCI(typeName, dt2, dt2._errSink(), dt2.phase);
//							resolve_vte_for_class(vte, klass);
				ci.resolvePromise().done(new DoneCallback<EvaClass>() {
					@Override
					public void onDone(@NotNull EvaClass result) {
						vte.resolveTypeToClass(result);
					}
				});
			} catch (ResolveError aResolveError) {
				dt2._errSink().reportDiagnostic(aResolveError);
			}
		}

		private void vte_pot_size_is_1_USER_TYPE(@NotNull VariableTableEntry vte, @Nullable OS_Type aTy) {
			try {
				@NotNull GenType ty2 = dt2.resolve_type(aTy, aTy.getTypeName().getContext());
				// TODO ite.setAttached(ty2) ??
				OS_Element           ele  = ty2.getResolved().getElement();
				LookupResultList     lrl  = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), dt2);
				@Nullable OS_Element best = lrl.chooseBest(null);
				ite.setStatus(BaseTableEntry.Status.KNOWN, dt2._inj().new_GenericElementHolder(best));
//									ite.setResolvedElement(best);

				final @NotNull ClassStatement klass = (ClassStatement) ele;

				dt2.register_and_resolve(vte, klass);
			} catch (ResolveError resolveError) {
				dt2._errSink().reportDiagnostic(resolveError);
			}
		}
	}
	private final DeduceTypes2        dt2;
	private final Context             ctx;
	private final IdentTableEntry     ite;
	private final BaseEvaFunction     generatedFunction;
	private       boolean             _done;

	private       ITE_Resolver_Result _resolve_result;

	Unnamed_ITE_Resolver1(final DeduceTypes2 aDeduceTypes2, IdentTableEntry aIte, BaseEvaFunction aEvaFunction, Context aCtx) {
		dt2               = aDeduceTypes2;
		ctx               = aCtx;
		generatedFunction = aEvaFunction;
		ite               = aIte;
	}

	private DeduceTypes2.DeduceTypes2Injector _inj() {
		return dt2._inj();
	}

	@Override
	public void check() {
		resolve_ident_table_entry2();
		ite.getIdent().getName().addUsage(_inj().new_EN_DeduceUsage(ite.getBacklink(), ite.__gf, ite));
	}

	@Override
	public ITE_Resolver_Result getResult() {
		return _resolve_result;
	}

	@Override
	public boolean isDone() {
		return _done;
	}

	public void resolve_ident_table_entry2() {
		@Nullable InstructionArgument instructionArgument = new IdentIA(ite.getIndex(), generatedFunction);

		{
			// FIXME begging for recursion
			while (instructionArgument != null && instructionArgument instanceof IdentIA identIA) {
				@NotNull IdentTableEntry runningEntry = identIA.getEntry();

				@Nullable BaseTableEntry x = null;
				final InstructionArgument runningEntryBacklink = runningEntry.getBacklink();
				if (runningEntryBacklink instanceof final IntegerIA runningEntryBacklinkVar) {
					x = runningEntryBacklinkVar.getEntry();
//					if (vte.constructable_pte != null)
					instructionArgument = null;
				} else if (runningEntryBacklink instanceof final IdentIA runningEntryBacklinkIdent) {
					x    = runningEntryBacklinkIdent.getEntry();
					instructionArgument = ((IdentTableEntry) x).getBacklink();
				} else if (runningEntryBacklink instanceof final ProcIA runningEntryBacklinkProc) {
					x = runningEntryBacklinkProc.getEntry();
//					if (runningEntry.getCallablePTE() == null)
//						// turned out to be wrong (by double calling), so let's wrap it
//						runningEntry.setCallablePTE((ProcTableEntry) x);
					// TODO Proc cannot have backlink??
					instructionArgument = null; //((ProcTableEntry) x).backlink;
				} else if (runningEntryBacklink == null) {
					instructionArgument = null;
					x                   = null;
				}

				if (x != null) {
//					LOG.info("162 Adding FoundParent for "+runningEntry);
//					LOG.info(String.format("1656 %s \n\t %s \n\t%s", x, runningEntry, instructionArgument));
					x.addStatusListener(new FoundParent(x, runningEntry, runningEntry.getIdent().getContext(), generatedFunction)); // TODO context??
				}
			}
		}

		if (ite.hasResolvedElement()) {
			_done = true;
			final OS_Element e = ite.getResolvedElement();
			_resolve_result = new ITE_Resolver_Result(e);
			return;
		}

		ite.calculateResolvedElement();

		final OS_Element re = ite.getResolvedElement();
		if (re != null) {
			//ite.resolveExpectation.satisfy(re);

			var de3_ite = ite.getDeduceElement3(ite._deduceTypes2(), ite.__gf);

			if (re instanceof VariableStatement vs) {
				var vs_name = vs.getNameToken().getName();
				vs_name.addUsage(_inj().new_EN_NameUsage(ite.getIdent().getName(), de3_ite));
				vs_name.addUsage(_inj().new_EN_DeduceUsage(ite.getBacklink(), ite.__gf, ite));
			} else if (re instanceof FunctionDef fd) {
				var fd_name = fd.getNameNode().getName();
				fd_name.addUsage(_inj().new_EN_NameUsage(ite.getIdent().getName(), de3_ite));
				fd_name.addUsage(_inj().new_EN_DeduceUsage(ite.getBacklink(), ite.__gf, ite));
			} else {
				assert false;
			}

			_done           = true;
			_resolve_result = _inj().new_ITE_Resolver_Result(re);
			return;
		}
		if (true) {
			ite.addStatusListener(new BaseTableEntry.StatusListener() {
				@Override
				public void onChange(final @NotNull IElementHolder eh, final BaseTableEntry.Status newStatus) {
					if (newStatus != BaseTableEntry.Status.KNOWN) return;

					final OS_Element e = eh.getElement();
					dt2.found_element_for_ite(generatedFunction, ite, e, ctx, dt2.central());
				}
			});
		}
	}

}
