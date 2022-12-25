package tripleo.elijah.stages.deduce.zero;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.LookupResultList;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.deduce.DeduceLookupUtils;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.gen_fn.*;

public class PTE_Zero {
    private final ProcTableEntry procTableEntry;
    
    public PTE_Zero(ProcTableEntry aProcTableEntry) {
        procTableEntry = aProcTableEntry;
    }
    
    public void foundCounstructorDef(final @NotNull GeneratedConstructor constructorDef,
                                     final @NotNull IdentTableEntry ite,
                                     final @NotNull DeduceTypes2 deduceTypes2,
                                     final @NotNull ErrSink errSink) {
        @NotNull final BaseFunctionDef ele = constructorDef.getFD();
        
        try {
            final LookupResultList lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), deduceTypes2);
            @Nullable final OS_Element best = lrl.chooseBest(null);
            ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
        } catch (final ResolveError aResolveError) {
            aResolveError.printStackTrace();
            errSink.reportDiagnostic(aResolveError);
        }
    }
}
