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
import tripleo.elijah.stages.deduce.FoundParent;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.gen_fn.*;

public class ITE_Zero {

	private final IdentTableEntry ite;
	ZeroResolver resolver;

	@Contract(pure = true)
	public ITE_Zero(IdentTableEntry aIdentTableEntry) {
		ite = aIdentTableEntry;
	}

	public void fp_onChange__001(@NotNull TypeTableEntry vte, IdentTableEntry ite, @NotNull DeduceTypes2 deduceTypes2, ErrSink errSink) {
		final OS_Type ty = vte.getAttached();

		@Nullable OS_Element ele2 = null;

		try {
			if (ty.getType() == OS_Type.Type.USER) {

				Zero_Type zero_type = resolver.resolve_type(ty);

				@NotNull GenType ty2;
				if (zero_type == null)
					throw new IllegalArgumentException("** 57 no type found");
				else
					ty2 = zero_type.genType();

//				ty2 = aFoundParent.deduceTypes2.resolve_type(ty, ty.getTypeName().getContext());
				OS_Element ele;
				if (vte.genType.resolved == null) {
					if (ty2.resolved.getType() == OS_Type.Type.USER_CLASS) {
						vte.genType.copy(ty2);
					}
				}
				ele = ty2.resolved.getElement();
				LookupResultList lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), deduceTypes2);
				ele2 = lrl.chooseBest(null);
			} else
				ele2 = ty.getClassOf(); // TODO might fail later (use getElement?)

			@Nullable LookupResultList lrl = null;

			lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele2.getContext(), deduceTypes2);
			@Nullable OS_Element best = lrl.chooseBest(null);
			// README commented out because only firing for dir.listFiles, and we always use `best'
//					if (best != ele2) LOG.err(String.format("2824 Divergent for %s, %s and %s", ite, best, ele2));;
			ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
		} catch (ResolveError aResolveError) {
			aResolveError.printStackTrace();
			errSink.reportDiagnostic(aResolveError);
		}
	}

}
