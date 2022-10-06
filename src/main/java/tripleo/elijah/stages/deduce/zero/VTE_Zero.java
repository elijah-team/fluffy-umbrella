package tripleo.elijah.stages.deduce.zero;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.gen_fn.*;

public class VTE_Zero {
	private final VariableTableEntry vte;
	private Zero_PotentialTypes _pt = new Zero_PotentialTypes();

	public VTE_Zero(VariableTableEntry aVariableTableEntry) {
		vte = aVariableTableEntry;
	}

	public Zero_PotentialTypes potentialTypes() {
		return _pt;
	}

	public void fp_onChange__002(@NotNull VariableTableEntry vte, @Nullable OS_Type ty, @NotNull DeduceTypes2 deduceTypes2, IdentTableEntry ite, ErrSink errSink, DeducePhase phase) {
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
			int y = 2;//LOG.err("1696");
		}
	}

	public void fp_onChange__001(@NotNull TypeTableEntry tte, IdentTableEntry ite, @NotNull DeduceTypes2 deduceTypes2, ErrSink errSink) {
		final OS_Type ty = tte.getAttached();

		@Nullable OS_Element ele2 = null;

		try {
			if (ty.getType() == OS_Type.Type.USER) {
				@NotNull GenType ty2 = deduceTypes2.resolve_type(ty, ty.getTypeName().getContext());
				OS_Element ele;
				if (tte.genType.resolved == null) {
					if (ty2.resolved.getType() == OS_Type.Type.USER_CLASS) {
						tte.genType.copy(ty2);
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

	private void vte_pot_size_is_1_USER_TYPE(@NotNull VariableTableEntry vte, @Nullable OS_Type aTy, @NotNull DeduceTypes2 deduceTypes2, IdentTableEntry ite, ErrSink errSink) {
		try {
			@NotNull GenType ty2 = deduceTypes2.resolve_type(aTy, aTy.getTypeName().getContext());
			// TODO ite.setAttached(ty2) ??
			OS_Element ele = ty2.resolved.getElement();
			LookupResultList lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), ele.getContext(), deduceTypes2);
			@Nullable OS_Element best = lrl.chooseBest(null);
			ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(best));
//									ite.setResolvedElement(best);

			final @NotNull ClassStatement klass = (ClassStatement) ele;

			deduceTypes2.register_and_resolve(vte, klass);
		} catch (ResolveError resolveError) {
			errSink.reportDiagnostic(resolveError);
		}
	}

	private void vte_pot_size_is_1_USER_CLASS_TYPE(@NotNull VariableTableEntry vte, @Nullable OS_Type aTy, DeduceTypes2 deduceTypes2, IdentTableEntry ite, ErrSink errSink, DeducePhase phase) {
		ClassStatement klass = aTy.getClassOf();
		@Nullable LookupResultList lrl = null;
		try {
			lrl = DeduceLookupUtils.lookupExpression(ite.getIdent(), klass.getContext(), deduceTypes2);
			@Nullable OS_Element best = lrl.chooseBest(null);
//							ite.setStatus(BaseTableEntry.Status.KNOWN, best);
			assert best != null;
			ite.setResolvedElement(best);

			final @NotNull GenType genType = new GenType(klass);
			final TypeName typeName = vte.type.genType.nonGenericTypeName;
			final @Nullable ClassInvocation ci = genType.genCI(typeName, deduceTypes2, errSink, phase);
//							resolve_vte_for_class(vte, klass);
			ci.resolvePromise().done(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(GeneratedClass result) {
					vte.resolveTypeToClass(result);
				}
			});
		} catch (ResolveError aResolveError) {
			errSink.reportDiagnostic(aResolveError);
		}
	}
}
