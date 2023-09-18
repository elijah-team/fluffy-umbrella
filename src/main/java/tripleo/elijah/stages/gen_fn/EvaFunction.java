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
import tripleo.elijah.nextgen.reactive.DefaultReactive;
import tripleo.elijah.stages.deduce.nextgen.DR_Ident;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created 6/27/21 9:40 AM
 */
public class EvaFunction extends BaseEvaFunction_1 implements GNCoded {
	public final GF_Delegate    GF_Delegate;
	public final List<DR_Ident> _idents = new ArrayList<>();
	private __Reactive _reactive;

	public EvaFunction(final @Nullable FunctionDef functionDef) {
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

	public @NotNull __Reactive reactive() {
		if (_reactive == null)
			_reactive = new __Reactive();
		return _reactive;
	}

	public static class __Reactive extends DefaultReactive {
		@Override
		public <T> void addListener(final Consumer<T> t) {
			int y = 2;
		}

		@Override
		public <T> void addResolveListener(final Consumer<T> aO) {
			throw new NotImplementedException();
		}
	}

	@Override
	public Role getRole() {
		return GF_Delegate.getRole();
	}

	@Override
	public void register(final ICodeRegistrar aCr) {
		throw new NotImplementedException();

	}
}

//
//
//
