package tripleo.elijah.lang;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang2.BuiltInTypes;

public interface OS_Type2 {

    static OS_Type2 of(@NotNull ClassStatement cs) {
        return new OS_Type2_ClassStatement(cs);
    }

    static OS_Type2 of(@NotNull FunctionDef fd) {
        return new OS_Type2_FunctionDef(fd);
    }

    static OS_Type2 of(@NotNull BuiltInTypes bit) {
        return new OS_Type2_BuiltInTypes(bit);
    }

    static OS_Type2 ofUnitType() {
        return new OS_Type2_UnitType();
    }

    /*@ ensures type_of_type = Type.USER; */
    public static OS_Type2 of(final @NotNull TypeName typeName) {
        if (typeName.isNull())
            System.err.println("170 null typeName in OS_Type");// throw new AssertionError();
        return new OS_Type2_TypeName(typeName);
    }

    @NotNull ClassStatement getClassOf();

    OS_Element getElement();

//     @Override
//     public boolean equals(final @Nullable Object o) {
//         if (this == o) return true;
//         if (o == null || getClass() != o.getClass()) return false;
//         final @NotNull OS_Type os_type = (OS_Type) o;
// /*		switch (kind) {
//         case USER: return (((OS_Type) o).getTypeName()).equals(getTypeName());
//         case BUILT_IN: return (((OS_Type) o).type).equals(type);
//         case USER_CLASS: return (((OS_Type) o).etype).equals(etype);
//         default: throw new IllegalStateException("Cant be here");
//     }
// */
//         final boolean b = type == os_type.type &&
//                 type_of_type == os_type.type_of_type &&
//                 Objects.equals(etype, os_type.etype) &&
//                 Objects.equals(ttype, os_type.ttype);
//         return b;
//     }

    // @Override
    // public int hashCode() {
    //     return Objects.hash(type, type_of_type, etype.hashCode(), ttype.hashCode());
    // }

//     public @NotNull ClassStatement getClassOf() {
//         if (etype != null && etype instanceof ClassStatement)
//             return (ClassStatement) etype;
//         System.err.println("3001 " + etype + " " + toString());
//         throw new IllegalArgumentException();
// //		return null;
//     }

//     public OS_Element getElement() {
//         switch (type_of_type) {
//             case USER_CLASS:
// //		case FUNCTION: // defined in subclass
//                 return etype;
//             default:
//                 throw new IllegalArgumentException();
//         }
//     }

    boolean isConcreteType(final OS_Element element);

    // public @NotNull OS_Type resolve(final @NotNull Context ctx) {
    //     assert ctx != null;
    //     switch (getType()) {
    //         case BUILT_IN: {
    //             //
    //             // TODO These are technically not right
    //             //
    //             switch (getBType()) {
    //                 case SystemInteger: {
    //                     final LookupResultList r;
    //                     final @Nullable OS_Element best;
    //
    //                     r = ctx.lookup("SystemInteger");
    //                     best = r.chooseBest(null);
    //                     return new OS_Type((ClassStatement) best);
    //                 }
    //                 case Boolean: {
    //                     final LookupResultList r;
    //                     final @Nullable OS_Element best;
    //
    //                     r = ctx.lookup("Boolean");
    //                     best = r.chooseBest(null);
    //                     return new OS_Type((ClassStatement) best);
    //                 }
    //                 case Unit: {
    //                     return new OS_Type.OS_UnitType();
    //                 }
    //                 case String_: {
    //                     final LookupResultList r;
    //                     final @Nullable OS_Element best;
    //
    //                     r = ctx.lookup("String8"); // TODO not sure about this
    //                     best = r.chooseBest(null);
    //                     return new OS_Type((ClassStatement) best);
    //                 }
    //                 default:
    //                     throw new IllegalStateException("Unexpected value: " + getBType());
    //             }
    //         }
    //         case USER: {
    //             final LookupResultList r = ctx.lookup(getTypeName().toString()); // TODO
    //             final @Nullable OS_Element best = r.chooseBest(null);
    //             return new OS_Type((ClassStatement) best);
    //         }
    //         case FUNCTION:
    //             return this;
    //         default:
    //             throw new IllegalStateException("can't be here.");
    //     }
    // }

    @NotNull OS_Type2 resolve(final @NotNull Context ctx);

    boolean isUnitType();

    @NotNull OS_Type.Type getType();

    /*@ ensures type_of_type = Type.BUILT_IN; */
    // public OS_Type(final BuiltInTypes aType) {
    //     this.type = aType;
    //     this.type_of_type = OS_Type.Type.BUILT_IN;
    // }

    /*@ ensures type_of_type = Type.USER_CLASS; */
    // public OS_Type(final @NotNull ClassStatement klass) {
    //     assert klass != null;
    //     this.etype = klass;
    //     this.type_of_type = OS_Type.Type.USER_CLASS;
    // }

    BuiltInTypes getBType();

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    // @Override
    // public String toString() {
    //     return String.format("<OS_Type %s %s %s %s>", ttype, etype, type, type_of_type);
    // }

    TypeName getTypeName();

    enum Type {
        BUILT_IN, USER, USER_CLASS, FUNC_EXPR, UNIT_TYPE, UNKNOWN, ANY, FUNCTION, GENERIC_TYPENAME
    }

    class OS_Type2_UnitType implements OS_Type2 {

        @Override
        public @NotNull ClassStatement getClassOf() {
            return null;
        }

        @Override
        public OS_Element getElement() {
            return null;
        }

        @Override
        public boolean isConcreteType(final OS_Element element) {
            return false;
        }

        @Override
        public @NotNull OS_Type2 resolve(final @NotNull Context ctx) {
            return null;
        }

        @Override
        public boolean isUnitType() {
            return true;
        }

        @Override
        public @NotNull OS_Type.Type getType() {
            return OS_Type.Type.UNIT_TYPE;
        }

        @Override
        public BuiltInTypes getBType() {
            return null;
        }

        @Override
        public TypeName getTypeName() {
            return null;
        }

        @Override
        public @NotNull String toString() {
            return "<UnitType>";
        }
    }

    class OS_Type2_ClassStatement implements OS_Type2 {
        private final ClassStatement _element;

        @Contract(pure = true)
        public OS_Type2_ClassStatement(final ClassStatement aElement) {
            _element = aElement;
        }

        @Override
        public @NotNull ClassStatement getClassOf() {
            return _element;
        }

        @Override
        public OS_Element getElement() {
            return _element;
        }

        @Override
        public boolean isConcreteType(final OS_Element element) {
            return true;
        }

        @Override
        public @NotNull OS_Type2 resolve(final @NotNull Context ctx) {
            return this;
        }

        @Override
        public boolean isUnitType() {
            return false;
        }

        @Override
        public @NotNull OS_Type.Type getType() {
            return OS_Type.Type.USER_CLASS;
        }

        @Override
        public BuiltInTypes getBType() {
            return null;
        }

        @Override
        public TypeName getTypeName() {
            return null;
        }
    }

    class OS_Type2_FunctionDef implements OS_Type2 {
        private final FunctionDef _element;

        public OS_Type2_FunctionDef(final @NotNull FunctionDef aFd) {
            _element = aFd;
        }

        @Override
        public @NotNull ClassStatement getClassOf() {
            return null;
        }

        @Override
        public OS_Element getElement() {
            return _element;
        }

        @Override
        public boolean isConcreteType(final OS_Element element) {
            return false;
        }

        @Override
        public @NotNull OS_Type2 resolve(final @NotNull Context ctx) {
            return null;
        }

        @Override
        public boolean isUnitType() {
            return false;
        }

        @Override
        public @NotNull OS_Type.Type getType() {
            return OS_Type.Type.FUNCTION;
        }

        @Override
        public BuiltInTypes getBType() {
            return null;
        }

        @Override
        public TypeName getTypeName() {
            return null;
        }
    }

    class OS_Type2_TypeName implements OS_Type2 {
        private final TypeName _tn;

        public OS_Type2_TypeName(final @NotNull TypeName aTypeName) {
            _tn = aTypeName;
        }

        @Override
        public @NotNull ClassStatement getClassOf() {
            return null;
        }

        @Override
        public OS_Element getElement() {
            return null;
        }

        @Override
        public boolean isConcreteType(final OS_Element element) {
            return false;
        }

        @Override
        public @NotNull OS_Type2 resolve(final @NotNull Context ctx) {
            return null;
        }

        @Override
        public boolean isUnitType() {
            return false;
        }

        @Override
        public @NotNull OS_Type.Type getType() {
            return OS_Type.Type.USER;
        }

        @Override
        public BuiltInTypes getBType() {
            return null;
        }

        @Override
        public TypeName getTypeName() {
            return _tn;
        }
    }

    class OS_Type2_BuiltInTypes implements OS_Type2 {
        private final BuiltInTypes _bit;

        public OS_Type2_BuiltInTypes(final @NotNull BuiltInTypes aBit) {
            _bit = aBit;
        }

        @Override
        public @NotNull ClassStatement getClassOf() {
            return null;
        }

        @Override
        public OS_Element getElement() {
            return null;
        }

        @Override
        public boolean isConcreteType(final OS_Element element) {
            return false;
        }

        @Override
        public @NotNull OS_Type2 resolve(final @NotNull Context ctx) {
            return null;
        }

        @Override
        public boolean isUnitType() {
            return false;
        }

        @Override
        public @NotNull OS_Type.Type getType() {
            return OS_Type.Type.BUILT_IN;
        }

        @Override
        public BuiltInTypes getBType() {
            return _bit;
        }

        @Override
        public TypeName getTypeName() {
            return null;
        }
    }
}
