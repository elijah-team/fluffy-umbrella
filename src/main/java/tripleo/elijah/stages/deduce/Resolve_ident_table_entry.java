package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;

class Resolve_ident_table_entry {

	private final DeduceTypes2 deduceTypes2;

	public Resolve_ident_table_entry(final DeduceTypes2 aDeduceTypes2) {
		deduceTypes2 = aDeduceTypes2;
	}

	public void act(@NotNull final IdentTableEntry ite, final BaseGeneratedFunction generatedFunction, final Context ctx) {

		@Nullable InstructionArgument itex = new IdentIA(ite.getIndex(), generatedFunction);
		{
			while (itex != null && itex instanceof IdentIA) {
				@NotNull final IdentTableEntry itee = ((IdentIA) itex).getEntry();

				@Nullable BaseTableEntry x = null;
				if (itee.getBacklink() instanceof IntegerIA) {
					@NotNull final VariableTableEntry vte = ((IntegerIA) itee.getBacklink()).getEntry();
					x = vte;
//					if (vte.constructable_pte != null)
					itex = null;
				} else if (itee.getBacklink() instanceof IdentIA) {
					x    = ((IdentIA) itee.getBacklink()).getEntry();
					itex = ((IdentTableEntry) x).getBacklink();
				} else if (itee.getBacklink() instanceof ProcIA) {
					x = ((ProcIA) itee.getBacklink()).getEntry();
//					if (itee.getCallablePTE() == null)
//						// turned out to be wrong (by double calling), so let's wrap it
//						itee.setCallablePTE((ProcTableEntry) x);
					itex = null; //((ProcTableEntry) x).backlink;
				} else if (itee.getBacklink() == null) {
					itex = null;
					x    = null;
				}

				if (x != null) {
//					LOG.err("162 Adding FoundParent for "+itee);
//					LOG.err(String.format("1656 %s \n\t %s \n\t%s", x, itee, itex));
					x.addStatusListener(new FoundParent(deduceTypes2, x, itee, itee.getIdent().getContext(), generatedFunction)); // TODO context??
				}
			}
		}
		if (ite.getResolvedElement() != null)
			return;
		if (true) {
			final @NotNull IdentIA identIA = new IdentIA(ite.getIndex(), generatedFunction);
			deduceTypes2.resolveIdentIA_(ite.getPC(), identIA, generatedFunction, new FoundElement(deduceTypes2.phase) {

				final String x = generatedFunction.getIdentIAPathNormal(identIA);

				@Override
				public void foundElement(final OS_Element e) {
//					ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(e)); // this is called in resolveIdentIA_
					deduceTypes2.found_element_for_ite(generatedFunction, ite, e, ctx);
				}

				@Override
				public void noFoundElement() {
					ite.setStatus(BaseTableEntry.Status.UNKNOWN, null);
					//errSink.reportError("1004 Can't find element for "+ x); // Already reported by 1179
				}
			});
		}
	}
}
