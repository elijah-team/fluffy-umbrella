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
import tripleo.elijah.lang.FunctionDef;

/**
 * Created 6/27/21 9:40 AM
 */
public class GeneratedFunction extends BaseGeneratedFunction_1 implements GNCoded {
	public final GF_Delegate GF_Delegate;

	public GeneratedFunction(final @Nullable FunctionDef functionDef) {
		GF_Delegate    = new GF_Delegate(this, functionDef);
	}

	//
	// region toString
	//

	@Override
	@NotNull
	public String toString() {


		// README if classInvocation or namespaceInvocation is resolved then use that to return string...

		// ... otherwise use parsetree parent
		return GF_Delegate.toString();
	}

	public String name() {
		return GF_Delegate.name();
	}

	// endregion

	@Override
	public String identityString() {
		return GF_Delegate.identityString();
	}

	@Override
	public @NotNull BaseFunctionDef getFD() {
		return GF_Delegate.getFD();
	}

	@Override
	public VariableTableEntry getSelf() {
		return GF_Delegate.getSelf();
	}

	@Override
	public Role getRole() {
		return GF_Delegate.getRole();
	}
}

//
//
//
