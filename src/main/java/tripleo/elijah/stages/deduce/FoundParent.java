package tripleo.elijah.stages.deduce;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.zero.ITE_Zero;
import tripleo.elijah.stages.deduce.zero.VTE_Zero;
import tripleo.elijah.stages.deduce.zero.Zero_PotentialTypes;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;

public class FoundParent implements BaseTableEntry.StatusListener {
	private final DeduceTypes2 deduceTypes2;
	private BaseTableEntry bte;
	private IdentTableEntry ite;
	private Context ctx;
	private BaseGeneratedFunction generatedFunction;

	@Contract(pure = true)
	public FoundParent(DeduceTypes2 aDeduceTypes2, BaseTableEntry aBte, IdentTableEntry aIte, Context aCtx, BaseGeneratedFunction aGeneratedFunction) {
		deduceTypes2 = aDeduceTypes2;
		bte = aBte;
		ite = aIte;
		ctx = aCtx;
		generatedFunction = aGeneratedFunction;
	}

	@Override
	public void onChange(IElementHolder eh, BaseTableEntry.Status newStatus) {
		if (newStatus == BaseTableEntry.Status.KNOWN) {
			if (bte instanceof VariableTableEntry) {
				final @NotNull VariableTableEntry vte = (VariableTableEntry) bte;
				onChangeVTE(vte);
			} else if (bte instanceof ProcTableEntry) {
				final @NotNull ProcTableEntry pte = (ProcTableEntry) bte;
				onChangePTE(pte);
			} else if (bte instanceof IdentTableEntry) {
				final @NotNull IdentTableEntry ite = (IdentTableEntry) bte;

				ErrSink errSink = deduceTypes2.errSink;

				onChangeITE(ite.zero(), ite, errSink);
			}
			postOnChange(eh);
		}
	}

	/* @ensures ite.type != null */
	private void postOnChange(@NotNull IElementHolder eh) {
		if (ite.type == null && eh.getElement() instanceof VariableStatement) {
			@NotNull TypeName typ = ((VariableStatement) eh.getElement()).typeName();
			@NotNull OS_Type ty = new OS_Type(typ);

			try {
				@Nullable GenType ty2 = getTY2(typ, ty);

				// no expression or TableEntryIV below
				if (ty2 != null) {
					final @NotNull TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null);
					// trying to keep genType up to date
					tte.setAttached(ty);
					tte.setAttached(ty2);
					ite.type = tte;
				}
			} catch (ResolveError aResolveError) {
				deduceTypes2.errSink.reportDiagnostic(aResolveError);
			}
		}
	}

	private @Nullable GenType getTY2(@NotNull TypeName aTyp, @NotNull OS_Type aTy) throws ResolveError {
		if (aTy.getType() != OS_Type.Type.USER) {
			assert false;
			@NotNull GenType genType = new GenType();
			genType.set(aTy);
			return genType;
		}

		@Nullable GenType ty2 = null;
		if (!aTyp.isNull()) {
			assert aTy.getTypeName() != null;
			ty2 = deduceTypes2.resolve_type(aTy, aTy.getTypeName().getContext());
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
		@NotNull DeduceTypes2.PromiseExpectation<GenType> pe = deduceTypes2.promiseExpectation((VariableTableEntry) bte, "Null USER type attached resolved");
		((VariableTableEntry) bte).typePromise().done(new DoneCallback<GenType>() {
			@Override
			public void onDone(@NotNull GenType result) {
				pe.satisfy(result);
				final OS_Type attached1 = result.resolved != null ? result.resolved : result.typeName;
				if (attached1 != null) {
					switch (attached1.getType()) {
						case USER_CLASS:
							ite.type = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, attached1);
							break;
						case USER:
							try {
								@NotNull GenType ty3 = deduceTypes2.resolve_type(attached1, attached1.getTypeName().getContext());
								// no expression or TableEntryIV below
								@NotNull TypeTableEntry tte4 = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, null);
								// README trying to keep genType up to date
								tte4.setAttached(attached1);
								tte4.setAttached(ty3);
								ite.type = tte4; // or ty2?
							} catch (ResolveError aResolveError) {
								aResolveError.printStackTrace();
							}
							break;
					}
				}
			}
		});
	}

	private void type_is_null_and_attached_is_null_ite(IdentTableEntry ite) {
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

	private void onChangePTE(@NotNull ProcTableEntry aPte) {
		if (aPte.getStatus() == BaseTableEntry.Status.KNOWN) { // TODO might be obvious
			if (aPte.getFunctionInvocation() != null) {
				FunctionInvocation fi = aPte.getFunctionInvocation();
				BaseFunctionDef fd = fi.getFunction();
				if (fd instanceof ConstructorDef) {
					fi.generateDeferred().done(new DoneCallback<BaseGeneratedFunction>() {
						@Override
						public void onDone(BaseGeneratedFunction result) {
							@NotNull GeneratedConstructor constructorDef = (GeneratedConstructor) result;

							@NotNull BaseFunctionDef ele = constructorDef.getFD();

							try {
								LookupResultList lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), deduceTypes2);
								@Nullable OS_Element best = lrl.chooseBest(null);
								ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
							} catch (ResolveError aResolveError) {
								aResolveError.printStackTrace();
								deduceTypes2.errSink.reportDiagnostic(aResolveError);
							}
						}
					});
				}
			} else
				throw new NotImplementedException();
		} else {
			deduceTypes2.LOG.info("1621");
			@Nullable LookupResultList lrl = null;
			try {
				lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ctx, deduceTypes2);
				@Nullable OS_Element best = lrl.chooseBest(null);
				assert best != null;
				ite.setResolvedElement(best);
				deduceTypes2.found_element_for_ite(null, ite, best, ctx);
//						ite.setStatus(BaseTableEntry.Status.KNOWN, best);
			} catch (ResolveError aResolveError) {
				aResolveError.printStackTrace();
			}
		}
	}

	private void onChangeVTE(@NotNull VariableTableEntry vte) {
		@NotNull ArrayList<TypeTableEntry> pot = deduceTypes2.getPotentialTypesVte(vte);

		VTE_Zero zero = vte.zero();
		Zero_PotentialTypes pot1 = zero.potentialTypes();

		ErrSink errSink = deduceTypes2.errSink;
		@NotNull DeducePhase phase = deduceTypes2.phase;

		if (vte.getStatus() == BaseTableEntry.Status.KNOWN && vte.type.getAttached() != null && vte.getResolvedElement() != null) {
			zero.fp_onChange__001(vte.type, ite, deduceTypes2, errSink);
		} else if (pot.size() == 1) {
			TypeTableEntry tte = pot.get(0);
			@Nullable OS_Type ty = tte.getAttached();
			zero.fp_onChange__002(vte, ty, deduceTypes2, ite, errSink, phase);
		}
	}

	private void onChangeITE(ITE_Zero zero, @NotNull IdentTableEntry identTableEntry, ErrSink errSink) {
		if (identTableEntry.type != null) {
			zero.fp_onChange__001(identTableEntry.type, this.ite, deduceTypes2, errSink);
		} else {
			if (!identTableEntry.fefi) {
				final Found_Element_For_ITE fefi = new Found_Element_For_ITE(generatedFunction, ctx, deduceTypes2.LOG, deduceTypes2.errSink, new DeduceTypes2.DeduceClient1(deduceTypes2));
				fefi.action(identTableEntry);
				identTableEntry.fefi = true;
				identTableEntry.onFefiDone(new DoneCallback<GenType>() {
					@Override
					public void onDone(final GenType result) {

						try {
							final ClassStatement   resolvedClassOf = result.resolved.getClassOf();
							final LookupResultList lrl             = DeduceLookupUtils.lookupExpression(ite.getIdent(), resolvedClassOf.getContext(), deduceTypes2);

							assert ite.getIdent().getText().equals(resolvedClassOf.getName());

							final OS_Element ele2 = lrl.chooseBest(null);


							if (ele2 != null) {
								ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(ele2));
								ite.resolveTypeToClass(result.node);
							}
						} catch (ResolveError aResolveError) {
//							aResolveError.printStackTrace();
							errSink.reportDiagnostic(aResolveError);
						}
					}
				});
			}
			// TODO we want to setStatus but have no USER or USER_CLASS to perform lookup with
//			identTableEntry.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(null));
		}
	}
}
