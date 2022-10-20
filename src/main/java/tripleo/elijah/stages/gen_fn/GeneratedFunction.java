/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.OS_Type;

/**
 * Created 6/27/21 9:40 AM
 */
public class GeneratedFunction extends BaseGeneratedFunction implements GNCoded {
	public final @Nullable FunctionDef fd;

	public GeneratedFunction(final @Nullable FunctionDef functionDef) {
		fd = functionDef;
	}

	//
	// region toString
	//

	@Override
	public String toString() {
		String pte_string = fd.getArgs().toString(); // TODO wanted PTE.getLoggingString
		return String.format("<GeneratedFunction %s %s %s>", fd.getParent(), fd.name(), pte_string);
	}

	public String name() {
		if (fd == null)
			throw new IllegalArgumentException("null fd");
		return fd.name();
	}

	// endregion

	@Override
	public @NotNull BaseFunctionDef getFD() {
		if (fd != null) return fd;
		throw new IllegalStateException("No function");
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
		return ""+fd;
	}

	@Override
	public Role getRole() {
		return Role.FUNCTION;
	}
}

//
//
//
