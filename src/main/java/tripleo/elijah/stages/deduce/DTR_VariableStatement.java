package tripleo.elijah.stages.deduce;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.NormalTypeName;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.lang.TypeNameList;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.Constructable;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GenericElementHolderWithType;
import tripleo.elijah.stages.gen_fn.IElementHolder;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;

import java.util.Map;

class DTR_VariableStatement {
	private final DeduceTypeResolve deduceTypeResolve;
	private final VariableStatement variableStatement;

	@Contract(pure = true)
	public DTR_VariableStatement(final DeduceTypeResolve aDeduceTypeResolve, final VariableStatement aVariableStatement) {
		deduceTypeResolve = aDeduceTypeResolve;
		variableStatement = aVariableStatement;
	}

	public void run(final IElementHolder eh, final GenType genType) {
		final TypeName typeName1 = variableStatement.typeName();

		if (!(typeName1 instanceof NormalTypeName)) {
			throw new IllegalStateException();
		}

		final NormalTypeName normalTypeName = (NormalTypeName) typeName1;

		if (normalTypeName.getGenericPart() != null) {
			normalTypeName_notGeneric(eh, genType, normalTypeName);
		} else {
			if (!normalTypeName.isNull()) {
				normalTypeName_generic_butNotNull(eh, genType, normalTypeName);
			}
		}
	}

	private void normalTypeName_notGeneric(final IElementHolder eh, final GenType genType, final @NotNull NormalTypeName normalTypeName) {
		final TypeNameList genericPart = normalTypeName.getGenericPart();
		if (eh instanceof GenericElementHolderWithType) {
			final GenericElementHolderWithType eh1  = (GenericElementHolderWithType) eh;
			final DeduceTypes2                 dt2  = eh1.getDeduceTypes2();
			final OS_Type                      type = eh1.getType();

			normalTypeName_notGeneric_typeProvided(genType, normalTypeName, dt2, type);
		} else
			normalTypeName_notGeneric_typeNotProvided(genType, normalTypeName);
	}

	private void normalTypeName_generic_butNotNull(final IElementHolder eh, final @NotNull GenType genType, final NormalTypeName normalTypeName) {
		if (eh instanceof GenericElementHolderWithType) {
			final GenericElementHolderWithType eh1  = (GenericElementHolderWithType) eh;
			final DeduceTypes2                 dt2  = eh1.getDeduceTypes2();
			final OS_Type                      type = eh1.getType();

			genType.typeName = new OS_UserType(normalTypeName);
			try {
				final @NotNull GenType resolved = dt2.resolve_type(genType.typeName, variableStatement.getContext());
				if (resolved.resolved.getType() == OS_Type.Type.GENERIC_TYPENAME) {
					final BaseTableEntry backlink = deduceTypeResolve.backlink;

					normalTypeName_generic_butNotNull_resolveToGeneric(genType, resolved, backlink);
				} else {
					normalTypeName_generic_butNotNull_resolveToNonGeneric(genType, resolved);
				}
			} catch (final ResolveError aResolveError) {
				aResolveError.printStackTrace();
				assert false;
			}
		} else if (eh instanceof DeduceElement3Holder) {
		} else
			genType.typeName = new OS_UserType(normalTypeName);
	}

	private void normalTypeName_notGeneric_typeProvided(final @NotNull GenType genType, final NormalTypeName normalTypeName, final @NotNull DeduceTypes2 dt2, final @NotNull OS_Type type) {
		genType.nonGenericTypeName = normalTypeName;

		assert normalTypeName == type.getTypeName();

		final OS_Type typeName = new OS_UserType(normalTypeName);
		try {
			final @NotNull GenType resolved = dt2.resolve_type(typeName, variableStatement.getContext());
			genType.resolved = resolved.resolved;
		} catch (final ResolveError aResolveError) {
			aResolveError.printStackTrace();
			assert false;
		}
	}

	private /*static*/ void normalTypeName_notGeneric_typeNotProvided(final @NotNull GenType genType, final NormalTypeName normalTypeName) {
		genType.nonGenericTypeName = normalTypeName;
	}

	private /*static*/ void normalTypeName_generic_butNotNull_resolveToGeneric(final GenType genType, final @NotNull GenType resolved, final @NotNull BaseTableEntry backlink) {
		backlink.typeResolvePromise().then(new DoneCallback<GenType>() {
			@Override
			public void onDone(final GenType result_gt) {
				((Constructable) backlink).constructablePromise().then(new DoneCallback<ProcTableEntry>() {
					@Override
					public void onDone(final @NotNull ProcTableEntry result_pte) {
						final ClassInvocation ci = result_pte.getClassInvocation();
						assert ci != null;
						final @Nullable Map<TypeName, OS_Type> gp  = ci.genericPart;
						final TypeName                         sch = resolved.typeName.getTypeName();
						assert gp != null;
						for (final Map.Entry<TypeName, OS_Type> entrySet : gp.entrySet()) {
							if (entrySet.getKey().equals(sch)) {
								genType.resolved = entrySet.getValue();
								break;
							}
						}
					}
				});
			}
		});
	}

	private /*static*/ void normalTypeName_generic_butNotNull_resolveToNonGeneric(final @NotNull GenType genType, final @NotNull GenType resolved) {
		genType.resolved = resolved.resolved;
	}
}
