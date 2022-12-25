package tripleo.elijah.stages.deduce.zero;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.diagnostic.Diagnostic;
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
    
    private final DeferredObject<IElementHolder, Diagnostic, Void> _foundCounstructorDef2Promise = new DeferredObject<>();
    
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
//            aResolveError.printStackTrace();
            errSink.reportDiagnostic(aResolveError);
        }
    }
    
    public Promise<IElementHolder, Diagnostic, Void> foundCounstructorPromise() {
        return _foundCounstructorDef2Promise.promise();
    }
    
    public void calculateConstructor(@NotNull GeneratedConstructor constructorDef, @NotNull IdentTableEntry ite, @NotNull DeduceTypes2 deduceTypes2) {
        @NotNull final BaseFunctionDef ele = constructorDef.getFD();
        
        try {
            final LookupResultList lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), deduceTypes2);
            @Nullable final OS_Element best = lrl.chooseBest(null);
//            ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
            GenericElementHolder elementHolder = new GenericElementHolder(best);
            _foundCounstructorDef2Promise.resolve(elementHolder);
        } catch (final ResolveError aResolveError) {
            _foundCounstructorDef2Promise.reject(aResolveError);
        }
    }
}
