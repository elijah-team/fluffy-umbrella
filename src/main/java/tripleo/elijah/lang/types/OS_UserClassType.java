package tripleo.elijah.lang.types;

import org.jetbrains.annotations.*;
import tripleo.elijah.comp.*;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.gen_fn.*;

import java.text.*;
import java.util.*;

public class OS_UserClassType extends __Abstract_OS_Type {
	private final @NotNull ClassStatement _classStatement;

	public OS_UserClassType(final @NotNull ClassStatement aClassStatement) {
		_classStatement = aClassStatement;
	}

	@Override
	public OS_Element getElement() {
		return _classStatement;
	}

	@Override
	public Type getType() {
		return Type.USER_CLASS;
	}

	@Override
	public String asString() {
		return MessageFormat.format("<OS_UserClassType {0}>", _classStatement);
	}

	@NotNull
	public ClassInvocation resolvedUserClass(final @NotNull GenType genType, final TypeName aGenericTypeName, final DeducePhase phase, final DeduceTypes2 deduceTypes2, final ErrSink errSink) {
		final ClassStatement   best            = _classStatement;
		@Nullable final String constructorName = null; // TODO what to do about this, nothing I guess

		@NotNull final List<TypeName> gp = best.getGenericPart();
		@Nullable ClassInvocation     clsinv;
		if (genType.ci == null) {
			clsinv = DeduceTypes2.ClassInvocationMake.withGenericPart(best, constructorName, (NormalTypeName) aGenericTypeName, deduceTypes2, errSink);
			if (clsinv == null) return null;
			clsinv     = phase.registerClassInvocation(clsinv);
			genType.ci = clsinv;
		} else
			clsinv = (ClassInvocation) genType.ci;
		return clsinv;
	}

	@Override
	public ClassStatement getClassOf() {
		return _classStatement;
	}

	protected boolean _isEqual(final OS_Type aType) {
		return aType.getType() == Type.USER_CLASS && _classStatement.equals(((OS_UserClassType) aType)._classStatement);
	}
}
