package tripleo.elijah.stages.deduce.post_bytecode;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.LookupResultList;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.DeduceLookupUtils;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.deduce.post_bytecode.DED.DED_ITE;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GenericElementHolder;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.util.NotImplementedException;

public class DeduceElement3_IdentTableEntry extends DefaultStateful implements IDeduceElement3 {

	public final IdentTableEntry       principal;
	public       BaseGeneratedFunction generatedFunction;
	public       DeduceTypes2          deduceTypes2;
	private      GenType               genType;
	private      Context               fdCtx;
	private      Context               context;

	@Contract(pure = true)
	public DeduceElement3_IdentTableEntry(final IdentTableEntry aIdentTableEntry) {
		principal = aIdentTableEntry;
	}

	@Override
	public void resolve(final IdentIA aIdentIA, final @NotNull Context aContext, final FoundElement aFoundElement) {
		// FoundElement is the "disease"
		deduceTypes2.resolveIdentIA_(aContext, aIdentIA, generatedFunction, aFoundElement);
	}

	public Operation2<GenType> resolve1(final IdentTableEntry ite, final @NotNull Context aContext) {
		// FoundElement is the "disease"
		try {
			return Operation2.success(deduceTypes2.resolve_type(ite.type.getAttached(), aContext));
		} catch (final ResolveError aE) {
			return Operation2.failure(aE);
		}
	}

	//	@NotNull final GenType xx = // TODO xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
	@Override
	public void resolve(final Context aContext, final DeduceTypes2 aDeduceTypes2) {
		//		deduceTypes2.resolveIdentIA_(aContext, aIdentIA, generatedFunction, aFoundElement);
		throw new NotImplementedException();
		// careful with this
		//		throw new UnsupportedOperationException("Should not be reached");
	}

	@Override
	public OS_Element getPrincipal() {
		return principal.getDeduceElement3(deduceTypes2, generatedFunction).getPrincipal();
	}

	@Override
	public DED elementDiscriminator() {
		return new DED_ITE(principal);
	}

	@Override
	public DeduceTypes2 deduceTypes2() {
		return deduceTypes2;
	}

	@Override
	public BaseGeneratedFunction generatedFunction() {
		return generatedFunction;
	}

	@Override
	public @NotNull GenType genType() {
		if (genType == null) {
			genType = new GenType();
		}
		return genType;
		//return principal.type.genType;
	}

	@Override
	public DeduceElement3_Kind kind() {
		return DeduceElement3_Kind.GEN_FN__ITE;
	}

	public void _ctxts(final Context aFdCtx, final Context aContext) {
		fdCtx   = aFdCtx;
		context = aContext;
	}

	public static class ST {
		public static State EXIT_GET_TYPE;

		public static void register(final DeducePhase phase) {
			EXIT_GET_TYPE = phase.register(new ExitGetType());
		}

		static class ExitGetType implements State {
			private int identity;

			@Override
			public void apply(final DefaultStateful element) {
				final DeduceElement3_IdentTableEntry ite_de             = ((DeduceElement3_IdentTableEntry) element);
				final IdentTableEntry                ite                = ite_de.principal;
				final BaseGeneratedFunction          generatedFunction1 = ite_de.generatedFunction();
				final DeduceTypes2                   dt2                = ite_de.deduceTypes2;
				final DeducePhase                    phase1             = ite_de.deduceTypes2._phase();

				final Context          aFd_ctx  = ite_de.fdCtx;
				@NotNull final Context aContext = ite_de.context;

				assign_type_to_idte(ite, generatedFunction1, aFd_ctx, aContext, dt2, phase1);
			}

			public void assign_type_to_idte(@NotNull final IdentTableEntry ite,
			                                @NotNull final BaseGeneratedFunction generatedFunction,
			                                @NotNull final Context aFunctionContext,
			                                @NotNull final Context aContext,
			                                @NotNull final DeduceTypes2 dt2,
			                                @NotNull final DeducePhase phase) {
				if (!ite.hasResolvedElement()) {
					@NotNull final IdentIA ident_a = new IdentIA(ite.getIndex(), generatedFunction);
					dt2.resolveIdentIA_(aContext, ident_a, generatedFunction, new FoundElement(phase) {

						final String path = generatedFunction.getIdentIAPathNormal(ident_a);

						@Override
						public void foundElement(final OS_Element x) {
							if (ite.getResolvedElement() != x)
								ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
							if (ite.type != null && ite.type.getAttached() != null) {
								switch (ite.type.getAttached().getType()) {
								case USER:
									try {
										@NotNull final GenType xx = dt2.resolve_type(ite.type.getAttached(), aFunctionContext);
										ite.type.setAttached(xx);
									} catch (final ResolveError resolveError) {
										dt2._LOG().info("192 Can't attach type to " + path);
										dt2._errSink().reportDiagnostic(resolveError);
									}
									if (ite.type.getAttached().getType() == OS_Type.Type.USER_CLASS) {
										use_user_class(ite.type.getAttached(), ite);
									}
									break;
								case USER_CLASS:
									use_user_class(ite.type.getAttached(), ite);
									break;
								case FUNCTION: {
									// TODO All this for nothing
									//  the ite points to a function, not a function call,
									//  so there is no point in resolving it
									if (ite.type.tableEntry instanceof ProcTableEntry) {
										final @NotNull ProcTableEntry pte = (ProcTableEntry) ite.type.tableEntry;

									} else if (ite.type.tableEntry instanceof IdentTableEntry) {
										final @NotNull IdentTableEntry identTableEntry = (IdentTableEntry) ite.type.tableEntry;
										if (identTableEntry.getCallablePTE() != null) {
											@Nullable final ProcTableEntry cpte = identTableEntry.getCallablePTE();
											cpte.typePromise().then(new DoneCallback<GenType>() {
												@Override
												public void onDone(@NotNull final GenType result) {
													tripleo.elijah.util.Stupidity.println2("1483 " + result.resolved + " " + result.node);
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
								final int yy = 2;
								if (!ite.hasResolvedElement()) {
									@Nullable LookupResultList lrl = null;
									try {
										lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), aFunctionContext, dt2);
										@Nullable final OS_Element best = lrl.chooseBest(null);
										if (best != null) {
											ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
											if (ite.type != null && ite.type.getAttached() != null) {
												if (ite.type.getAttached().getType() == OS_Type.Type.USER) {
													try {
														@NotNull final GenType xx = dt2.resolve_type(ite.type.getAttached(), aFunctionContext);
														ite.type.setAttached(xx);
													} catch (final ResolveError resolveError) { // TODO double catch
														dt2._LOG().info("210 Can't attach type to " + ite.getIdent());
														dt2._errSink().reportDiagnostic(resolveError);
//												continue;
													}
												}
											}
										} else {
											dt2._LOG().err("184 Couldn't resolve " + ite.getIdent());
										}
									} catch (final ResolveError aResolveError) {
										dt2._LOG().err("184-506 Couldn't resolve " + ite.getIdent());
										aResolveError.printStackTrace();
									}
									if (ite.type.getAttached().getType() == OS_Type.Type.USER_CLASS) {
										use_user_class(ite.type.getAttached(), ite);
									}
								}
							}
						}

						private void use_user_class(@NotNull final OS_Type aType, @NotNull final IdentTableEntry aEntry) {
							final ClassStatement cs = aType.getClassOf();
							if (aEntry.constructable_pte != null) {
								final int yyy = 3;
								tripleo.elijah.util.Stupidity.println2("use_user_class: " + cs);
							}
						}

						@Override
						public void noFoundElement() {
							ite.setStatus(BaseTableEntry.Status.UNKNOWN, null);
							dt2._errSink().reportError("165 Can't resolve " + path);
						}
					});
				}
			}

			@Override
			public void setIdentity(final int aId) {
				identity = aId;
			}

			@Override
			public boolean checkState(final DefaultStateful aElement3) {
				return true;
			}
		}
	}
}
