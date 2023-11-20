package tripleo.elijah.stages.deduce.nextgen;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.TableEntryIV;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;

public class VTE_TTE_Resolver implements DN_Resolver2 {
	private final VariableTableEntry    vte;
	private final int                   instructionIndex;
	private final DeduceTypes2          deduceTypes2;
	private final BaseGeneratedFunction generatedFunction;

	public VTE_TTE_Resolver(final VariableTableEntry aVte, final int aInstructionIndex, final DeduceTypes2 aDeduceTypes2, final @NotNull BaseGeneratedFunction aGeneratedFunction) {
		vte               = aVte;
		instructionIndex  = aInstructionIndex;
		deduceTypes2      = aDeduceTypes2;
		generatedFunction = aGeneratedFunction;
	}

	public void apply(final @NotNull GenType aType, final TableEntryIV pte, final OS_Type gt) {
		@NotNull final TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, gt, pte._expression(), pte);
		vte.addPotentialType(instructionIndex, tte);
	}

	@Override
	public void resolve(final DN_Resolver2 aResolver2) {
		throw new Error();
	}
}
