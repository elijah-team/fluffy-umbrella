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

//import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.deduce.DeducePath;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.IDeduceResolvable;
import tripleo.elijah.stages.deduce.ITE_Resolver;
import tripleo.elijah.stages.deduce.OnType;
import tripleo.elijah.stages.deduce.PromiseExpectation;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.deduce.Resolve_Ident_IA;
import tripleo.elijah.stages.deduce.nextgen.DN_Resolver;
import tripleo.elijah.stages.deduce.nextgen.DN_ResolverRejection;
import tripleo.elijah.stages.deduce.nextgen.DN_ResolverResolution;
import tripleo.elijah.stages.deduce.nextgen.DR_Ident;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_IdentTableEntry;
import tripleo.elijah.stages.deduce.zero.ITE_Zero;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.util.Holder;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created 9/12/20 10:27 PM
 */
public class IdentTableEntry extends BaseTableEntry1 implements Constructable, TableEntryIV, DeduceTypes2.ExpectationBase, IDeduceResolvable {
	private final @NotNull Map<Integer, TypeTableEntry>                    potentialTypes = new HashMap<>();
	protected final        DeferredObject<InstructionArgument, Void, Void> backlinkSet    = new DeferredObject<>();
	final                 DeferredObject<ProcTableEntry, Void, Void>      constructableDeferred = new DeferredObject<>();
	private final         int                                             index;
	private final         IdentExpression                                 ident;
	private final Context                             pc;
	private final Resolve_Ident_IA.DeduceElementIdent dei      = new Resolve_Ident_IA.DeduceElementIdent(this);
	private final DeferredObject<GenType, Void, Void> fefiDone = new DeferredObject<>();
	public VariableStatement _cheat_variableStatement;
	private                boolean                                         preUpdateStatusListenerAdded;
	private TypeTableEntry type;
	private EvaNode        externalRef;
	private boolean        fefi           = false;
	private ProcTableEntry                          constructable_pte;
	private PromiseExpectation<String> resolveExpectation;
	InstructionArgument backlink;
	boolean             insideGetResolvedElement = false;
	private EvaNode                        resolvedType;
	private DeduceElement3_IdentTableEntry _de3;
	private ITE_Zero                       _zero;

	public IdentTableEntry(final int index, final IdentExpression ident, final Context pc) {
		this.index = index;
		this.ident = ident;
		this.pc    = pc;
		addStatusListener(new StatusListener() {
			@Override
			public void onChange(final IElementHolder eh, final Status newStatus) {
				if (newStatus == Status.KNOWN) {
					setResolvedElement(eh.getElement());
				}
			}
		});
		setupResolve();
	}

	public IdentTableEntry(int aI, IdentExpression aIdentExpression, Context aContext,
			BaseEvaFunction aGeneratedFunction) {
		this(aI, aIdentExpression, aContext);
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

	public void addPotentialType(final int instructionIndex, final TypeTableEntry tte) {
		potentialTypes.put(instructionIndex, tte);
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

	public void onType(@NotNull final DeducePhase phase, final OnType callback) {
		phase.onType(this, callback);
	}

	//	@SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
	public @NotNull Collection<TypeTableEntry> potentialTypes() {
		return potentialTypes.values();
	}

	@Override
	public void setConstructable(final ProcTableEntry aPte) {
		constructable_pte = aPte;
		if (constructableDeferred.isPending())
			constructableDeferred.resolve(constructable_pte);
		else {
			final Holder<ProcTableEntry> holder = new Holder<>();
			constructableDeferred.then(holder::set);
			System.err.printf("Setting constructable_pte twice 1) %s and 2) %s%n", holder.get(), aPte);
		}
	}

	// region constructable

	@Override
	public void resolveTypeToClass(final EvaNode gn) {
		resolvedType = gn;
		if (type != null) // TODO maybe find a more robust solution to this, like another Promise? or just setType? or onPossiblesResolve?
			type.resolve(gn); // TODO maybe this obviates the above?
	}

	@Override
	public void setGenType(final GenType aGenType) {
		if (type != null) {
			type.genType.copy(aGenType);
		} else {
			throw new IllegalStateException("idte-102 Attempting to set a null type");
//			tripleo.elijah.util.Stupidity.println_err2("idte-102 Attempting to set a null type");
		}
	}

	@Override
	public Promise<ProcTableEntry, Void, Void> constructablePromise() {
		return constructableDeferred.promise();
	}

	public void setGenType(final GenType genType, final BaseEvaFunction gf) {
		if (type == null) {
			makeType(gf, TypeTableEntry.Type.SPECIFIED, genType.resolved);
		}

		type.genType.copy(genType);
	}

	// endregion constructable

	public void makeType(final BaseEvaFunction aGeneratedFunction, final TypeTableEntry.Type aType, final OS_Type aOS_Type) {
		type = aGeneratedFunction.newTypeTableEntry(aType, aOS_Type, getIdent(), this);
	}

	public IdentExpression getIdent() {
		return ident;
	}

	public void setDeduceTypes2(final @NotNull DeduceTypes2 aDeduceTypes2, final Context aContext, final @NotNull BaseEvaFunction aGeneratedFunction) {
		dei.setDeduceTypes2(aDeduceTypes2, aContext, aGeneratedFunction);
	}

	public DeducePath buildDeducePath(final BaseEvaFunction generatedFunction) {
		@NotNull final List<InstructionArgument> x = BaseEvaFunction._getIdentIAPathList(new IdentIA(index, generatedFunction));
		return new DeducePath(this, x);
	}

	public void fefiDone(final GenType aGenType) {
		if (fefiDone.isPending())
			fefiDone.resolve(aGenType);
	}

	@Override
	public String expectationString() {
		return "IdentTableEntry{" +
		  "index=" + index +
		  ", ident=" + ident +
		  ", backlink=" + backlink +
		  "}";
	}

	public Promise<InstructionArgument, Void, Void> backlinkSet() {
		return backlinkSet.promise();
	}

	public void onFefiDone(final DoneCallback<GenType> aCallback) {
		fefiDone.then(aCallback);
	}

	/**
	 * Either an {@link IntegerIA} which is a vte
	 * or a {@link IdentIA} which is an idte
	 */
	public InstructionArgument getBacklink() {
		return backlink;
	}

	public void setBacklink(final InstructionArgument aBacklink) {
		backlink = aBacklink;
		backlinkSet.resolve(backlink);
	}

	public void makeType(final BaseEvaFunction aGeneratedFunction, final TypeTableEntry.Type aType, final IExpression aExpression) {
		type = aGeneratedFunction.newTypeTableEntry(aType, null, aExpression, this);
	}

	public DeduceElement3_IdentTableEntry getDeduceElement3(final DeduceTypes2 aDeduceTypes2, final BaseEvaFunction aGeneratedFunction) {
		if (_de3 == null) {
			_de3                   = new DeduceElement3_IdentTableEntry(this);
			_de3.deduceTypes2      = aDeduceTypes2;
			_de3.generatedFunction = aGeneratedFunction;
		}
		return _de3;
	}

	public ITE_Zero zero() {
		if (_zero == null) {
			_zero = new ITE_Zero(this);
		}
		return _zero;
	}

	private final List<DN_Resolver>                       resolvers                 = new ArrayList<>();
	public DeferredObject<OS_Element, ResolveError, Void> _p_resolvedElementPromise = new DeferredObject<OS_Element, ResolveError, Void>();

	public DN_Resolver newResolver(final Context aCtx, final BaseEvaFunction aGeneratedFunction) {
		ITE_DefaultResolver x = new ITE_DefaultResolver(aCtx, aGeneratedFunction, this);
		resolvers.add(x);
		return x;
	}

	@Override
	public IExpression _expression() {
		return this.ident;
	}

	public Map<Integer, TypeTableEntry> getPotentialTypes() {
		return potentialTypes;
	}

	public boolean isPreUpdateStatusListenerAdded() {
		return preUpdateStatusListenerAdded;
	}

	public void setPreUpdateStatusListenerAdded(boolean aPreUpdateStatusListenerAdded) {
		preUpdateStatusListenerAdded = aPreUpdateStatusListenerAdded;
	}

	public TypeTableEntry getType() {
		return type;
	}

	public void setType(TypeTableEntry aType) {
		type = aType;
	}

	public EvaNode getExternalRef() {
		return externalRef;
	}

	public void setExternalRef(EvaNode aExternalRef) {
		externalRef = aExternalRef;
	}

	public boolean isFefi() {
		return fefi;
	}

	public void setFefi(boolean aFefi) {
		fefi = aFefi;
	}

	public ProcTableEntry getConstructable_pte() {
		return constructable_pte;
	}

	public void setConstructable_pte(ProcTableEntry aConstructable_pte) {
		constructable_pte = aConstructable_pte;
	}

	public PromiseExpectation<String> getResolveExpectation() {
		return resolveExpectation;
	}

	public void setResolveExpectation(PromiseExpectation<String> aResolveExpectation) {
		resolveExpectation = aResolveExpectation;
	}

	public DeduceTypes2 _deduceTypes2() {
		return this._deduceTypes2;
	}

	public DeduceElement3_IdentTableEntry getDeduceElement3() {
		return _de3;
	}

	public void addResolver(final ITE_Resolver aResolver000) {
		throw new NotImplementedException();
	}

	public DR_Ident get_ident() {
		return null;
	}

	public Resolve_Ident_IA.DeduceElementIdent getDeduceElement() {
//		return _de3;
		throw new NotImplementedException();

	}

	public void onResolvedElement(final Consumer<OS_Element> ce) {
		throw new NotImplementedException();
	}

	public Object getDefinedIdent() {
		throw new NotImplementedException();
	}

	public void resolvers_round() {
		throw new NotImplementedException();
	}

	public Object externalRef() {
		throw new NotImplementedException();
	}

	public void onExternalRef(final Consumer<EvaNode> a) {
		throw new NotImplementedException();
	}

	public void calculateResolvedElement() {
		throw new NotImplementedException();

	}

	class ITE_DefaultResolver implements DN_Resolver {

		private final Context ctx;
		private final BaseEvaFunction generatedFunction;
		private final BaseTableEntry bb;

		public ITE_DefaultResolver(final Context aCtx, final BaseEvaFunction aGeneratedFunction, final BaseTableEntry aBb) {
			ctx = aCtx;
			generatedFunction = aGeneratedFunction;
			bb = aBb;
		}

		@Override
		public void resolve(final DN_ResolverResolution aResolution) {
			aResolution.apply();
//			throw new NotImplementedException();
		}

		@Override
		public void reject(final DN_ResolverRejection aRejection) {
			aRejection.print_message(bb, IdentTableEntry.this);
		}
	}

	public record ITE_Resolver_Result(OS_Element element) {
	}

//	private final DeferredObject<GenType, Void, Void> typeDeferred = new DeferredObject<GenType, Void, Void>();
//
//	public Promise<GenType, Void, Void> typeResolvePromise() {
//		return typeDeferred.promise();
//	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
