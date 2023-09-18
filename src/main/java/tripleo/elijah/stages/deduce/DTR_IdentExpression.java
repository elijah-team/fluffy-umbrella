package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GenericElementHolderWithIntegerIA;
import tripleo.elijah.stages.gen_fn.IElementHolder;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IntegerIA;

public class DTR_IdentExpression {
	private final DeduceTypeResolve deduceTypeResolve;
	private final IdentExpression   identExpression;
	private final BaseTableEntry    bte;
	private       GenType           genType;

	public DTR_IdentExpression(final DeduceTypeResolve aDeduceTypeResolve, final IdentExpression aIdentExpression, final BaseTableEntry aBte) {
		deduceTypeResolve = aDeduceTypeResolve;
		identExpression   = aIdentExpression;
		bte               = aBte;
	}

	public void run(final IElementHolder eh, final GenType genType1) {
		this.genType = genType1;

		if (eh instanceof final @NotNull GenericElementHolderWithIntegerIA eh1) {
			final IntegerIA                   integerIA          = eh1.getIntegerIA();
			final @NotNull VariableTableEntry variableTableEntry = integerIA.getEntry();
			assert variableTableEntry == bte;
			variableTableEntry.typeResolvePromise().then(this::q);
		} else {
			//final DeduceLocalVariable dlv = ((VariableTableEntry) bte).dlv;
			//dlv.setDeduceTypes2(deduceTypeResolve.backlink.__dt2, null, deduceTypeResolve.backlink.__gf);

			bte.typeResolvePromise().then(this::q);
		}
	}

	private void q(final GenType result) {
		if (genType == result) return; // !!??
		genType.copy(result); // TODO genType = result?? because we want updates...
	}
}
