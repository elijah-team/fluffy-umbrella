package tripleo.elijah.lang.types;

import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.LookupResultList;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;


public class OS_UserType extends __Abstract_OS_Type {
	private final TypeName typeName;

	public OS_UserType(final TypeName aTypeName) {
		typeName = aTypeName;
	}

	@Override
	public OS_Element getElement() {
		return null;
	}

	@Override
	public Type getType() {
		return Type.USER;
	}

	@Override
	public OS_Type resolve(final Context ctx) {
		assert ctx != null;

		final LookupResultList r    = ctx.lookup(getTypeName().toString()); // TODO
		final OS_Element       best = r.chooseBest(null);
		return ((ClassStatement) best).getOS_Type();
	}

	@Override
	public TypeName getTypeName() {
		return typeName;
	}

}
