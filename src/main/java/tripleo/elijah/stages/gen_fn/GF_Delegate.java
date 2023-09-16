package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.Promise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.NamespaceInvocation;

public class GF_Delegate {
	private final GeneratedFunction generatedFunction;
	private final FunctionDef       fd;

	public GF_Delegate(GeneratedFunction aGeneratedFunction, final @Nullable FunctionDef aFd) {
		generatedFunction = aGeneratedFunction;
		fd                = aFd;
	}

	@Override
	@NotNull
	public String toString() {
		assert fd != null;

		String R = null;

		String pte_string = null; //// = fd.getArgs().toString(); // TODO wanted PTE.getLoggingString


		ClassInvocation     classInvocation     = null; //// = fi.getClassInvocation();
		NamespaceInvocation namespaceInvocation = null; //// = fi.getNamespaceInvocation();

		Promise<GeneratedClass, Void, Void>     crp;
		Promise<GeneratedNamespace, Void, Void> nsrp;


		// README if classInvocation or namespaceInvocation is resolved then use that to return string...

		short state = 0;
		while (state != 5) {
			switch (state) {
			case 0:
				classInvocation = generatedFunction.getFi().getClassInvocation();
				namespaceInvocation = generatedFunction.getFi().getNamespaceInvocation();
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
					R     = String.format("<GeneratedFunction %d %s %s %s>", generatedFunction.getCode(), parent[0], fd.name(), pte_string);
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
					R     = String.format("<GeneratedFunction %d %s %s %s>", generatedFunction.getCode(), parent[0], fd.name(), pte_string);
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

	public String identityString() {
		return String.valueOf(fd);
	}

	public @NotNull FunctionDef getFD() {
		if (fd != null) return fd;
		throw new IllegalStateException("No function");
	}

	public VariableTableEntry getSelf() {
		if (generatedFunction.getFD().getParent() instanceof ClassStatement)
			return generatedFunction.getVarTableEntry(0);
		else
			return null;
	}

	public GNCoded.Role getRole() {
		return GNCoded.Role.FUNCTION;
	}
}
