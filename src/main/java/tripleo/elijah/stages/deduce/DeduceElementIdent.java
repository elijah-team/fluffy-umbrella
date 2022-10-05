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

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.gen_fn.BaseGeneratedFunction;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.util.Holder;

/**
 * Created 11/22/21 8:23 PM
 */
public class DeduceElementIdent {
	private final IdentTableEntry identTableEntry;
	private DeduceTypes2 deduceTypes2;
	private Context context;
	private BaseGeneratedFunction generatedFunction;

	public DeduceElementIdent(final IdentTableEntry aIdentTableEntry) {
		identTableEntry = aIdentTableEntry;
	}

	public void setDeduceTypes2(final DeduceTypes2 aDeduceTypes2, final Context aContext, final @NotNull BaseGeneratedFunction aGeneratedFunction) {
		deduceTypes2 = aDeduceTypes2;
		context = aContext;
		generatedFunction = aGeneratedFunction;
	}

	public OS_Element getResolvedElement() {
		Holder<OS_Element> holder = new Holder<>();
		final IdentIA identIA = new IdentIA(identTableEntry.getIndex(), generatedFunction);
		if (deduceTypes2 != null) { // TODO remove this ASAP. Should never happen
			deduceTypes2.resolveIdentIA_(context, identIA, generatedFunction, new FoundElement(deduceTypes2.phase) {
				@Override
				public void foundElement(final OS_Element e) {
					holder.set(e);
				}

				@Override
				public void noFoundElement() {
					deduceTypes2.LOG.err("DeduceElementIdent: can't resolve element for "+identTableEntry);
				}
			});
		} else
			System.err.println("5454 Should never happen. gf is not deduced.");
		return holder.get();
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
