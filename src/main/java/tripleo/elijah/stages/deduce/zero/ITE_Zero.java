package tripleo.elijah.stages.deduce.zero;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.LookupResultList;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.stages.deduce.DeduceLookupUtils;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GenericElementHolder;
import tripleo.elijah.stages.gen_fn.IElementHolder;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;

public class ITE_Zero {

    private final IdentTableEntry ite;
    ZeroResolver resolver;
    private boolean _preUpdateStatus_Change_called;

    @Contract(pure = true)
    public ITE_Zero(final IdentTableEntry aIdentTableEntry) {
        ite = aIdentTableEntry;
    }

    public void fp_onChange__001(@NotNull final TypeTableEntry vte, final IdentTableEntry ite, @NotNull final DeduceTypes2 deduceTypes2, final ErrSink errSink) {
        final OS_Type ty = vte.getAttached();

        @Nullable OS_Element ele2 = null;

        try {
            if (ty.getType() == OS_Type.Type.USER) {

                final Zero_Type zero_type = resolver.resolve_type(ty);

                @NotNull final GenType ty2;
                if (zero_type == null) {
                    throw new IllegalArgumentException("** 57 no type found");
                } else {
                    ty2 = zero_type.genType();
                }

//				ty2 = aFoundParent.deduceTypes2.resolve_type(ty, ty.getTypeName().getContext());
                final OS_Element ele;
                if (vte.genType.resolved == null) {
                    if (ty2.resolved.getType() == OS_Type.Type.USER_CLASS) {
                        vte.genType.copy(ty2);
                    }
                }
                ele = ty2.resolved.getElement();
                final LookupResultList lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), deduceTypes2);
                ele2 = lrl.chooseBest(null);
            } else {
                ele2 = ty.getClassOf(); // TODO might fail later (use getElement?)
            }

            @Nullable LookupResultList lrl = null;

            lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele2.getContext(), deduceTypes2);
            @Nullable final OS_Element best = lrl.chooseBest(null);
            // README commented out because only firing for dir.listFiles, and we always use `best'
//					if (best != ele2) LOG.err(String.format("2824 Divergent for %s, %s and %s", ite, best, ele2));;
            ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
        } catch (final ResolveError aResolveError) {
            aResolveError.printStackTrace();
            errSink.reportDiagnostic(aResolveError);
        }
    }

    public void setType(final GenType aGenType) {
        // TODO fill this in later with a Promise, perhaps
    }

    public void preUpdateStatus_Change(final IElementHolder eh, final BaseTableEntry.Status newStatus, final FoundElement foundElement, final String normal_path) {
        if (_preUpdateStatus_Change_called) return;

        if (newStatus == BaseTableEntry.Status.KNOWN) {
            _preUpdateStatus_Change_called = true;
//			y.preUpdateStatusListenerAdded = true;

//			assert el2 != eh.getElement();
            ite.resolveExpectation.satisfy(normal_path);
//			dc.found_element_for_ite(generatedFunction, y, eh.getElement(), null); // No context
//			LOG.info("1424 Found for " + normal_path);
            foundElement.doFoundElement(eh.getElement());
        }

    }
}
