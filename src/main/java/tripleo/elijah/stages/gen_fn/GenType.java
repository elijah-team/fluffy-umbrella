/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.NormalTypeName;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.lang.types.OS_FuncExprType;
import tripleo.elijah.lang.types.OS_FuncType;
import tripleo.elijah.lang.types.OS_UserClassType;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.IInvocation;
import tripleo.elijah.stages.deduce.NamespaceInvocation;
import tripleo.elijah.stages.deduce.zero.Zero_FuncExprType;

import java.util.Objects;

/**
 * Created 5/31/21 1:32 PM
 */
public class GenType {
	public NamespaceStatement resolvedn;
	public OS_Type            typeName; // TODO or just TypeName ??
	public TypeName           nonGenericTypeName;
	public OS_Type            resolved;
	public IInvocation        ci;
	public GeneratedNode      node;
	public FunctionInvocation functionInvocation;

	@Contract(pure = true)
	public GenType(final NamespaceStatement aNamespaceStatement) {
		resolvedn = /*new OS_Type*/(aNamespaceStatement);
	}

	public GenType(final @NotNull ClassStatement aClassStatement) {
		resolved = aClassStatement.getOS_Type();
	}

	public GenType(final OS_Type aAttached,
	               final OS_Type aOS_type,
	               final boolean aB,
	               final TypeName aTypeName,
	               final DeduceTypes2 deduceTypes2,
	               final ErrSink errSink,
	               final DeducePhase phase) {
		typeName = aAttached;
		resolved = aOS_type;
		if (aB) {
			ci = genCI(aTypeName, deduceTypes2, errSink, phase);
		}
	}

	@Override
	public boolean equals(final Object aO) {
		if (this == aO) return true;
		if (aO == null || getClass() != aO.getClass()) return false;

		final GenType genType = (GenType) aO;

		if (!Objects.equals(resolvedn, genType.resolvedn)) return false;
		if (!Objects.equals(typeName, genType.typeName)) return false;
		if (!Objects.equals(nonGenericTypeName, genType.nonGenericTypeName))
			return false;
		if (!Objects.equals(resolved, genType.resolved)) return false;
		if (!Objects.equals(ci, genType.ci)) return false;
		if (!Objects.equals(node, genType.node)) return false;
		return Objects.equals(functionInvocation, genType.functionInvocation);
	}

	@Override
	public int hashCode() {
		int result = resolvedn != null ? resolvedn.hashCode() : 0;
		result = 31 * result + (typeName != null ? typeName.hashCode() : 0);
		result = 31 * result + (nonGenericTypeName != null ? nonGenericTypeName.hashCode() : 0);
		result = 31 * result + (resolved != null ? resolved.hashCode() : 0);
		result = 31 * result + (ci != null ? ci.hashCode() : 0);
		result = 31 * result + (node != null ? node.hashCode() : 0);
		result = 31 * result + (functionInvocation != null ? functionInvocation.hashCode() : 0);
		return result;
	}

	public ClassInvocation genCI(final TypeName aGenericTypeName,
	                             final DeduceTypes2 deduceTypes2,
	                             final ErrSink errSink,
	                             final DeducePhase phase) {
		final SetGenCI        sgci = new SetGenCI();
		final ClassInvocation ci   = sgci.call(this, aGenericTypeName, deduceTypes2, errSink, phase);
		return ci;
	}

	public GenType() {
	}

	public String asString() {
		final String sb = "GenType{" + "resolvedn=" + resolvedn +
		  ", typeName=" + typeName +
		  ", nonGenericTypeName=" + nonGenericTypeName +
		  ", resolved=" + resolved +
		  ", ci=" + ci +
		  ", node=" + node +
		  ", functionInvocation=" + functionInvocation +
		  '}';
		return sb;
	}

	public void set(final @NotNull OS_Type aType) {
		switch (aType.getType()) {
			case USER:
				typeName = aType;
				break;
			case USER_CLASS:
				resolved = aType;
				break;
			default:
				tripleo.elijah.util.Stupidity.println_err2("48 Unknown in set: " + aType);
		}
	}

	public boolean isNull() {
		if (resolvedn != null) return false;
		if (typeName != null) return false;
		if (nonGenericTypeName != null) return false;
		if (resolved != null) return false;
		if (ci != null) return false;
		return node == null;
	}

	public void copy(final GenType aGenType) {
		if (resolvedn == null) resolvedn = aGenType.resolvedn;
		if (typeName == null) typeName = aGenType.typeName;
		if (nonGenericTypeName == null) nonGenericTypeName = aGenType.nonGenericTypeName;
		if (resolved == null) resolved = aGenType.resolved;
		if (ci == null) ci = aGenType.ci;
		if (node == null) node = aGenType.node;
	}

	public void genCIForGenType2(final DeduceTypes2 aDeduceTypes2) {
		genCI(nonGenericTypeName, aDeduceTypes2, aDeduceTypes2._errSink(), aDeduceTypes2._phase());
		final IInvocation invocation = ci;
		if (invocation instanceof NamespaceInvocation) {
			final NamespaceInvocation namespaceInvocation = (NamespaceInvocation) invocation;
			namespaceInvocation.resolveDeferred().then(new DoneCallback<GeneratedNamespace>() {
				@Override
				public void onDone(final GeneratedNamespace result) {
					node = result;
				}
			});
		} else if (invocation instanceof ClassInvocation) {
			final ClassInvocation classInvocation = (ClassInvocation) invocation;
			classInvocation.resolvePromise().then(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(final GeneratedClass result) {
					node = result;
				}
			});
		} else {
			if (resolved instanceof OS_FuncExprType) {
				final OS_FuncExprType funcExprType = (OS_FuncExprType) resolved;

				final Zero_FuncExprType zfet = aDeduceTypes2.getZero(funcExprType);

				node = zfet.genCIForGenType2(aDeduceTypes2);
			} else if (resolved instanceof OS_FuncType) {
				final OS_FuncType funcType = (OS_FuncType) resolved;
				final int         y        = 2;
			} else
				throw new IllegalStateException("invalid invocation");
		}
	}

	/**
	 * Sets the node for a GenType, invocation must already be set
	 */
	public void genNodeForGenType2() {
//		assert aGenType.nonGenericTypeName != null;

		final IInvocation invocation = ci;

		if (invocation instanceof NamespaceInvocation) {
			final NamespaceInvocation namespaceInvocation = (NamespaceInvocation) invocation;
			namespaceInvocation.resolveDeferred().then(new DoneCallback<GeneratedNamespace>() {
				@Override
				public void onDone(final GeneratedNamespace result) {
					node = result;
				}
			});
		} else if (invocation instanceof ClassInvocation) {
			final ClassInvocation classInvocation = (ClassInvocation) invocation;
			classInvocation.resolvePromise().then(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(final GeneratedClass result) {
					node = result;
				}
			});
		} else
			throw new IllegalStateException("invalid invocation");
	}

	static class SetGenCI {

		public ClassInvocation call(@NotNull final GenType genType, final TypeName aGenericTypeName, final @NotNull DeduceTypes2 deduceTypes2, final ErrSink errSink, final DeducePhase phase) {
			if (genType.nonGenericTypeName != null) {
				return nonGenericTypeName(genType, deduceTypes2, errSink, phase);
			}
			if (genType.resolved != null) {
				final OS_Type.Type type = genType.resolved.getType();
				switch (type) {
				case USER_CLASS:
					return ((OS_UserClassType) genType.resolved).resolvedUserClass(genType, aGenericTypeName, phase, deduceTypes2, errSink);
				case FUNCTION:
					return ((OS_FuncType) genType.resolved).resolvedFunction(genType, aGenericTypeName, deduceTypes2, errSink, phase);
				case FUNC_EXPR:
					// TODO what to do here?
					final int y = 2;
					break;
				}
			}
			return null;
		}

		private @NotNull ClassInvocation nonGenericTypeName(final @NotNull GenType genType, final DeduceTypes2 deduceTypes2, final ErrSink errSink, final DeducePhase phase) {
			@NotNull final NormalTypeName aTyn1           = (NormalTypeName) genType.nonGenericTypeName;
			@Nullable final String        constructorName = null; // TODO this comes from nowhere

			switch (genType.resolved.getType()) {
				case GENERIC_TYPENAME:
					final int y = 2; // TODO seems to not be necessary
					assert false;
					return null;
				case USER_CLASS:
					final @NotNull ClassStatement best = genType.resolved.getClassOf();
					//
					ClassInvocation clsinv2 = DeduceTypes2.ClassInvocationMake.withGenericPart(best, constructorName, aTyn1, deduceTypes2, errSink);
					clsinv2 = phase.registerClassInvocation(clsinv2);
					genType.ci = clsinv2;
					return clsinv2;
				default:
					throw new IllegalStateException("Unexpected value: " + genType.resolved.getType());
			}
		}

	}
}

//
//
//
