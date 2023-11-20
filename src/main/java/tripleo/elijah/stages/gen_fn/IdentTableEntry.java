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
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.stages.deduce.DeducePath;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.OnType;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created 9/12/20 10:27 PM
 */
public class IdentTableEntry extends BaseTableEntry1 implements Constructable, TableEntryIV, DeduceTypes2.ExpectationBase {
    private final int index;
    private final IdentExpression ident;
	private final Context pc;
	/**
	 * Either an {@link IntegerIA} which is a vte
	 * or a {@link IdentIA} which is an idte
	 */
	public InstructionArgument backlink;
	public @NotNull Map<Integer, TypeTableEntry> potentialTypes = new HashMap<Integer, TypeTableEntry>();
	public TypeTableEntry type;
	public GeneratedNode externalRef;
	public boolean fefi = false;
	private GeneratedNode resolvedType;
	public ProcTableEntry constructable_pte;

	public DeduceTypes2.PromiseExpectation<String> resolveExpectation;

	public IdentTableEntry(final int index, final IdentExpression ident, final Context pc) {
        this.index  = index;
        this.ident  = ident;
        this.pc     = pc;
        addStatusListener(new StatusListener() {
			@Override
			public void onChange(final IElementHolder eh, final Status newStatus) {
				if (newStatus == Status.KNOWN) {
					setResolvedElement(eh.getElement());
				}
			}
		});
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

	public IdentExpression getIdent() {
		return ident;
	}

	@Override
	public void resolveTypeToClass(final GeneratedNode gn) {
		resolvedType = gn;
		if (type != null) // TODO maybe find a more robust solution to this, like another Promise? or just setType? or onPossiblesResolve?
			type.resolve(gn); // TODO maybe this obviates the above?
	}

	private final DeferredObject<GenType, Void, Void> _typeDeferred = new DeferredObject<>();

	public boolean isResolved() {
		return resolvedType != null;
	}

	public GeneratedNode resolvedType() {
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

	public void onType(@NotNull final DeducePhase phase, final OnType callback) {
		phase.onType(this, callback);
	}

	@Override
	public void setConstructable(final ProcTableEntry aPte) {
		constructable_pte = aPte;
	}

	public DeducePath buildDeducePath(final BaseGeneratedFunction generatedFunction) {
		@NotNull final List<InstructionArgument> x = generatedFunction._getIdentIAPathList(new IdentIA(index, generatedFunction));
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

	private final DeferredObject<GenType, Void, Void> fefiDone = new DeferredObject<GenType, Void, Void>();

	public void fefiDone(final GenType aGenType) {
		if (fefiDone.isPending())
			fefiDone.resolve(aGenType);
	}

	public void onFefiDone(final DoneCallback<GenType> aCallback) {
		fefiDone.then(aCallback);
	}

	public InstructionArgument getBacklink() {
		return backlink;
	}

	public void makeType(final BaseGeneratedFunction aGeneratedFunction, final TypeTableEntry.Type aType, final OS_Type aOS_Type) {
		type = aGeneratedFunction.newTypeTableEntry(aType, aOS_Type, getIdent(), this);
	}

	public void makeType(final BaseGeneratedFunction aGeneratedFunction, final TypeTableEntry.Type aType, final IExpression aExpression) {
		type = aGeneratedFunction.newTypeTableEntry(aType, null, aExpression, this);
	}

	@Override
	public void setGenType(final GenType aGenType) {
		if (type != null) {
			type.genType.copy(aGenType);
		}

		if (!_typeDeferred.isResolved()) {
			_typeDeferred.resolve(aGenType);
		}
	}

	public Promise<GenType, Void, Void> typePromise() {
		return _typeDeferred.promise();
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
