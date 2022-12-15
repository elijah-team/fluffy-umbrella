package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.contexts.ClassContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;

class DT2_Logic_ResolveType {
    private final ElLog LOG;
    private final DeduceTypes2 _dt2;

    DT2_Logic_ResolveType(final ElLog aLOG, final DeduceTypes2 aDeduceTypes2) {
        LOG = aLOG;
        _dt2 = aDeduceTypes2;
    }

    /*static*/
    @NotNull GenType resolve_type(final /*@NotNull*/ OS_Module module, final @NotNull OS_Type type, final Context ctx) throws ResolveError {
        @NotNull GenType R = new GenType();
        R.typeName = type;

        switch (type.getType()) {
            case BUILT_IN:
                __resolve_type_built_in(module, type, R);
                break;
            case USER:
                __resolve_type_user(type, R);
                break;
            case USER_CLASS:
            case FUNCTION:
                break;
            default:
                throw new IllegalStateException("565 Unexpected value: " + type.getType());
        }

        return R;
    }

    private void __resolve_type_user(final @NotNull OS_Type type, final @NotNull GenType R) throws ResolveError {
        final TypeName tn1 = type.getTypeName();
        switch (tn1.kindOfType()) {
            case NORMAL: {
                final Qualident tn = ((NormalTypeName) tn1).getRealName();
                LOG.info("799 [resolving USER type named] " + tn);
                final LookupResultList lrl = DeduceLookupUtils.lookupExpression(tn, tn1.getContext(), _dt2);
                @Nullable OS_Element best = lrl.chooseBest(null);
                while (best instanceof AliasStatement) {
                    best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, _dt2);
                }
                if (best == null) {
                    if (tn.asSimpleString().equals("Any"))
                        /*return*/ R.resolved = new OS_AnyType(); // TODO not a class
                    throw new ResolveError(tn1, lrl);
                }

                if (best instanceof ClassContext.OS_TypeNameElement) {
                    /*return*/
                    R.resolved = new OS_GenericTypeNameType((ClassContext.OS_TypeNameElement) best); // TODO not a class
                } else
                    R.resolved = new OS_Type((ClassStatement) best);
                break;
            }
            case FUNCTION:
            case GENERIC:
            case TYPE_OF:
                throw new NotImplementedException();
            default:
                throw new IllegalStateException("414 Unexpected value: " + tn1.kindOfType());
        }
    }

    private void __resolve_type_built_in(final OS_Module module, final @NotNull OS_Type type, final @NotNull GenType R) throws ResolveError {
        switch (type.getBType()) {
            case SystemInteger: {
                @NotNull String typeName = type.getBType().name();
                assert typeName.equals("SystemInteger");
                OS_Module prelude = module.prelude;
                if (OS_Module.is_prelude(prelude)) // README Assume `module' IS prelude
                    prelude = module;
                final LookupResultList lrl = prelude.getContext().lookup(typeName);
                @Nullable OS_Element best = lrl.chooseBest(null);
                while (!(best instanceof ClassStatement)) {
                    if (best instanceof AliasStatement) {
                        best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, _dt2);
                    } else if (OS_Type.isConcreteType(best)) {
                        throw new NotImplementedException();
                    } else
                        throw new NotImplementedException();
                }
                if (best == null) {
                    throw new ResolveError(IdentExpression.forString(typeName), lrl);
                }
                R.resolved = new OS_Type((ClassStatement) best);
                break;
            }
            case String_: {
                @NotNull String typeName = type.getBType().name();
                assert typeName.equals("String_");
                OS_Module prelude = module.prelude;
                if (OS_Module.is_prelude(prelude)) // README Assume `module' IS prelude
                    prelude = module;
                final LookupResultList lrl = prelude.getContext().lookup("ConstString"); // TODO not sure about String
                @Nullable OS_Element best = lrl.chooseBest(null);
                while (!(best instanceof ClassStatement)) {
                    if (best instanceof AliasStatement) {
                        best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, _dt2);
                    } else if (OS_Type.isConcreteType(best)) {
                        throw new NotImplementedException();
                    } else
                        throw new NotImplementedException();
                }
                if (best == null) {
                    throw new ResolveError(IdentExpression.forString(typeName), lrl);
                }
                R.resolved = new OS_Type((ClassStatement) best);
                break;
            }
            case SystemCharacter: {
                @NotNull String typeName = type.getBType().name();
                assert typeName.equals("SystemCharacter");
                OS_Module prelude = module.prelude;
                if (OS_Module.is_prelude(prelude)) // README Assume `module' IS prelude
                    prelude = module;
                final LookupResultList lrl = prelude.getContext().lookup("SystemCharacter");
                @Nullable OS_Element best = lrl.chooseBest(null);
                while (!(best instanceof ClassStatement)) {
                    if (best instanceof AliasStatement) {
                        best = DeduceLookupUtils._resolveAlias2((AliasStatement) best, _dt2);
                    } else if (OS_Type.isConcreteType(best)) {
                        throw new NotImplementedException();
                    } else
                        throw new NotImplementedException();
                }
                if (best == null) {
                    throw new ResolveError(IdentExpression.forString(typeName), lrl);
                }
                R.resolved = new OS_Type((ClassStatement) best);
                break;
            }
            case Boolean: {
                OS_Module prelude = module.prelude;
                if (OS_Module.is_prelude(prelude)) // README Assume `module' IS prelude
                    prelude = module;
                final LookupResultList lrl = prelude.getContext().lookup("Boolean");
                final @Nullable OS_Element best = lrl.chooseBest(null);
                assert best != null;
                R.resolved = new OS_Type((ClassStatement) best); // TODO might change to Type
                break;
            }
            default:
                throw new IllegalStateException("531 Unexpected value: " + type.getBType());
        }
    }
}
