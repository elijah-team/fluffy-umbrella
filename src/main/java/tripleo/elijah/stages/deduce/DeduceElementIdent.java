/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;

/**
 * Created 11/22/21 8:23 PM
 */
public class DeduceElementIdent {
	private final IdentTableEntry                              identTableEntry;
	private final DeferredObject<OS_Element, Diagnostic, Void> _resolvedElementPromise = new DeferredObject<>();
	private       DeduceTypes2                                 deduceTypes2;
	private       Context                                      context;
	private       BaseGeneratedFunction                        generatedFunction;

	public DeduceElementIdent(final IdentTableEntry aIdentTableEntry) {
		identTableEntry = aIdentTableEntry;
	}

	public void setDeduceTypes2(final DeduceTypes2 aDeduceTypes2, final Context aContext, final @NotNull BaseGeneratedFunction aGeneratedFunction) {
		deduceTypes2      = aDeduceTypes2;
		context           = aContext;
		generatedFunction = aGeneratedFunction;

		calculateResolvedObject();
	}

	private void calculateResolvedObject() {
		final IdentIA identIA = new IdentIA(identTableEntry.getIndex(), generatedFunction);

		if (deduceTypes2 == null) { // TODO remove this ASAP. Should never happen
			throw new IllegalStateException("5454 Should never happen. gf is not deduced.");
		}

		try {
			resolveIdentIA_(context, identIA, generatedFunction, new FoundElement(deduceTypes2.phase) {
				@Override
				public void foundElement(final OS_Element e) {
					if (_resolvedElementPromise.isPending())
						_resolvedElementPromise.resolve(e);
				}

				@Override
				public void noFoundElement() {
					final @NotNull CantResolveElement err = new CantResolveElement(
					  "DeduceElementIdent: can't resolve element for " + identTableEntry,
					  identTableEntry,
					  generatedFunction);

					if (_resolvedElementPromise.isPending())
						_resolvedElementPromise.reject(err);

//					deduceTypes2.LOG.err(err.message);
				}
			});
		} catch (final ResolveError aE) {
			System.err.println("** 72 ResolveError: " + identTableEntry.getIdent().getText());
			if (_resolvedElementPromise.isPending())
				_resolvedElementPromise.reject(aE); // TODO feb 20
		}
	}

	private void resolveIdentIA_(@NotNull final Context context, @NotNull final IdentIA identIA, final BaseGeneratedFunction generatedFunction, @NotNull final FoundElement foundElement) throws ResolveError {
		final DeduceTypes2.DeduceClient3 aDeduceClient3 = new DeduceTypes2.DeduceClient3(deduceTypes2);
		final Resolve_Ident_IA           ria            = new Resolve_Ident_IA(aDeduceClient3, context, identIA, generatedFunction, foundElement, deduceTypes2._errSink());
		ria.action();
	}

	private DeferredObject<OS_Element, Diagnostic, Void> resolvedElementPromise() {
		return _resolvedElementPromise;
	}

	public OS_Element getResolvedElement() {
		if (_resolvedElementPromise.isResolved()) {
			final OS_Element[] R = new OS_Element[1];
			_resolvedElementPromise.then(r -> R[0] = r);
			return R[0];
		}

		return null;
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
