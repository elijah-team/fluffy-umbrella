/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;

/**
 * Created 5/31/21 12:00 PM
 */
public class NamespaceInvocation implements IInvocation {

	private final DeferredObject<GeneratedNamespace, Void, Void> resolveDeferred = new DeferredObject<GeneratedNamespace, Void, Void>();
	private final NamespaceStatement                             namespaceStatement;

	public NamespaceInvocation(final NamespaceStatement aNamespaceStatement) {
		namespaceStatement = aNamespaceStatement;
	}

	public @NotNull DeferredObject<GeneratedNamespace, Void, Void> resolveDeferred() {
		return resolveDeferred;
	}

	public @NotNull Promise<GeneratedNamespace, Void, Void> resolvePromise() {
		return resolveDeferred.promise();
	}

	public NamespaceStatement getNamespace() {
		return namespaceStatement;
	}

	@Override
	public void setForFunctionInvocation(@NotNull final FunctionInvocation aFunctionInvocation) {
		aFunctionInvocation.setNamespaceInvocation(this);
	}
}

//
//
//
