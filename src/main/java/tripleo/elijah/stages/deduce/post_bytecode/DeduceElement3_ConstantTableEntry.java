package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.deduce.post_bytecode.DED.DED_CTE;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.ConstantTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.util.NotImplementedException;

public class DeduceElement3_ConstantTableEntry implements IDeduceElement3 {

    private final ConstantTableEntry    principal;
    public        BaseGeneratedFunction generatedFunction;
    public        DeduceTypes2          deduceTypes2;
    public        IDeduceElement3       deduceElement3;
    public        OS_Type               osType;
    public        Diagnostic            diagnostic;
    private       GenType               genType;

    @Contract(pure = true)
    public DeduceElement3_ConstantTableEntry(final ConstantTableEntry aConstantTableEntry) {
        principal = aConstantTableEntry;
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
        return principal.getDeduceElement3().getPrincipal();
    }

    @Override
    public DED elementDiscriminator() {
        return new DED_CTE(principal);
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
    public GenType genType() {
        return genType;
    }

    @Override
    public @NotNull DeduceElement3_Kind kind() {
        return DeduceElement3_Kind.GEN_FN__CTE;
    }

}
