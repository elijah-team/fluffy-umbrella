package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.LookupResultList;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang2.SpecialFunctions;
import tripleo.elijah.lang2.SpecialVariables;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;
import java.util.regex.Pattern;

class Implement_Calls_ {
	private final DeduceTypes2          deduceTypes2;
	private final BaseGeneratedFunction gf;
	private final Context               context;
	private final InstructionArgument   i2;
	private final ProcTableEntry        pte;
	private final int                   pc;

	public Implement_Calls_(final DeduceTypes2 aDeduceTypes2, final @NotNull BaseGeneratedFunction aGf,
	                        final @NotNull Context aContext,
	                        final @NotNull InstructionArgument aI2,
	                        final @NotNull ProcTableEntry aPte,
	                        final int aPc) {
		deduceTypes2 = aDeduceTypes2;
		gf           = aGf;
		context      = aContext;
		i2           = aI2;
		pte          = aPte;
		pc           = aPc;
	}

	void action() {
		final IExpression pn1 = pte.expression;
		if (!(pn1 instanceof IdentExpression)) {
			throw new IllegalStateException("pn1 is not IdentExpression");
		}

		final String pn    = ((IdentExpression) pn1).getText();
		boolean      found = deduceTypes2.lookup_name_calls(context, pn, pte);
		if (found) return;

		final @Nullable String pn2 = SpecialFunctions.reverse_name(pn);
		if (pn2 != null) {
//				LOG.info("7002 "+pn2);
			found = deduceTypes2.lookup_name_calls(context, pn2, pte);
			if (found) return;
		}

		if (i2 instanceof IntegerIA) {
			found = action_i2_IntegerIA(pn, pn2);
		} else {
			found = action_dunder(pn);
		}

		if (!found)
			pte.setStatus(BaseTableEntry.Status.UNKNOWN, null);
	}

	private boolean action_i2_IntegerIA(final String pn, @Nullable final String pn2) {
		final boolean                     found;
		final @NotNull VariableTableEntry vte     = gf.getVarTableEntry(DeduceTypes2.to_int(i2));
		final Context                     ctx     = gf.getContextFromPC(pc); // might be inside a loop or something
		final String                      vteName = vte.getName();
		if (vteName != null) {
			found = action_i2_IntegerIA_vteName_is_null(pn, pn2, ctx, vteName);
		} else {
			found = action_i2_IntegerIA_vteName_is_not_null(pn, pn2, vte);
		}
		return found;
	}

	private boolean action_dunder(final String pn) {
		assert Pattern.matches("__[a-z]+__", pn);
//			LOG.info(String.format("i2 is not IntegerIA (%s)",i2.getClass().getName()));
		//
		// try to get dunder method from class
		//
		final IExpression exp = pte.getArgs().get(0).expression;
		if (exp instanceof IdentExpression) {
			return action_dunder_doIt(pn, (IdentExpression) exp);
		}
		return false;
	}

	private boolean action_i2_IntegerIA_vteName_is_null(final String pn, @Nullable final String pn2, final Context ctx, final String vteName) {
		boolean found = false;
		if (SpecialVariables.contains(vteName)) {
			deduceTypes2.LOG.err("Skipping special variable " + vteName + " " + pn);
		} else {
			final LookupResultList lrl2 = ctx.lookup(vteName);
//				LOG.info("7003 "+vteName+" "+ctx);
			final @Nullable OS_Element best2 = lrl2.chooseBest(null);
			if (best2 != null) {
				found = deduceTypes2.lookup_name_calls(best2.getContext(), pn, pte);
				if (found) return true;

				if (pn2 != null) {
					found = deduceTypes2.lookup_name_calls(best2.getContext(), pn2, pte);
					if (found) return true;
				}

				deduceTypes2.errSink.reportError("Special Function not found " + pn);
			} else {
				throw new NotImplementedException(); // Cant find vte, should never happen
			}
		}
		return found;
	}

	private boolean action_i2_IntegerIA_vteName_is_not_null(final String pn, @Nullable final String pn2, @NotNull final VariableTableEntry vte) {
		final @NotNull List<TypeTableEntry> tt = deduceTypes2.getPotentialTypesVte(vte);
		if (tt.size() != 1) {
			return false;
		}
		final OS_Type x = tt.get(0).getAttached();
		assert x != null;
		switch (x.getType()) {
		case USER_CLASS:
			pot_types_size_is_1_USER_CLASS(pn, pn2, x);
			return true;
		case BUILT_IN:
			final Context ctx2 = context;//x.getTypeName().getContext();
			try {
				@NotNull final GenType ty2 = deduceTypes2.resolve_type(x, ctx2);
				pot_types_size_is_1_USER_CLASS(pn, pn2, ty2.resolved);
				return true;
			} catch (final ResolveError resolveError) {
				resolveError.printStackTrace();
				deduceTypes2.errSink.reportDiagnostic(resolveError);
				return false;
			}
		default:
			assert false;
			return false;
		}
	}

	private boolean action_dunder_doIt(final String pn, final IdentExpression exp) {
		final @NotNull IdentExpression      identExpression = exp;
		@Nullable final InstructionArgument vte_ia          = gf.vte_lookup(identExpression.getText());
		if (vte_ia != null) {
			VTE_TypePromises.dunder(pn, (IntegerIA) vte_ia, pte, deduceTypes2);
			return true;
		}
		return false;
	}

	private void pot_types_size_is_1_USER_CLASS(final String pn, @Nullable final String pn2, final OS_Type x) {
		boolean       found;
		final Context ctx1 = x.getClassOf().getContext();

		found = deduceTypes2.lookup_name_calls(ctx1, pn, pte);
		if (found) return;

		if (pn2 != null) {
			found = deduceTypes2.lookup_name_calls(ctx1, pn2, pte);
		}

		if (!found) {
			//throw new NotImplementedException(); // TODO
			deduceTypes2.errSink.reportError("Special Function not found " + pn);
		}
	}
}
