package tripleo.elijah.lang.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.NormalTypeName;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.gen_fn.GenType;

import java.util.List;

public class OS_UserClassType extends __Abstract_OS_Type {
	private final ClassStatement _classStatement;

	public OS_UserClassType(final ClassStatement aClassStatement) {
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
}
