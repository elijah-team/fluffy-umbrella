package tripleo.elijah.stages.deduce;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.*;
import tripleo.elijah.util.NotImplementedException;

import java.util.Collection;
import java.util.List;

class Implement_construct {

	private final DeduceTypes2 deduceTypes2;
	private final BaseGeneratedFunction generatedFunction;
	private final Instruction instruction;
	private final InstructionArgument expression;

	private final @NotNull ProcTableEntry pte;

	public Implement_construct(DeduceTypes2 aDeduceTypes2, BaseGeneratedFunction aGeneratedFunction, Instruction aInstruction) {
		deduceTypes2 = aDeduceTypes2;
		generatedFunction = aGeneratedFunction;
		instruction = aInstruction;

		// README all these asserts are redundant, I know
		assert instruction.getName() == InstructionName.CONSTRUCT;
		assert instruction.getArg(0) instanceof ProcIA;

		final int pte_num = ((ProcIA) instruction.getArg(0)).getIndex();
		pte = generatedFunction.getProcTableEntry(pte_num);

		expression = pte.expression_num;

		assert expression instanceof IntegerIA || expression instanceof IdentIA;
	}

	public void action() {
		if (expression instanceof IntegerIA) {
			action_IntegerIA();
		} else if (expression instanceof IdentIA) {
			action_IdentIA();
		} else {
			throw new NotImplementedException();
		}
	}

	public void action_IdentIA() {
		@NotNull IdentTableEntry idte = ((IdentIA) expression).getEntry();
		DeducePath deducePath = idte.buildDeducePath(generatedFunction);
		{
			@Nullable OS_Element el3;
			@Nullable Context ectx = generatedFunction.getFD().getContext();
			for (int i = 0; i < deducePath.size(); i++) {
				InstructionArgument ia2 = deducePath.getIA(i);

				el3 = deducePath.getElement(i);

				if (ia2 instanceof IntegerIA) {
					@NotNull VariableTableEntry vte = ((IntegerIA) ia2).getEntry();
					// TODO will fail if we try to construct a tmp var, but we never try to do that
					assert vte.vtt != VariableTableType.TEMP;
					assert el3 != null;
					assert i == 0;
					ectx = deducePath.getContext(i);
				} else if (ia2 instanceof IdentIA) {
					@NotNull IdentTableEntry idte2 = ((IdentIA) ia2).getEntry();
					final String s = idte2.getIdent().toString();
					LookupResultList lrl = ectx.lookup(s);
					@Nullable OS_Element el2 = lrl.chooseBest(null);
					if (el2 == null) {
						assert el3 instanceof VariableStatement;
						@Nullable VariableStatement vs = (VariableStatement) el3;
						@NotNull TypeName tn = vs.typeName();
						@NotNull OS_Type ty = new OS_Type(tn);

						if (idte2.type == null) {
							// README Don't remember enough about the constructors to select a different one
							@NotNull TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, ty);
							try {
								@NotNull GenType resolved = deduceTypes2.resolve_type(ty, tn.getContext());
								deduceTypes2.LOG.err("892 resolved: " + resolved);
								tte.setAttached(resolved);
							} catch (ResolveError aResolveError) {
								deduceTypes2.errSink.reportDiagnostic(aResolveError);
							}

							idte2.type = tte;
						}
						// s is constructor name
						implement_construct_type(idte2, ty, s);
						return;
					} else {
						if (i + 1 == deducePath.size()) {
							assert el3 == el2;
							if (el2 instanceof ConstructorDef) {
								@Nullable GenType type = deducePath.getType(i);
								if (type.nonGenericTypeName == null) {
									type.nonGenericTypeName = deducePath.getType(i - 1).nonGenericTypeName; // HACK. not guararnteed to work!
								}
								@NotNull OS_Type ty = new OS_Type(type.nonGenericTypeName);
								implement_construct_type(idte2, ty, s);
							}
						} else {
							ectx = deducePath.getContext(i);
						}
					}
//						implement_construct_type(idte/*??*/, ty, null); // TODO how bout when there is no ctor name
				} else {
					throw new NotImplementedException();
				}
			}
		}
	}

	public void action_IntegerIA() {
		@NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(((IntegerIA) expression).getIndex());
		assert vte.type.getAttached() != null; // TODO will fail when empty variable expression
		@Nullable OS_Type ty = vte.type.getAttached();
		implement_construct_type(vte, ty, null);
	}

	private void implement_construct_type(@Nullable Constructable co, @Nullable OS_Type aTy, String constructorName) {
		assert aTy != null;
		if (aTy.getType() == OS_Type.Type.USER) {
			TypeName tyn = aTy.getTypeName();
			if (tyn instanceof NormalTypeName) {
				final @NotNull NormalTypeName tyn1 = (NormalTypeName) tyn;
				_implement_construct_type(co, constructorName, (NormalTypeName) tyn);
			}
		} else
			throw new NotImplementedException();
		if (co != null) {
			co.setConstructable(pte);
			ClassInvocation best = pte.getClassInvocation();
			assert best != null;
			best.resolvePromise().done(new DoneCallback<GeneratedClass>() {
				@Override
				public void onDone(GeneratedClass result) {
					co.resolveTypeToClass(result);
				}
			});
		}
	}

	private void _implement_construct_type(@Nullable Constructable co, @Nullable String constructorName, @NotNull NormalTypeName aTyn1) {
		String s = aTyn1.getName();
		LookupResultList lrl = aTyn1.getContext().lookup(s);
		@Nullable OS_Element best = lrl.chooseBest(null);
		assert best instanceof ClassStatement;
		@NotNull List<TypeName> gp = ((ClassStatement) best).getGenericPart();
		@Nullable ClassInvocation clsinv = new ClassInvocation((ClassStatement) best, constructorName);
		if (gp.size() > 0) {
			TypeNameList gp2 = aTyn1.getGenericPart();
			for (int i = 0; i < gp.size(); i++) {
				final TypeName typeName = gp2.get(i);
				@NotNull GenType typeName2;
				try {
					// TODO transition to GenType
					typeName2 = deduceTypes2.resolve_type(new OS_Type(typeName), typeName.getContext());
					clsinv.set(i, gp.get(i), typeName2.resolved);
				} catch (ResolveError aResolveError) {
					aResolveError.printStackTrace();
				}
			}
		}
		clsinv = deduceTypes2.phase.registerClassInvocation(clsinv);
		if (co != null) {
			if (co instanceof IdentTableEntry) {
				final @Nullable IdentTableEntry idte3 = (IdentTableEntry) co;
				idte3.type.genTypeCI(clsinv);
				clsinv.resolvePromise().then(new DoneCallback<GeneratedClass>() {
					@Override
					public void onDone(GeneratedClass result) {
						idte3.resolveTypeToClass(result);
					}
				});
			} else if (co instanceof VariableTableEntry) {
				final @NotNull VariableTableEntry vte = (VariableTableEntry) co;
				vte.type.genTypeCI(clsinv);
				clsinv.resolvePromise().then(new DoneCallback<GeneratedClass>() {
					@Override
					public void onDone(GeneratedClass result) {
						vte.resolveTypeToClass(result);
					}
				});
			}
		}
		pte.setClassInvocation(clsinv);
		pte.setResolvedElement(best);
		// set FunctionInvocation with pte args
		{
			@Nullable ConstructorDef cc = null;
			if (constructorName != null) {
				Collection<ConstructorDef> cs = ((ClassStatement) best).getConstructors();
				for (@NotNull ConstructorDef c : cs) {
					if (c.name().equals(constructorName)) {
						cc = c;
						break;
					}
				}
			}
			// TODO also check arguments
			{
				if (cc == null) assert pte.getArgs().size() == 0;
				@NotNull FunctionInvocation fi = deduceTypes2.newFunctionInvocation(cc, pte, clsinv, deduceTypes2.phase);
				pte.setFunctionInvocation(fi);
			}
		}
	}
}
