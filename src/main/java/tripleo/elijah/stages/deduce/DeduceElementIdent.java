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

import org.jdeferred2.Deferred;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.deduce.post_bytecode.IDeduceElement3;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.util.Holder;

/**
 * Created 11/22/21 8:23 PM
 */
public class DeduceElementIdent implements IDeduceElement_old {
	private final IdentTableEntry identTableEntry;
	private final Deferred<OS_Element, Void, Void> _resolvedElementPromise = new DeferredObject<>();
	private       DeduceTypes2    deduceTypes2;
	private       Context         context;
	private       BaseEvaFunction generatedFunction;

	public DeduceElementIdent(final IdentTableEntry aIdentTableEntry) {
		identTableEntry = aIdentTableEntry;
	}

	public void setDeduceTypes2(final DeduceTypes2 aDeduceTypes2, final Context aContext, final @NotNull BaseEvaFunction aGeneratedFunction) {
		deduceTypes2      = aDeduceTypes2;
		context           = aContext;
		generatedFunction = aGeneratedFunction;
	}

	public Promise<OS_Element, Void, Void> resolvedElementPromise() {
		return _resolvedElementPromise.promise();
	}

	public void resolveElement(final OS_Element aElement) {
		_resolvedElementPromise.resolve(aElement);
	}

	public OS_Element getResolvedElement() {
		if (deduceTypes2 == null) { // TODO remove this ASAP. Should never happen
			tripleo.elijah.util.Stupidity.println_err_2("5454 Should never happen. gf is not deduced.");
			return null;
		}

		final Holder<OS_Element> holder = new Holder<>();

		boolean rp = false;

		if (deduceTypes2.hasResolvePending(identTableEntry)) {
			identTableEntry.elementPromise(holder::set, null);
			final DeducePath      dp  = identTableEntry.buildDeducePath(generatedFunction);
			final IDeduceElement3 de3 = identTableEntry.getDeduceElement3(deduceTypes2, generatedFunction);

			rp = true;
		} else {
			deduceTypes2.addResolvePending(identTableEntry, this, holder);
		}

		if (!rp) { // eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
			final IdentIA identIA = new IdentIA(identTableEntry.getIndex(), generatedFunction);

			deduceTypes2.resolveIdentIA_(context, identIA, generatedFunction, new FoundElement(deduceTypes2.phase) {
				@Override
				public void foundElement(final OS_Element e) {
					holder.set(e);
				}

				@Override
				public void noFoundElement() {
					deduceTypes2.LOG.err("DeduceElementIdent: can't resolve element for " + identTableEntry);
				}
			});
		}
		final OS_Element R = holder.get();
		tripleo.elijah.util.Stupidity.println_err_2("8989 " + (R != null));
		return R;
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
