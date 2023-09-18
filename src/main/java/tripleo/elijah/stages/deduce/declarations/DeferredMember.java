/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce.declarations;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.deduce.DeduceElementWrapper;
import tripleo.elijah.stages.deduce.IInvocation;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.GenType;

/**
 * Created 6/27/21 1:41 AM
 */
public class DeferredMember {
	private final     DeferredObject<GenType, Diagnostic, Void> typePromise = new DeferredObject<>();
	private final     DeferredObject<EvaNode, Void, Void>       externalRef = new DeferredObject<>();

	/*@Getter*/ private final IInvocation                               invocation;
	/*@Getter*/ private final DeduceElementWrapper parent;
	/*@Getter*/ private final VariableStatement    variableStatement;

	public DeferredMember(DeduceElementWrapper aParent, IInvocation aInvocation, VariableStatement aVariableStatement) {
		parent            = aParent;
		invocation        = aInvocation;
		variableStatement = aVariableStatement;
	}

	public Promise<EvaNode, Void, Void> externalRef() {
		return externalRef.promise();
	}

	public @NotNull DeferredObject<EvaNode, Void, Void> externalRefDeferred() {
		return externalRef;
	}

	@Override
	public @NotNull String toString() {
		return "DeferredMember{" +
				"parent=" + parent +
				", variableName=" + variableStatement.getName() +
				'}';
	}

	public @NotNull Promise<GenType, Diagnostic, Void> typePromise() {
		return typePromise;
	}

	// for DeducePhase
	public @NotNull DeferredObject<GenType, Diagnostic, Void> typeResolved() {
		return typePromise;
	}

	private final DeferredMemberInjector __inj = new DeferredMemberInjector();

	public DeferredMemberInjector _inj() {
		return __inj;
	}

	public IInvocation getInvocation() {
		return invocation;
	}

	public DeduceElementWrapper getParent() {
		return parent;
	}

	public VariableStatement getVariableStatement() {
		return variableStatement;
	}

	public static class DeferredMemberInjector {

		public DeferredObject<EvaNode, java.lang.Void, java.lang.Void> new_DeferredObject__EvaNode() {
			return new DeferredObject<EvaNode, Void, Void>();
		}

		public DeferredObject<GenType, Diagnostic, Void> new_DeferredObject__GenType() {
			return new DeferredObject<GenType, Diagnostic, Void>();
		}
	}
}

//
//
//
