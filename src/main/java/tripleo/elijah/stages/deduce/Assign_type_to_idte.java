package tripleo.elijah.stages.deduce;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.util.NotImplementedException;

class Assign_type_to_idte {
    private final DeduceTypes2 aDeduceTypes2;
    private final IdentTableEntry ite;
    private final BaseGeneratedFunction generatedFunction;
    private final Context aFunctionContext;
    private final Context aContext;

    @Contract(pure = true)
    public Assign_type_to_idte(DeduceTypes2 aDeduceTypes2, IdentTableEntry ite, BaseGeneratedFunction generatedFunction, Context aFunctionContext, Context aContext) {
        this.aDeduceTypes2 = aDeduceTypes2;
        this.ite = ite;
        this.generatedFunction = generatedFunction;
        this.aFunctionContext = aFunctionContext;
        this.aContext = aContext;
    }

    public void run() {
        if (!ite.hasResolvedElement()) {
            @NotNull final IdentIA ident_a = new IdentIA(ite.getIndex(), generatedFunction);
            aDeduceTypes2.resolveIdentIA_(aContext, ident_a, generatedFunction, new ATTI_FoundElement(ident_a));
        }

    }

    private class ATTI_FoundElement extends FoundElement {
        final String path;
        private final @NotNull IdentIA ident_a;

        public ATTI_FoundElement(@NotNull IdentIA ident_a) {
            super(aDeduceTypes2.phase);
            this.ident_a = ident_a;
            path = generatedFunction.getIdentIAPathNormal(ident_a);
        }

        @Override
        public void foundElement(final OS_Element x) {
            if (ite.getResolvedElement() != x)
                ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));
            if (ite.type != null && ite.type.getAttached() != null) {
                switch (ite.type.getAttached().getType()) {
                    case USER:
                        __foundElement_USER_type();
                        break;
                    case USER_CLASS:
                        use_user_class(ite.type.getAttached(), ite);
                        break;
                    case FUNCTION:
                        __foundElement__FUNCTION_type();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + ite.type.getAttached().getType());
                }
            } else {
                NotImplementedException.raise();
                __ite_has_type(x);
            }
        }

        private void __ite_has_type(OS_Element x) {
            if (ite.hasResolvedElement())
                return;

            @Nullable LookupResultList lrl = null;
            IdentExpression iteIdent = ite.getIdent();

            try {
                lrl = DeduceLookupUtils.lookupExpression(iteIdent, aFunctionContext, aDeduceTypes2);
                @Nullable final OS_Element best = lrl.chooseBest(null);
                if (best != null) {
                    // TODO how does best relate to x??
                    ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(x));

                    // TODO this is checked above, so is below redundant??
                    assert ite.type != null;
                    assert ite.type.getAttached() != null;

                    if (ite.type != null && ite.type.getAttached() != null) {
                        switch (ite.type.getAttached().getType()) {
                            case USER:
                                try {
                                    @NotNull final GenType xx = aDeduceTypes2.resolve_type(ite.type.getAttached(), aFunctionContext);
                                    ite.type.setAttached(xx);
                                } catch (final ResolveError resolveError) { // TODO double catch
                                    aDeduceTypes2.LOG.info("210 Can't attach type to " + iteIdent);
                                    aDeduceTypes2.errSink.reportDiagnostic(resolveError);
                                }
                                break;
                        }
                    }
                } else {
                    aDeduceTypes2.LOG.err("184 Couldn't resolve " + iteIdent);
                }
            } catch (final ResolveError aResolveError) {
                aDeduceTypes2.LOG.err("184-506 Couldn't resolve " + iteIdent);
//						aResolveError.printStackTrace();
                aDeduceTypes2.errSink.reportDiagnostic(aResolveError);
            }

            assert ite.type != null;
            assert ite.type.getAttached() != null;

            if (ite.type.getAttached().getType() == OS_Type.Type.USER_CLASS) {
                use_user_class(ite.type.getAttached(), ite);
            }
        }

        private void __foundElement__FUNCTION_type() {
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
                            System.out.println("1483 " + result.resolved + " " + result.node);
                        }
                    });
                }
            }
        }

        private void __foundElement_USER_type() {
            try {
                @NotNull final GenType xx = aDeduceTypes2.resolve_type(ite.type.getAttached(), aFunctionContext);
                ite.type.setAttached(xx);
            } catch (final ResolveError resolveError) {
                aDeduceTypes2.LOG.info("192 Can't attach type to " + path);
                aDeduceTypes2.errSink.reportDiagnostic(resolveError);
            }
            if (ite.type.getAttached().getType() == OS_Type.Type.USER_CLASS) {
                use_user_class(ite.type.getAttached(), ite);
            }
            return;
        }

        private void use_user_class(@NotNull final OS_Type aType, @NotNull final IdentTableEntry aEntry) {
            final ClassStatement cs = aType.getClassOf();
            if (aEntry.constructable_pte != null) {
                final int yyy = 3;
                System.out.println("use_user_class: " + cs);
            }
        }

        @Override
        public void noFoundElement() {
            ite.setStatus(BaseTableEntry.Status.UNKNOWN, null);
            aDeduceTypes2.errSink.reportError("165 Can't resolve " + path);
        }
    }
}
