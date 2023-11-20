/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce.declarations;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.deduce.IInvocation;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;

/**
 * Created 11/21/21 6:32 AM
 */
public class DeferredMemberFunction {
	private final OS_Element                                        parent;
	private final BaseFunctionDef                                   functionDef;
	private final DeferredObject<GenType, Diagnostic, Void>         typePromise = new DeferredObject<GenType, Diagnostic, Void>();
	private final DeferredObject<BaseGeneratedFunction, Void, Void> externalRef = new DeferredObject<BaseGeneratedFunction, Void, Void>();
	private final DeduceTypes2                                      deduceTypes2;
	private final FunctionInvocation                                functionInvocation;
	/**
	 * A {@link tripleo.elijah.stages.deduce.ClassInvocation} or {@link tripleo.elijah.stages.deduce.NamespaceInvocation}.
	 * useless if parent is a {@link tripleo.elijah.stages.deduce.DeduceTypes2.OS_SpecialVariable} and its
	 * {@link tripleo.elijah.stages.deduce.DeduceTypes2.OS_SpecialVariable#memberInvocation} role value is
	 * {@link tripleo.elijah.stages.deduce.DeduceTypes2.MemberInvocation.Role#INHERITED}
	 */
	private       IInvocation                                       invocation;

	public DeferredMemberFunction(final @NotNull OS_Element aParent,
	                              final @Nullable IInvocation aInvocation,
	                              final @NotNull BaseFunctionDef aBaseFunctionDef,
	                              final @NotNull DeduceTypes2 aDeduceTypes2,
	                              final @NotNull FunctionInvocation aFunctionInvocation) { // TODO can this be nullable?
		parent             = aParent;
		invocation         = aInvocation;
		functionDef        = aBaseFunctionDef;
		deduceTypes2       = aDeduceTypes2;
		functionInvocation = aFunctionInvocation;
		//

		if (functionInvocation == null) {
			tripleo.elijah.util.Stupidity.println2("**=== functionInvocation == null ");
			return;
		}

		functionInvocation.generatePromise().then(new DoneCallback<BaseGeneratedFunction>() {
			@Override
			public void onDone(final BaseGeneratedFunction result) {
				deduceTypes2.deduceOneFunction((GeneratedFunction) result, deduceTypes2._phase()); // !!
				result.onType(new DoneCallback<GenType>() {
					@Override
					public void onDone(final GenType result) {
						typePromise.resolve(result);
					}
				});
			}
		});
	}

	public @NotNull Promise<GenType, Diagnostic, Void> typePromise() {
		return typePromise;
	}

	public OS_Element getParent() {
		return parent;
	}

	public IInvocation getInvocation() {
		if (invocation == null) {
			if (parent instanceof DeduceTypes2.OS_SpecialVariable) {
				final DeduceTypes2.OS_SpecialVariable specialVariable = (DeduceTypes2.OS_SpecialVariable) parent;
				invocation = specialVariable.getInvocation(deduceTypes2);
			}
		}
		return invocation;
	}

	public BaseFunctionDef getFunctionDef() {
		return functionDef;
	}

	// for DeducePhase
	public @NotNull DeferredObject<GenType, Diagnostic, Void> typeResolved() {
		return typePromise;
	}

	public Promise<BaseGeneratedFunction, Void, Void> externalRef() {
		return externalRef.promise();
	}

	public @NotNull DeferredObject<BaseGeneratedFunction, Void, Void> externalRefDeferred() {
		return externalRef;
	}

	@Override
	public @NotNull String toString() {
		return "DeferredMemberFunction{" +
		  "parent=" + parent +
		  ", functionName=" + functionDef.name() +
		  '}';
	}

	public FunctionInvocation functionInvocation() {
		return functionInvocation;
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
