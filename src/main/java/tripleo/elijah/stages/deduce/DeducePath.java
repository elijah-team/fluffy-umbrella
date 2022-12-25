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

import org.jdeferred2.DoneCallback;
import org.jdeferred2.FailCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.diagnostic.Diagnostic;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.LookupResultList;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.ProcIA;

import java.util.List;

/**
 * Created 7/9/21 6:10 AM
 */
public class DeducePath {
	private final @NotNull List<InstructionArgument> ias;
	private final IdentTableEntry           base;
	private final OS_Element @NotNull []              elements;  // arrays because they never need to be resized
	private final GenType @NotNull []                 types;
	private final MemberContext @NotNull []           contexts;

	@Contract(pure = true)
	public DeducePath(final IdentTableEntry aIdentTableEntry, @NotNull final List<InstructionArgument> aX) {
		final int size = aX.size();
		assert size > 0;

		base = aIdentTableEntry;
		ias  = aX;

		elements = new OS_Element   [size];
		types    = new GenType      [size];
		contexts = new MemberContext[size];
	}

	public int size() {
		return ias.size();
	}

	public InstructionArgument getIA(final int index) {
		return ias.get(index);
	}

	@Nullable
	public OS_Element getElement(final int aIndex) {
		if (elements[aIndex] == null) {
			final InstructionArgument ia2 = getIA(aIndex);
			@Nullable final OS_Element el;
			if (ia2 instanceof IntegerIA) {
				@NotNull final VariableTableEntry vte = ((IntegerIA) ia2).getEntry();
				el = vte.getResolvedElement();
				assert el != null;
				// set this to set resolved_elements of remaining entries
				vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolderWithIntegerIA(el, (IntegerIA) ias.get(aIndex)));
			} else if (ia2 instanceof IdentIA) {
				@NotNull final IdentTableEntry identTableEntry = ((IdentIA) ia2).getEntry();
				el = identTableEntry.getResolvedElement();
//				if (el == null) {
//					if (aIndex == 0) throw new IllegalStateException();
//					getEntry(aIndex-1).setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(getElement(aIndex-1)));
//					el = identTableEntry.resolved_element;
//				}
				assert el != null;
				if (aIndex == 0)
					if (identTableEntry.getResolvedElement() != el)
						identTableEntry.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(el));
			} else if (ia2 instanceof ProcIA) {
				final @NotNull ProcTableEntry procTableEntry = ((ProcIA) ia2).getEntry();
				el = procTableEntry.getResolvedElement(); // .expression?
				// TODO no setStatus here?
				assert el != null;
			} else
				el = null; // README shouldn't be calling for other subclasses
			elements[aIndex] = el;
			return el;
		} else {
			return elements[aIndex];
		}
	}

	@Nullable
	public BaseTableEntry getEntry(final int aIndex) {
		final InstructionArgument ia2 = getIA(aIndex);
		if (ia2 instanceof IntegerIA) {
			@NotNull final VariableTableEntry vte = ((IntegerIA) ia2).getEntry();
			return vte;
		} else if (ia2 instanceof IdentIA) {
			@NotNull final IdentTableEntry identTableEntry = ((IdentIA) ia2).getEntry();
			return identTableEntry;
		} else if (ia2 instanceof ProcIA) {
			final @NotNull ProcTableEntry procTableEntry = ((ProcIA) ia2).getEntry();
			return procTableEntry;
		}
		return null;
	}

	public @Nullable Context getContext(final int aIndex) {
		if (contexts[aIndex] == null) {
			final @Nullable MemberContext memberContext = new MemberContext(this, aIndex, getElement(aIndex));
			contexts[aIndex] = memberContext;
			return memberContext;
		} else
			return contexts[aIndex];

	}

	public void getElementPromise(final int aIndex, final DoneCallback<OS_Element> aOS_elementDoneCallback, final FailCallback<Diagnostic> aDiagnosticFailCallback) {
		getEntry(aIndex).elementPromise(aOS_elementDoneCallback, aDiagnosticFailCallback);
	}

	static class MemberContext extends Context {

		private final DeducePath deducePath;
		private final int index;
		private final OS_Element element;
		private final @Nullable GenType type;

		public MemberContext(final DeducePath aDeducePath, final int aIndex, final OS_Element aElement) {
			assert aIndex >= 0;

			deducePath = aDeducePath;
			index = aIndex;
			element = aElement;

			type = deducePath.getType(aIndex);
		}

		@Override
		public LookupResultList lookup(final String name, final int level, final LookupResultList Result, final List<Context> alreadySearched, final boolean one) {
//			if (index == 0)
				return type.resolved.getElement().getContext().lookup(name, level, Result, alreadySearched, one);
//			else
//				return null;
		}

		@Override
		public @Nullable Context getParent() {
			if (index == 0)
				return element.getContext().getParent();
			return deducePath.getContext(index - 1);
		}
	}

	public @Nullable GenType getType(final int aIndex) {
		if (types[aIndex] == null) {
			final InstructionArgument ia2 = getIA(aIndex);
			@Nullable final GenType gt;
			if (ia2 instanceof IntegerIA) {
				@NotNull final VariableTableEntry vte = ((IntegerIA) ia2).getEntry();
				gt = vte.type.genType;
				assert gt != null;
			} else if (ia2 instanceof IdentIA) {
				@NotNull final IdentTableEntry identTableEntry = ((IdentIA) ia2).getEntry();
				if (identTableEntry.type != null) {
					gt = identTableEntry.type.genType;
					assert gt != null;
				} else {
					gt = null;
				}
			} else if (ia2 instanceof ProcIA) {
				final @NotNull ProcTableEntry procTableEntry = ((ProcIA) ia2).getEntry();
				gt = null;//procTableEntry.getResolvedElement(); // .expression?
//				assert gt != null;
			} else
				gt = null; // README shouldn't be calling for other subclasses
			types[aIndex] = gt;
			return gt;
		} else {
			return types[aIndex];
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
