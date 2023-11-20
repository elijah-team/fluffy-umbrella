/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.NamespaceInvocation;

/**
 * Created 6/27/21 9:45 AM
 */
public class GeneratedConstructor extends BaseGeneratedFunction {
	public final @Nullable ConstructorDef cd;

	public GeneratedConstructor(final @Nullable ConstructorDef aConstructorDef) {
		cd = aConstructorDef;
	}

	public void setFunctionInvocation(final FunctionInvocation fi) {
		final GenType genType = new GenType();

		// TODO will fail on namespace constructors; next line too
		if (genType.ci instanceof ClassInvocation) {
//			throw new IllegalStateException("34 Needs class invocation");

			final ClassInvocation classInvocation = (ClassInvocation) genType.ci;

			genType.ci       = classInvocation;
			genType.resolved = classInvocation.getKlass().getOS_Type();
		} else if (genType.ci instanceof NamespaceInvocation) {
			final NamespaceInvocation namespaceInvocation = (NamespaceInvocation) genType.ci;

			genType.ci       = namespaceInvocation;
			genType.resolved = namespaceInvocation.getNamespace().getOS_Type();
		}

		genType.node = this;

		resolveTypeDeferred(genType);
	}

	//
	// region toString
	//

	@Override
	public String toString() {
		return String.format("<GeneratedConstructor %s>", cd);
	}

	public String name() {
		if (cd == null)
			throw new IllegalArgumentException("null cd");
		return cd.name();
	}

	// endregion

	@Override
	public @NotNull BaseFunctionDef getFD() {
		if (cd == null) throw new IllegalStateException("No function");
		return cd;
	}

	@Override
	public VariableTableEntry getSelf() {
		if (getFD().getParent() instanceof ClassStatement)
			return getVarTableEntry(0);
		else
			return null;
	}

	@Override
	public String identityString() {
		return ""+cd;
	}

}

//
//
//
