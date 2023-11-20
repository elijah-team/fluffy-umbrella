/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */

/**
 *
 */
package tripleo.elijah.lang.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.NormalTypeName;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.gen_fn.GenType;

import java.text.MessageFormat;
import java.util.List;

public class OS_FuncType extends __Abstract_OS_Type {
	private final FunctionDef function_def;

	@Override
	public OS_Element getElement() {
		return function_def;
	}

	@Override
	public Type getType() {
		return Type.FUNCTION;
	}

	public OS_FuncType(final FunctionDef functionDef) {
		this.function_def = functionDef;
	}

	@Override
	public String toString() {
		return String.format("<OS_FuncType %s>", function_def);
	}

	@Override
	public String asString() {
		return MessageFormat.format("<OS_FuncType {0}>", function_def);
	}

	@NotNull
	public ClassInvocation resolvedFunction(final @NotNull GenType genType, final TypeName aGenericTypeName, final DeduceTypes2 deduceTypes2, final ErrSink errSink, final DeducePhase phase) {
		// TODO what to do here?
		final OS_Element               ele             = function_def;
		final @Nullable ClassStatement best            = (ClassStatement) ele.getParent();//genType.resolved.getClassOf();
		@Nullable final String         constructorName = null; // TODO what to do about this, nothing I guess

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


	protected boolean _isEqual(final OS_Type aType) {
		return aType.getType() == Type.FUNCTION && function_def.equals(aType.getElement());
	}
}



//
//
//
