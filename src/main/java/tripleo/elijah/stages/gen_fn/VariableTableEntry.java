/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.IExpression;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeduceLocalVariable;
import tripleo.elijah.stages.deduce.DeduceTypeResolve;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.foo.DN_Resolver2;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_VariableTableEntry;
import tripleo.elijah.stages.deduce.post_bytecode.PostBC_Processor;
import tripleo.elijah.stages.deduce.zero.VTE_Zero;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.util.Stupidity;

/**
 * Created 9/10/20 4:51 PM
 */
public class VariableTableEntry extends BaseTableEntry1 implements Constructable, TableEntryIV, DeduceTypes2.ExpectationBase {
	private final         VariableTableType                          vtt;
	public final @NotNull Map<Integer, TypeTableEntry>               potentialTypes        = new HashMap<Integer, TypeTableEntry>();
	public final          DeduceLocalVariable                        dlv                   = new DeduceLocalVariable(this);
	final                 DeferredObject<ProcTableEntry, Void, Void> constructableDeferred = new DeferredObject<>();
	private final         int                                        index;
	private final         String                                     name;
	private final         DeferredObject2<GenType, Void, Void>       typeDeferred          = new DeferredObject2<GenType, Void, Void>();
	public                TypeTableEntry                             type;
	public                int                                        tempNum               = -1;
	public                ProcTableEntry                             constructable_pte;
	public    GenType    genType               = new GenType();
	public    OS_Element _vs;
	// endregion constructable
	@Nullable GenType    _resolveTypeCalled    = null;

	private EvaNode                           _resolvedType;
	private DeduceElement3_VariableTableEntry _de3;
	private VTE_Zero _zero;

	private final List<DN_Resolver2> resolvers = new ArrayList<>();

	public VariableTableEntry(final int aIndex, final VariableTableType aVtt, final String aName, final TypeTableEntry aTTE, final OS_Element el) {
		this.index = aIndex;
		this.name  = aName;
		this.vtt   = aVtt;
		this.type  = aTTE;
		this.setResolvedElement(el);
		setupResolve();

		_vs = el;
	}

	public DeduceTypes2 _deduceTypes2() {
		return __dt2;
	}

	@Override
	public IExpression _expression() {
		NotImplementedException.raise();
		return null;
		//this.getResolvedElement();
	}

//	public DeferredObject<GenType, Void, Void> typeDeferred() {
//		return typeDeferred;
//	}

	public void addPotentialType(final int instructionIndex, final TypeTableEntry tte) {
		// TODO 09/14 DR_PossibleType here
		if (!typeDeferred.isPending()) {
			Stupidity.println_err2("62 addPotentialType while typeDeferred is already resolved " + this);//throw new AssertionError();
			return;
		}
		//
		if (!potentialTypes.containsKey(instructionIndex))
			potentialTypes.put(instructionIndex, tte);
		else {
			final TypeTableEntry v = potentialTypes.get(instructionIndex);
			if (v.getAttached() == null) {
				v.setAttached(tte.getAttached());
				// TODO 09/14 wrap all type and type.genType calls?
				type.genType.copy(tte.genType); // README don't lose information
			} else if (tte.lifetime == TypeTableEntry.Type.TRANSIENT && v.lifetime == TypeTableEntry.Type.SPECIFIED) {
				//v.attached = v.attached; // leave it as is
			} else if (tte.lifetime == v.lifetime && v.getAttached() == tte.getAttached()) {
				// leave as is
			} else if (v.getAttached().equals(tte.getAttached())) {
				// leave as is
			} else {
				assert false;
				//
				// Make sure you check the differences between USER and USER_CLASS types
				// May not be any
				//
//				tripleo.elijah.util.Stupidity.println_err2("v.attached: " + v.attached);
//				tripleo.elijah.util.Stupidity.println_err2("tte.attached: " + tte.attached);
				Stupidity.println2("72 WARNING two types at the same location.");
				if ((tte.getAttached() != null && tte.getAttached().getType() != OS_Type.Type.USER) || v.getAttached().getType() != OS_Type.Type.USER_CLASS) {
					// TODO prefer USER_CLASS as we are assuming it is a resolved version of the other one
					if (tte.getAttached() == null)
						tte.setAttached(v.getAttached());
					else if (tte.getAttached().getType() == OS_Type.Type.USER_CLASS)
						v.setAttached(tte.getAttached());
				}
			}
		}
	}

	public DN_Resolver2 addResolver() {
		var delegate = new DN_Resolver2() { // TODO ResolverRunner
			@Override
			public void resolve(final DN_Resolver2 aResolver2) {
				throw new NotImplementedException();
			}
		};
		resolvers.add(delegate);
		return delegate;
	}

	@Override
	public Promise<ProcTableEntry, Void, Void> constructablePromise() {
		return constructableDeferred.promise();
	}

	@Override
	public @NotNull String expectationString() {
		return "VariableTableEntry{" +
		  "index=" + index +
		  ", name='" + name + '\'' +
		  "}";
	}

	public ProcTableEntry getConstructable_pte() {
		return constructable_pte;
	}

	public DeduceElement3_VariableTableEntry getDeduceElement3() {
		if (_de3 == null) {
			_de3 = new DeduceElement3_VariableTableEntry(this);
//			_de3.
		}
		return _de3;
	}

	// region constructable

	public DeduceElement3_VariableTableEntry getDeduceElement3(final DeduceTypes2 aDt2, final BaseEvaFunction aGf1) {
		return getDeduceElement3();
	}

	public DeduceLocalVariable getDlv() {
		return dlv;
	}

	public GenType getGenType() {
		return genType;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public PostBC_Processor getPostBC_Processor(final Context aFd_ctx, final DeduceTypes2.DeduceClient1 aDeduceClient1) {
		return PostBC_Processor.make_VTE(this, aFd_ctx, aDeduceClient1);
	}

	public List<OS_Type> getPotentialTypes() {
		return potentialTypes.values().stream()
		  .map(TypeTableEntry::getAttached)
		  .collect(Collectors.toList());
	}

	public int getTempNum() {
		return tempNum;
	}

	public TypeTableEntry getType() {
		return type;
	}

	public VariableTableType getVtt() {
		return vtt;
	}

	public @NotNull Collection<TypeTableEntry> potentialTypes() {
		return potentialTypes.values();
	}

	public void resolve_var_table_entry_for_exit_function() {
		dlv.resolve_var_table_entry_for_exit_function();
	}

	public EvaNode resolvedType() {
		return _resolvedType;
	}

	public void resolveType(final @NotNull GenType aGenType) {
		if (_resolveTypeCalled != null) { // TODO what a hack
			if (_resolveTypeCalled.resolved != null) {
				if (!aGenType.equals(_resolveTypeCalled)) {
					System.err.printf("** 130 Attempting to replace %s with %s in %s%n", _resolveTypeCalled.asString(), aGenType.asString(), this);
//					throw new AssertionError();
				}
			} else {
				_resolveTypeCalled = aGenType;
				typeDeferred.reset();
				typeDeferred.resolve(aGenType);
			}
			return;
		}
		if (typeDeferred.isResolved()) {
			Stupidity.println_err2("126 typeDeferred is resolved " + this);
		}
		_resolveTypeCalled = aGenType;
		typeDeferred.resolve(aGenType);
	}

	@Override
//	public void setConstructable(final ProcTableEntry aPte) {
//		constructable_pte = aPte;
//	}

//	@Override
	public void resolveTypeToClass(final EvaNode aNode) {
		_resolvedType = aNode;
		genType.node  = aNode;
		type.resolve(aNode); // TODO maybe this obviates above
	}

	@Override
	public void setConstructable(final ProcTableEntry aPte) {
		if (constructable_pte != aPte) {
			constructable_pte = aPte;
			constructableDeferred.resolve(constructable_pte);
		}
	}

	public void setDeduceTypes2(final DeduceTypes2 aDeduceTypes2, final Context aContext, final BaseEvaFunction aGeneratedFunction) {
		dlv.setDeduceTypes2(aDeduceTypes2, aContext, aGeneratedFunction);
	}

	@Override
	public void setGenType(final GenType aGenType) {
		genType.copy(aGenType);
		resolveType(aGenType);
	}

	public void setLikelyType(final GenType aGenType) {
		final GenType bGenType = type.genType;

		// 1. copy arg into member
		bGenType.copy(aGenType);

		// 2. set node when available
		((ClassInvocation) bGenType.ci).resolvePromise().done(aGeneratedClass -> {
			bGenType.node = aGeneratedClass;
			resolveTypeToClass(aGeneratedClass);
			genType = bGenType; // TODO who knows if this is necessary?
		});

	}

	public void setTempNum(int num) {
		tempNum = num;
	}

	public void setType(@NotNull TypeTableEntry tte1) {
		type = tte1;
	}

	@Override
	public @NotNull String toString() {
		return "VariableTableEntry{" +
		  "index=" + index +
		  ", name='" + name + '\'' +
		  ", status=" + status +
		  ", type=" + type.index +
		  ", vtt=" + vtt +
		  ", potentialTypes=" + potentialTypes +
		  '}';
	}

	public boolean typeDeferred_isPending() {
		return typeDeferred.isPending();
	}

	public boolean typeDeferred_isResolved() {
		return typeDeferred.isResolved();
	}

	public Promise<GenType, Void, Void> typePromise() {
		return typeDeferred.promise();
	}

	public DeduceTypeResolve typeResolve() {
		return typeResolve;
	}

	public VariableTableType vtt() {
		return vtt;
	}

	public VTE_Zero zero() {
		if (_zero == null) {
			_zero = new VTE_Zero(this);
		}
		return _zero;
	}

//	public Promise<GenType, Void, Void> typeResolvePromise() {
//		return typeDeferred.promise();
//	}
}

//
//
//
