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
import tripleo.elijah.stages.deduce.DeduceLookupUtils;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.deduce.ITE_Resolver;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stateful.DefaultStateful;
import tripleo.elijah.stateful.State;
import tripleo.elijah.stateful.StateRegistrationToken;

//@StatefulProperty
public class ExitGetType implements State {
	private static class __foundElement_hasIdteType__ITE_Resolver implements ITE_Resolver {
		@Override
		public void check() {
			int y = 2;
		}

		@Contract(pure = true)
		@Override
		public IdentTableEntry.@Nullable ITE_Resolver_Result getResult() {
			return null;
		}

		@Override
		public boolean isDone() {
			return false;
		}
	}

	public static class __foundElement_noIdteType__ITE_Resolver implements ITE_Resolver {
		@Override
		public void check() {
			int y = 2;
		}

		@Contract(pure = true)
		@Override
		public IdentTableEntry.@Nullable ITE_Resolver_Result getResult() {
			return null;
		}

		@Override
		public boolean isDone() {
			return false;
		}
	}

	private StateRegistrationToken identity;

	@Override
	public void apply(final DefaultStateful element) {
		final DeduceElement3_IdentTableEntry ite_de             = ((DeduceElement3_IdentTableEntry) element);
		final IdentTableEntry                ite                = ite_de.principal;
		final BaseEvaFunction                generatedFunction1 = ite_de.generatedFunction();
		final DeduceTypes2                   dt2                = ite_de.deduceTypes2;
		final DeducePhase phase1   = ite_de.deduceTypes2._phase();
		final Context     aFd_ctx  = ite_de.fdCtx;
		final Context     aContext = ite_de.context;

		assign_type_to_idte(ite, generatedFunction1, aFd_ctx, aContext, dt2, phase1);
	}

	public void assign_type_to_idte(@NotNull final IdentTableEntry ite,
									@NotNull final BaseEvaFunction generatedFunction,
									@NotNull final Context aFunctionContext,
									@NotNull final Context aContext,
									@NotNull final DeduceTypes2 dt2,
									@NotNull final DeducePhase phase) {
		if (!ite.hasResolvedElement()) {
			@NotNull final IdentIA ident_a = new IdentIA(ite.getIndex(), generatedFunction);
			dt2.resolveIdentIA_(aContext, ident_a, generatedFunction, new FoundElement(phase) {

				final String path = generatedFunction.getIdentIAPathNormal(ident_a);

				final __foundElement_noIdteType__ITE_Resolver resolver000 = new __foundElement_noIdteType__ITE_Resolver();

				final __foundElement_hasIdteType__ITE_Resolver resolver001 = new __foundElement_hasIdteType__ITE_Resolver();
				private void __foundElement_hasIdteType() {

					ite.addResolver(resolver001);

					switch (ite.getType().getAttached().getType()) {
					case USER -> {
						try {
							final @NotNull GenType xx = dt2.resolve_type(ite.getType().getAttached(), aFunctionContext);
							ite.getType().setAttached(xx);
						} catch (final ResolveError resolveError) {
							dt2._LOG().info("192 Can't attach type to " + path);
							dt2._errSink().reportDiagnostic(resolveError);
						}
						if (ite.getType().getAttached().getType() == OS_Type.Type.USER_CLASS) {
							use_user_class(ite.getType().getAttached(), ite);
						}
					}
					case USER_CLASS -> use_user_class(ite.getType().getAttached(), ite);
					case FUNCTION -> {
						// TODO All this for nothing
						//  the ite points to a function, not a function call,
						//  so there is no point in resolving it
						if (ite.getType().tableEntry instanceof final @NotNull ProcTableEntry pte) {

						} else if (ite.getType().tableEntry instanceof final @NotNull IdentTableEntry identTableEntry) {
							if (identTableEntry.getCallablePTE() != null) {
								@Nullable final ProcTableEntry cpte = identTableEntry.getCallablePTE();
								cpte.typePromise().then(new DoneCallback<GenType>() {
									@Override
									public void onDone(@NotNull final GenType result) {
										tripleo.elijah.util.Stupidity.println2("1483 " + result.getResolved() + " " + result.getNode());
									}
								});
							}
						}
					}
					default -> throw new IllegalStateException("Unexpected value: " + ite.getType().getAttached().getType());
					}
				}

				private void __foundElement_noIdteType(final @NotNull OS_Element x) {

					ite.addResolver(resolver000);

					final int yy = 2;
					if (!ite.hasResolvedElement()) {
						@Nullable LookupResultList lrl = null;
						try {
							lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), aFunctionContext, dt2);
							@Nullable final OS_Element best = lrl.chooseBest(null);
							if (best != null) {
								ite.setStatus(BaseTableEntry.Status.KNOWN, dt2._inj().new_GenericElementHolder(x));
								if (ite.getType() != null && ite.getType().getAttached() != null) {
									if (ite.getType().getAttached().getType() == OS_Type.Type.USER) {
										try {
											@NotNull final GenType xx = dt2.resolve_type(ite.getType().getAttached(), aFunctionContext);
											ite.getType().setAttached(xx);
										} catch (final ResolveError resolveError) { // TODO double catch
											dt2._LOG().info("210 Can't attach type to " + ite.getIdent());
											dt2._errSink().reportDiagnostic(resolveError);
											//continue;
										}
									}
								}
							} else {
								dt2._LOG().err("184 Couldn't resolve " + ite.getIdent());
							}
						} catch (final ResolveError aResolveError) {
							dt2._LOG().err("184-506 Couldn't resolve " + ite.getIdent());
							aResolveError.printStackTrace();
							dt2._errSink().reportDiagnostic(aResolveError);
						}
						if (ite.getType().getAttached().getType() == OS_Type.Type.USER_CLASS) {
							use_user_class(ite.getType().getAttached(), ite);
						}
					}
				}

				@Override
				public void foundElement(final @NotNull OS_Element x) {
					System.err.println("590-590 " + x);


					if (ite.getResolvedElement() != x)
						ite.setStatus(BaseTableEntry.Status.KNOWN, dt2._inj().new_GenericElementHolder(x));
					if (ite.getType() != null && ite.getType().getAttached() != null) {
						__foundElement_hasIdteType();
					} else {
						__foundElement_noIdteType(x);
					}
				}

				@Override
				public void noFoundElement() {
					ite.setStatus(BaseTableEntry.Status.UNKNOWN, null);
					dt2._errSink().reportError("165 Can't resolve " + path);
				}

				private void use_user_class(@NotNull final OS_Type aType, @NotNull final IdentTableEntry aEntry) {
					final ClassStatement cs = aType.getClassOf();
					if (aEntry.getConstructable_pte() != null) {
						final int yyy = 3;
						tripleo.elijah.util.Stupidity.println2("use_user_class: " + cs);
					}
				}
			});
		}
	}

	@Override
	public boolean checkState(final DefaultStateful aElement3) {
		return true;
	}

	@Override
	public void setIdentity(final StateRegistrationToken aId) {
		identity = aId;
	}
}
