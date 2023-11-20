package tripleo.elijah.stages.deduce.zero;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.LookupResultList;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeduceLookupUtils;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GenericElementHolder;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.util.Stupidity;

public class VTE_Zero {
    private final VariableTableEntry  vte;
    private final Zero_PotentialTypes _pt = new Zero_PotentialTypes();

    public VTE_Zero(final VariableTableEntry aVariableTableEntry) {
        vte = aVariableTableEntry;
    }

    public Zero_PotentialTypes potentialTypes() {
        return _pt;
    }

    public void fp_onChange__002(@NotNull final VariableTableEntry vte, @Nullable final OS_Type ty, @NotNull final DeduceTypes2 deduceTypes2, final IdentTableEntry ite, final ErrSink errSink, final DeducePhase phase) {
        if (ty != null) {
            switch (ty.getType()) {
                case USER:
                    vte_pot_size_is_1_USER_TYPE(vte, ty, deduceTypes2, ite, errSink);
                    break;
                case USER_CLASS:
                    vte_pot_size_is_1_USER_CLASS_TYPE(vte, ty, deduceTypes2, ite, errSink, phase);
                    break;
            }
        } else {
            final int y = 2;//LOG.err("1696");
        }
    }

    private void vte_pot_size_is_1_USER_TYPE(final @NotNull VariableTableEntry vte,
                                             final @Nullable OS_Type aTy,
                                             final @NotNull DeduceTypes2 deduceTypes2,
                                             final @NotNull IdentTableEntry ite,
                                             final @NotNull ErrSink errSink) {
        try {
            @NotNull final GenType ty2 = deduceTypes2.resolve_type(aTy, aTy.getTypeName().getContext());
            // TODO ite.setAttached(ty2) ??
            final OS_Element           ele  = ty2.resolved.getElement();
            final LookupResultList     lrl  = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), deduceTypes2);
            @Nullable final OS_Element best = lrl.chooseBest(null);
            ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
//									ite.setResolvedElement(best);

            final @NotNull ClassStatement klass = (ClassStatement) ele;

            deduceTypes2.register_and_resolve(vte, klass);
        } catch (final ResolveError resolveError) {
            errSink.reportDiagnostic(resolveError);
        }
    }

    private void vte_pot_size_is_1_USER_CLASS_TYPE(@NotNull final VariableTableEntry vte, @Nullable final OS_Type aTy, final @NotNull DeduceTypes2 deduceTypes2, final IdentTableEntry ite, final ErrSink errSink, final DeducePhase phase) {
        final ClassStatement       klass = aTy.getClassOf();
        @Nullable LookupResultList lrl   = null;
        try {
            lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), klass.getContext(), deduceTypes2);
            @Nullable final OS_Element best = lrl.chooseBest(null);
//							ite.setStatus(BaseTableEntry.Status.KNOWN, best);
            assert best != null;
            ite.setResolvedElement(best);

            final @NotNull GenType          genType  = new GenType(klass);
            final TypeName                  typeName = vte.type.genType.nonGenericTypeName;
            final @Nullable ClassInvocation ci       = genType.genCI(typeName, deduceTypes2, errSink, phase);
//							resolve_vte_for_class(vte, klass);
            ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
                @Override
                public void onDone(final GeneratedClass result) {
                    vte.resolveTypeToClass(result);
                }
            });
        } catch (final ResolveError aResolveError) {
            errSink.reportDiagnostic(aResolveError);
        }
    }

    public void fp_onChange__001(@NotNull final TypeTableEntry tte, final IdentTableEntry ite, @NotNull final DeduceTypes2 deduceTypes2, final @NotNull ErrSink errSink) {
        final OS_Type ty = tte.getAttached();

        @Nullable OS_Element ele2 = null;

        try {
            final IdentExpression iteIdent = ite.getIdent();

            Stupidity.println_out("*** Looking for " + iteIdent.getText());

            switch (ty.getType()) {
                case USER:
                    final @NotNull GenType ty2 = deduceTypes2.resolve_type(ty, ty.getTypeName().getContext());

                    if (tte.genType.resolved == null) {
                        if (ty2.resolved.getType() == OS_Type.Type.USER_CLASS) {
                            tte.genType.copy(ty2);
                        }
                    }

                    final OS_Element ele = ty2.resolved.getElement();
                    final LookupResultList lrl = DeduceLookupUtils.lookupExpression(iteIdent, ele.getContext(), deduceTypes2);

                    ele2 = lrl.chooseBest(null);
                    break;
                case USER_CLASS:
                    ele2 = ty.getClassOf();
                    break;
                default:
                    ele2 = ty.getElement();
                    break;
            }

            //
            //

            Stupidity.println_out("*** Looking for " + iteIdent.getText() + " ; found " + ele2);

            //
            //

            @Nullable LookupResultList lrl = null;

            lrl = DeduceLookupUtils.lookupExpression(iteIdent, ele2.getContext(), deduceTypes2);
            @Nullable final OS_Element best = lrl.chooseBest(null);
            // README commented out because only firing for dir.listFiles, and we always use `best'
//					if (best != ele2) LOG.err(String.format("2824 Divergent for %s, %s and %s", ite, best, ele2));;
            ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
        } catch (final ResolveError aResolveError) {
            //
            //

//			Stupidity.println_out("*** Looking for " + iteIdent.getText() + " ; found "+ ele2);
            Stupidity.println_out("*** Second lookup failed");

            //
            //


//			aResolveError.printStackTrace();
            errSink.reportDiagnostic(aResolveError);
        }
    }
}
