/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah.lang;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang2.BuiltInTypes;

import java.util.Objects;

/**
 *
 * This class represents all the different type of types in the system possible
 *
 * Created on Sep 1, 2005 8:16:32 PM
 *
 * @author Tripleo(sb)
 *
 */
public class OS_Type {

	public OS_Type(final Type t) {
		type_of_type = t;
	}

	public static boolean isConcreteType(final OS_Element element) {
		return element instanceof ClassStatement;
		// enum
		// type
	}

	@Override
	public boolean equals(final Object aO) {
		if (this == aO) return true;
		if (aO == null || getClass() != aO.getClass()) return false;

		final OS_Type os_type = (OS_Type) aO;

		if (type != os_type.type) return false;
		if (type_of_type != os_type.type_of_type) return false;
		if (!Objects.equals(etype, os_type.etype)) return false;
		return Objects.equals(ttype, os_type.ttype);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, type_of_type, etype, ttype);
	}

	public ClassStatement getClassOf() {
		if (etype != null && etype instanceof ClassStatement)
			return (ClassStatement) etype;
		System.err.println("3001 "+etype+" "+ this);
		throw new IllegalArgumentException();
//		return null;
	}

	public OS_Element getElement() {
		if (type_of_type == Type.USER_CLASS) {//		case FUNCTION: // defined in subclass
			return etype;
		}
		throw new IllegalArgumentException();
	}

	public OS_Type resolve(final Context ctx) {
		assert ctx != null;
		switch (getType()) {
		case BUILT_IN:
			{
				//
				// TODO These are technically not right
				//
				switch (getBType()) {
					case SystemInteger: {
						final LookupResultList r;
						OS_Element             best;

						r    = ctx.lookup("SystemInteger");
						best = r.chooseBest(null);
						while (best instanceof AliasStatement) {
							final AliasStatement   aliasStatement = (AliasStatement) best;
							final LookupResultList lrl            = aliasStatement.getContext().lookup(aliasStatement.getExpression().toString());
							best = lrl.chooseBest(null);
						}
						return ((ClassStatement) best).getOS_Type();
					}
				case Boolean:
					{
						final LookupResultList r;
						final OS_Element best;

						r = ctx.lookup("Boolean");
						best = r.chooseBest(null);
						return ((ClassStatement) best).getOS_Type();
					}
				case Unit:
					{
						return new OS_UnitType();
					}
				case String_:
					{
						final LookupResultList r;
						final OS_Element best;

						r = ctx.lookup("String8"); // TODO not sure about this
						best = r.chooseBest(null);
						return ((ClassStatement) best).getOS_Type();
					}
				default:
					throw new IllegalStateException("Unexpected value: " + getBType());
				}
			}
		case USER:
			{
				final LookupResultList r = ctx.lookup(getTypeName().toString()); // TODO
				final OS_Element best = r.chooseBest(null);
				return ((ClassStatement) best).getOS_Type();
			}
		case USER_CLASS:
		case FUNCTION:
			return this;
		default:
			throw new IllegalStateException("can't be here.");
		}
	}

	public boolean isUnitType() {
		return false;
	}

	public enum Type {
		BUILT_IN, USER, USER_CLASS, FUNC_EXPR, UNIT_TYPE, UNKNOWN, ANY, FUNCTION, GENERIC_TYPENAME
	}

	public Type getType() {
		return type_of_type;
	}

	private BuiltInTypes type;
	protected @NotNull final Type type_of_type;
	private OS_Element etype;
	private TypeName ttype;

	/*@ ensures type_of_type = Type.BUILT_IN; */
	public OS_Type(final BuiltInTypes aType) {
		this.type = aType;
		this.type_of_type = Type.BUILT_IN;
	}

	/*@ ensures type_of_type = Type.USER_CLASS; */
	public OS_Type(final ClassStatement klass) {
		assert klass != null;
		this.etype = klass;
		this.type_of_type = Type.USER_CLASS;
	}

	/*@ ensures type_of_type = Type.USER; */
	public OS_Type(final @NotNull TypeName typeName) {
		if (typeName.isNull())
			System.err.println("170 null typeName in OS_Type");//throw new AssertionError();
		this.ttype = typeName;
		this.type_of_type = Type.USER;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("<OS_Type %s %s %s %s>", ttype, etype, type, type_of_type);
	}

	/*@ requires type_of_type = Type.BUILT_IN; */
	public BuiltInTypes getBType() {
		return type;
	}

	/*@ requires type_of_type = Type.USER; */
	public TypeName getTypeName() {
		return ttype;
	}

	public static class OS_UnitType extends OS_Type {

		public OS_UnitType() {
			super(Type.UNIT_TYPE);
		}

		@Override
		public boolean isUnitType() {
			return true;
		}

		@Override
		public String toString() {
			return "<UnitType>";
		}
	}
}

//
//
//
