package tripleo.elijah.stages.deduce.post_bytecode;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.deduce.post_bytecode.DED.DED_ITE;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;

public class DeduceElement3_IdentTableEntry implements IDeduceElement3 {

	public final IdentTableEntry       principal;
	public       BaseGeneratedFunction generatedFunction;
	public       DeduceTypes2          deduceTypes2;
	private      GenType               genType;

	@Contract(pure = true)
	public DeduceElement3_IdentTableEntry(final IdentTableEntry aIdentTableEntry) {
		principal = aIdentTableEntry;
	}

	@Override
	public void resolve(final IdentIA aIdentIA, final Context aContext, final FoundElement aFoundElement) {
		// FoundElement is the "disease"
		deduceTypes2.resolveIdentIA_(aContext, aIdentIA, generatedFunction, aFoundElement);
	}

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

	public void assign_type_to_idte(final Context aFunctionContext, final Context aContext) {


		final IdentTableEntry      ite   = this.principal;
		final @NotNull DeducePhase phase   = deduceTypes2.phase;
		final @NotNull ElLog       LOG     = deduceTypes2.LOG;
		final ErrSink              errSink = deduceTypes2._errSink();


		if (!ite.hasResolvedElement()) {
			@NotNull IdentIA ident_a = new IdentIA(ite.getIndex(), generatedFunction);
			deduceTypes2.resolveIdentIA_(aContext, ident_a, generatedFunction, new FoundElement(phase) {

				final String path = generatedFunction.getIdentIAPathNormal(ident_a);

				@Override
				public void foundElement(OS_Element x) {
					if (ite.getResolvedElement() != x)
						ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
					if (ite.type != null && ite.type.getAttached() != null) {
						switch (ite.type.getAttached().getType()) {
						case USER:
							try {
								@NotNull GenType xx = deduceTypes2.resolve_type(ite.type.getAttached(), aFunctionContext);
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
								lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), aFunctionContext, deduceTypes2);
								@Nullable OS_Element best = lrl.chooseBest(null);
								if (best != null) {
									ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
									if (ite.type != null && ite.type.getAttached() != null) {
										if (ite.type.getAttached().getType() == OS_Type.Type.USER) {
											try {
												@NotNull GenType xx = deduceTypes2.resolve_type(ite.type.getAttached(), aFunctionContext);
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
}
