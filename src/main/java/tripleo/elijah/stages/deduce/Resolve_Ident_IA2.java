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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TableEntryIV;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;
import tripleo.elijah.util.Helpers;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 7/21/21 7:33 PM
 */
class Resolve_Ident_IA2 {
	private DeduceTypes2 deduceTypes2;
	private ErrSink errSink;
	private DeducePhase phase;
	private BaseGeneratedFunction generatedFunction;
	private FoundElement foundElement;

	public Resolve_Ident_IA2(final DeduceTypes2 aDeduceTypes2,
							 ErrSink aErrSink,
							 DeducePhase aPhase,
							 @NotNull BaseGeneratedFunction aGeneratedFunction,
							 @NotNull FoundElement aFoundElement) {
		deduceTypes2 = aDeduceTypes2;
		errSink = aErrSink;
		phase = aPhase;
		generatedFunction = aGeneratedFunction;
		foundElement = aFoundElement;
	}

	OS_Element el = null;
	Context ectx;

	public void resolveIdentIA2_(@NotNull final Context ctx,
								 @Nullable IdentIA identIA,
								 @Nullable List<InstructionArgument> s) {
		el = null;
		ectx = ctx;

		if (identIA == null)
			assert s != null;

		if (s == null)
			s = generatedFunction._getIdentIAPathList(identIA);

		if (identIA != null) {
			DeducePath dp = identIA.getEntry().buildDeducePath(generatedFunction);
			InstructionArgument ia2 = dp.getIA(dp.size() - 1);
			// ia2 is not == equals to identIA, but functionally equivalent
			if (ia2 instanceof IdentIA) {
				final @NotNull IdentTableEntry ite = ((IdentIA) ia2).getEntry();
				if (!ite.hasResolvedElement()) {
					int y = 2;
				}
				int y = 2;
			}
//			el = dp.getElement(dp.size()-1);
		} else {
			for (InstructionArgument ia2 : s) {
				if (ia2 instanceof IntegerIA) {
					ia2_IntegerIA((IntegerIA) ia2, ectx);
				} else if (ia2 instanceof IdentIA) {
					RIA_STATE st = ia2_IdentIA((IdentIA) ia2, ectx);

					switch (st) {
						case CONTINUE:
							continue;
						case NEXT:
							break;
						case RETURN:
							return;
					}
				} else if (ia2 instanceof ProcIA) {
					System.err.println("1373 ProcIA");
//						@NotNull ProcTableEntry pte = ((ProcIA) ia2).getEntry(); // README ectx seems to be set up already
				} else
					throw new NotImplementedException();
			}
		}
		foundElement.doFoundElement(el);
	}

	private RIA_STATE ia2_IdentIA(IdentIA ia2, Context ectx) {
		final IdentTableEntry idte2 = ((IdentIA) ia2).getEntry();
		final String text = idte2.getIdent().getText();

		final LookupResultList lrl = ectx.lookup(text);
		el = lrl.chooseBest(null);
		if (el == null) {
			errSink.reportError("1007 Can't resolve " + text);
			foundElement.doNoFoundElement();
			return RIA_STATE.RETURN;
		} else {
			if (idte2.type == null) {
				if (el instanceof VariableStatement) {
					VariableStatement vs = (VariableStatement) el;
					ia2_IdentIA_VariableStatement(ectx, idte2, vs);
				} else if (el instanceof FunctionDef) {
					OS_Type attached = new OS_UnknownType(el);
					TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, attached, null, idte2);
					idte2.type = tte;
				}
			}
			if (idte2.type != null) {
				assert idte2.type.getAttached() != null;
				try {
					if (!(idte2.type.getAttached() instanceof OS_UnknownType)) { // TODO
						OS_Type rtype = deduceTypes2.resolve_type(idte2.type.getAttached(), ectx);
						switch (rtype.getType()) {
							case USER_CLASS:
								ectx = rtype.getClassOf().getContext();
								break;
							case FUNCTION:
								ectx = ((OS_FuncType) rtype).getElement().getContext();
								break;
						}
						idte2.type.setAttached(rtype); // TODO may be losing alias information here
					}
				} catch (ResolveError resolveError) {
					if (resolveError.resultsList().size() > 1)
						errSink.reportDiagnostic(resolveError);
					else
						System.out.println("1089 Can't attach type to " + idte2.type.getAttached());
//							resolveError.printStackTrace(); // TODO print diagnostic
					return RIA_STATE.CONTINUE;
				}
			} else {
//					throw new IllegalStateException("who knows");
				errSink.reportWarning("2010 idte2.type == null for " + text);
			}
		}

		return RIA_STATE.NEXT;
	}

	private void ia2_IdentIA_VariableStatement(Context ectx, IdentTableEntry idte, VariableStatement vs) {
		try {
			if (!vs.typeName().isNull()) {
				TypeTableEntry tte;
				OS_Type attached;
				if (vs.initialValue() != IExpression.UNASSIGNED) {
					attached = DeduceLookupUtils.deduceExpression(vs.initialValue(), ectx);
				} else { // if (vs.typeName() != null) {
					attached = new OS_Type(vs.typeName());
				}

				IExpression initialValue;

				if (vs.initialValue() != IExpression.UNASSIGNED) {
					initialValue = vs.initialValue();
				} else {
//						attached = new OS_Type(vs.typeName());
					initialValue = null; // README presumably there is none, ie when generated
				}

				tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, attached, initialValue);
				idte.type = tte;
			} else if (vs.initialValue() != IExpression.UNASSIGNED) {
				OS_Type attached = DeduceLookupUtils.deduceExpression(vs.initialValue(), ectx);
				TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, attached, vs.initialValue());
				idte.type = tte;
			} else {
				System.err.println("Empty Variable Expression");
				throw new IllegalStateException("Empty Variable Expression");
//					return; // TODO call noFoundElement, raise exception
			}
		} catch (ResolveError aResolveError) {
			System.err.println("1937 resolve error " + vs.getName());
//				aResolveError.printStackTrace();
			errSink.reportDiagnostic(aResolveError);
		}
	}

	private void ia2_IntegerIA(IntegerIA ia2, Context ctx) {
		VariableTableEntry vte = generatedFunction.getVarTableEntry(DeduceTypes2.to_int(ia2));
		final String text = vte.getName();

		{
			List<TypeTableEntry> pot = deduceTypes2.getPotentialTypesVte(vte);
			if (pot.size() == 1) {
				final OS_Type attached = pot.get(0).getAttached();
				if (attached == null) {
					ia2_IntegerIA_null_attached(ctx, pot);
				} else {
					// TODO what is the state of vte.genType here?
					switch (attached.getType()) {
						case USER_CLASS:
							ectx = attached.getClassOf().getContext(); // TODO can combine later
							break;
						case FUNCTION:
							ectx = ((OS_FuncType) attached).getElement().getContext();
							break;
						case USER:
							ectx = attached.getTypeName().getContext();
							break;
						default:
							System.err.println("1098 " + attached.getType());
							throw new IllegalStateException("Can't be here.");
					}
				}
			}
		}

		OS_Type attached = vte.type.getAttached();
		if (attached != null) {
			switch (attached.getType()) {
				case USER_CLASS:
					ectx = attached.getClassOf().getContext();
					break;
				case FUNCTION:
					ectx = attached.getElement().getContext();
					break;
				case USER:
					try {
						@NotNull OS_Type ty = deduceTypes2.resolve_type(attached, ctx);
						ectx = ty.getClassOf().getContext();
					} catch (ResolveError resolveError) {
						System.err.println("1300 Can't resolve " + attached);
						resolveError.printStackTrace();
					}
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + attached.getType());
			}
		} else {
			if (vte.potentialTypes().size() == 1) {
				ia2_IntegerIA_potentialTypes_equals_1(vte, text);
			}
		}
	}

	private void ia2_IntegerIA_potentialTypes_equals_1(VariableTableEntry aVte, String aText) {
		int state = 0;
		final ArrayList<TypeTableEntry> pot = deduceTypes2.getPotentialTypesVte(aVte);
		final OS_Type attached1 = pot.get(0).getAttached();
		TableEntryIV te = pot.get(0).tableEntry;
		if (te instanceof ProcTableEntry) {
			final ProcTableEntry procTableEntry = (ProcTableEntry) te;
			// This is how it should be done, with an Incremental
			procTableEntry.getFunctionInvocation().generateDeferred().done(new DoneCallback<BaseGeneratedFunction>() {
				@Override
				public void onDone(BaseGeneratedFunction result) {
					result.typePromise().then(new DoneCallback<GenType>() {
						@Override
						public void onDone(GenType result) {
							int y = 2;
							aVte.typeDeferred().resolve(result); // save for later
						}
					});
				}
			});
			// but for now, just set ectx
			InstructionArgument en = procTableEntry.expression_num;
			if (en instanceof IdentIA) {
				final IdentIA identIA2 = (IdentIA) en;
				DeducePath ded = identIA2.getEntry().buildDeducePath(generatedFunction);
				@Nullable OS_Element el2 = ded.getElement(ded.size() - 1);
				if (el2 != null) {
					state = 1;
					ectx = el2.getContext();
					aVte.type.setAttached(attached1);
				}
			}
		}
		switch (state) {
			case 0:
				assert attached1 != null;
				aVte.type.setAttached(attached1);
				// TODO this will break
				switch (attached1.getType()) {
					case USER:
						final TypeName attached1TypeName = attached1.getTypeName();
						assert attached1TypeName instanceof RegularTypeName;
						final Qualident realName = ((RegularTypeName) attached1TypeName).getRealName();
						try {
							final List<LookupResult> lrl = DeduceLookupUtils.lookupExpression(realName, ectx).results();
							ectx = lrl.get(0).getElement().getContext();
						} catch (ResolveError aResolveError) {
							aResolveError.printStackTrace();
							int y = 2;
							throw new NotImplementedException();
						}
						break;
					case USER_CLASS:
						ectx = attached1.getClassOf().getContext();
						break;
					default:
						final TypeName typeName = attached1.getTypeName();
						errSink.reportError("1442 Don't know " + typeName.getClass().getName());
						throw new NotImplementedException();
				}
				break;
			case 1:
				break;
			default:
				System.out.println("1006 Can't find type of " + aText);
				break;
		}
	}

	private void ia2_IntegerIA_null_attached(Context ctx, List<TypeTableEntry> pot) {
		try {
			FunctionDef fd = null;
			ProcTableEntry pte = null;
			TableEntryIV xx = pot.get(0).tableEntry;
			if (xx != null) {
				if (xx instanceof ProcTableEntry) {
					final ProcTableEntry procTableEntry = (ProcTableEntry) xx;
					pte = procTableEntry;
					InstructionArgument xxx = procTableEntry.expression_num;
					if (xxx instanceof IdentIA) {
						final IdentIA identIA2 = (IdentIA) xxx;
						@NotNull IdentTableEntry ite = identIA2.getEntry();
						DeducePath deducePath = ite.buildDeducePath(generatedFunction);
						@Nullable OS_Element el5 = deducePath.getElement(deducePath.size() - 1);
						int y = 2;
						fd = (FunctionDef) el5;
					}
				}
			} else {
				LookupResultList lrl = DeduceLookupUtils.lookupExpression(pot.get(0).expression.getLeft(), ctx);
				OS_Element best = lrl.chooseBest(Helpers.List_of(
						new DeduceUtils.MatchFunctionArgs(
								(ProcedureCallExpression) pot.get(0).expression)));
				if (best instanceof FunctionDef) {
					fd = (FunctionDef) best;
				} else {
					fd = null;
					System.err.println("1195 Can't find match");
				}
			}
			if (fd != null) {
				final IInvocation invocation = deduceTypes2.getInvocation((GeneratedFunction) generatedFunction);
				final FunctionDef fd2 = fd;
				deduceTypes2.forFunction(deduceTypes2.newFunctionInvocation(fd2, pte, invocation, phase), new ForFunction() {
					@Override
					public void typeDecided(GenType aType) {
						assert fd2 == generatedFunction.getFD();
						//
						pot.get(0).setAttached(deduceTypes2.gt(aType));
					}
				});
			} else {
				errSink.reportError("1196 Can't find function");
			}
		} catch (ResolveError aResolveError) {
			aResolveError.printStackTrace();
			int y = 2;
			throw new NotImplementedException();
		}
	}

	enum RIA_STATE {
		CONTINUE, RETURN, NEXT
	}
}

//
//
//