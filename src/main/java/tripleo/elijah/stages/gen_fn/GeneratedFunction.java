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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.NamespaceInvocation;

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
	@NotNull
	public String toString() {
		assert fd != null;

		String R = null;

		String pte_string = null; //// = fd.getArgs().toString(); // TODO wanted PTE.getLoggingString


		ClassInvocation classInvocation = null; //// = fi.getClassInvocation();
		NamespaceInvocation namespaceInvocation = null; //// = fi.getNamespaceInvocation();

		Promise<GeneratedClass, Void, Void>     crp;
		Promise<GeneratedNamespace, Void, Void> nsrp;


		// README if classInvocation or namespaceInvocation is resolved then use that to return string...

		short state = 0;
		while (state != 5) {
			switch (state) {
				case 0:
					classInvocation = fi.getClassInvocation();
					namespaceInvocation = fi.getNamespaceInvocation();
					pte_string = fd.getArgs().toString(); // TODO wanted PTE.getLoggingString

					state = 1;
					break;
				case 1:
					if (classInvocation != null) {
						state = 2;
						break;
					} else if (namespaceInvocation != null) {
						state = 3;
						break;
					} else {
						state = 4;
					}
					break;
				case 2:
					crp = classInvocation.resolvePromise();
					if (crp.isResolved()) {
						final GeneratedClass[] parent = new GeneratedClass[1];
						crp.then(gc -> parent[0] = gc);
						R = String.format("<GeneratedFunction %d %s %s %s>", getCode(), parent[0], fd.name(), pte_string);
						state = 5;
					} else {
						state = 4;
					}
					break;
				case 3:
					nsrp = namespaceInvocation.resolveDeferred();
					if (nsrp.isResolved()) {
						final GeneratedNamespace[] parent = new GeneratedNamespace[1];
						nsrp.then(gc -> parent[0] = gc);
						R = String.format("<GeneratedFunction %d %s %s %s>", getCode(), parent[0], fd.name(), pte_string);
						state = 5;
					} else {
						state = 4;
					}
					break;
				case 4:
					R = String.format("<GeneratedFunction %s %s %s>", fd.getParent(), fd.name(), pte_string);
					state = 5;
					break;
				case 5:
					break;
				default:
					throw new IllegalStateException("Invalid state in #toString");
			}
		}

		// ... otherwise use parsetree parent
		return R;
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
		return "" + fd;
	}

	@Override
	public Role getRole() {
		return Role.FUNCTION;
	}
}

//
//
//
