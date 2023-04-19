/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_IdentTableEntry;
import tripleo.elijah.stages.deduce.post_bytecode.IDeduceElement3;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.util.Holder;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created 9/12/20 10:27 PM
 */
public class IdentTableEntry extends BaseTableEntry1 implements Constructable, TableEntryIV, DeduceTypes2.ExpectationBase, IDeduceResolvable {
	private final int             index;
	private final IdentExpression ident;
	private final Context         pc;
	public        boolean         preUpdateStatusListenerAdded;
	public @NotNull Map<Integer, TypeTableEntry> potentialTypes = new HashMap<Integer, TypeTableEntry>();
	public          TypeTableEntry               type;
	public          EvaNode                      externalRef;
	public          boolean                      fefi           = false;
	public          ProcTableEntry               constructable_pte;
	public  DeduceTypes2.PromiseExpectation<String> resolveExpectation;
	protected DeferredObject<InstructionArgument, Void, Void> backlinkSet = new DeferredObject<InstructionArgument, Void, Void>();
	InstructionArgument backlink;
	boolean insideGetResolvedElement = false;
	DeferredObject<ProcTableEntry, Void, Void> constructableDeferred = new DeferredObject<>();
	private         EvaNode                      resolvedType;
	private         DeduceElementIdent           dei            = new DeduceElementIdent(this);
	private DeduceElement3_IdentTableEntry          _de3;
	private DeferredObject<GenType, Void, Void> fefiDone = new DeferredObject<GenType, Void, Void>();

	public IdentTableEntry(final int index, final IdentExpression ident, Context pc) {
		this.index = index;
		this.ident = ident;
		this.pc    = pc;
		addStatusListener(new StatusListener() {
			@Override
			public void onChange(IElementHolder eh, Status newStatus) {
				if (newStatus == Status.KNOWN) {
					setResolvedElement(eh.getElement());
				}
			}
		});
		setupResolve();
	}

	@Override
	public OS_Element getResolvedElement() {
		// short circuit
		if (resolved_element != null)
			return resolved_element;

		if (insideGetResolvedElement)
			return null;
		insideGetResolvedElement = true;
		resolved_element         = dei.getResolvedElement();
		insideGetResolvedElement = false;
		return resolved_element;
	}

	public void setDeduceTypes2(final @NotNull DeduceTypes2 aDeduceTypes2, final Context aContext, final @NotNull BaseEvaFunction aGeneratedFunction) {
		dei.setDeduceTypes2(aDeduceTypes2, aContext, aGeneratedFunction);
	}

	public void addPotentialType(final int instructionIndex, final TypeTableEntry tte) {
		potentialTypes.put(instructionIndex, tte);
	}

	@SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
	public @NotNull Collection<TypeTableEntry> potentialTypes() {
		return potentialTypes.values();
	}

	@Override
	public @NotNull String toString() {
		return "IdentTableEntry{" +
				"index=" + index +
				", ident=" + ident +
				", backlink=" + backlink +
				", potentialTypes=" + potentialTypes +
				", status=" + status +
				", type=" + type +
				", resolved=" + resolvedType +
				'}';
	}

	public boolean isResolved() {
		return resolvedType != null;
	}

	public EvaNode resolvedType() {
		return resolvedType;
	}

	public boolean hasResolvedElement() {
		return resolved_element != null;
	}

	public int getIndex() {
		return index;
	}

	public Context getPC() {
		return pc;
	}

	public void onType(@NotNull DeducePhase phase, OnType callback) {
		phase.onType(this, callback);
	}

	// region constructable

	@Override
	public void setConstructable(ProcTableEntry aPte) {
		constructable_pte = aPte;
		if (constructableDeferred.isPending())
			constructableDeferred.resolve(constructable_pte);
		else {
			final Holder<ProcTableEntry> holder = new Holder<ProcTableEntry>();
			constructableDeferred.then(new DoneCallback<ProcTableEntry>() {
				@Override
				public void onDone(final ProcTableEntry result) {
					holder.set(result);
				}
			});
			tripleo.elijah.util.Stupidity.println_err_2(String.format("Setting constructable_pte twice 1) %s and 2) %s", holder.get(), aPte));
		}

	}

	@Override
	public void resolveTypeToClass(EvaNode gn) {
		resolvedType = gn;
		if (type != null) // TODO maybe find a more robust solution to this, like another Promise? or just setType? or onPossiblesResolve?
			type.resolve(gn); // TODO maybe this obviates the above?
	}

	@Override
	public void setGenType(GenType aGenType) {
		if (type != null) {
			type.genType.copy(aGenType);
		} else {
			throw new IllegalStateException("idte-102 Attempting to set a null type");
//			tripleo.elijah.util.Stupidity.println_err_2("idte-102 Attempting to set a null type");
		}
	}

	// endregion constructable

	@Override
	public Promise<ProcTableEntry, Void, Void> constructablePromise() {
		return constructableDeferred.promise();
	}

	public DeducePath buildDeducePath(BaseEvaFunction generatedFunction) {
		@NotNull List<InstructionArgument> x = generatedFunction._getIdentIAPathList(new IdentIA(index, generatedFunction));
		return new DeducePath(this, x);
	}

	@Override
	public String expectationString() {
		return "IdentTableEntry{" +
				"index=" + index +
				", ident=" + ident +
				", backlink=" + backlink +
				"}";
	}

	public void fefiDone(final GenType aGenType) {
		if (fefiDone.isPending())
			fefiDone.resolve(aGenType);
	}

	public Promise<InstructionArgument, Void, Void> backlinkSet() {
		return backlinkSet.promise();
	}

	public void onFefiDone(DoneCallback<GenType> aCallback) {
		fefiDone.then(aCallback);
	}

	/**
	 * Either an {@link IntegerIA} which is a vte
	 * or a {@link IdentIA} which is an idte
	 */
	public InstructionArgument getBacklink() {
		return backlink;
	}

	public void setBacklink(InstructionArgument aBacklink) {
		backlink = aBacklink;
		backlinkSet.resolve(backlink);
	}

	public void makeType(final BaseEvaFunction aGeneratedFunction, final TypeTableEntry.Type aType, final OS_Type aOS_Type) {
		type = aGeneratedFunction.newTypeTableEntry(aType, aOS_Type, getIdent(), this);
	}

	public IdentExpression getIdent() {
		return ident;
	}

	public void makeType(final BaseEvaFunction aGeneratedFunction, final TypeTableEntry.Type aType, final IExpression aExpression) {
		type = aGeneratedFunction.newTypeTableEntry(aType, null, aExpression, this);
	}

	public IDeduceElement3 getDeduceElement3(DeduceTypes2 aDeduceTypes2, BaseEvaFunction aGeneratedFunction) {
		if (_de3 == null) {
			_de3                   = new DeduceElement3_IdentTableEntry(this);
			_de3.deduceTypes2      = aDeduceTypes2;
			_de3.generatedFunction = aGeneratedFunction;
		}
		return _de3;
	}

	public DeduceElementIdent getDeduceElemnt() {
		return dei;
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
