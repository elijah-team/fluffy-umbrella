package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.deduce.post_bytecode.IDeduceElement3;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.EvaConstructor;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.stateful.State;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.util.ReadySupplier_1;

public class DeduceElement3_Constructor implements IDeduceElement3 {
	private final EvaConstructor evaConstructor;
	private final DeduceTypes2   deduceTypes2;

	public DeduceElement3_Constructor(final EvaConstructor aEvaConstructor, final DeduceTypes2 aDeduceTypes2) {
		evaConstructor = aEvaConstructor;
		deduceTypes2   = aDeduceTypes2;
	}

//	@Override
//	public DED elementDiscriminator() {
//		return null;
//	}

	public void __post_deduce_generated_function_base(final @NotNull DeducePhase aDeducePhase) {
		for (@NotNull IdentTableEntry identTableEntry : evaConstructor.idte_list) {
			if (identTableEntry.getResolvedElement() instanceof final @NotNull VariableStatement vs) {
				final OS_Element el  = vs.getParent().getParent();
				final OS_Element el2 = evaConstructor.getFD().getParent();

				if (el != el2) {
					if (!(el instanceof ClassStatement) && !(el instanceof NamespaceStatement)) {
						continue;
					}

					// NOTE there is no concept of gf here
					aDeducePhase.registerResolvedVariable(identTableEntry, el, vs.getName());
				}
			}
		}
		{
			final @NotNull EvaConstructor gf = evaConstructor;

			@Nullable InstructionArgument result_index = gf.vte_lookup("Result");
			if (result_index == null) {
				// if there is no Result, there should be Value
				result_index = gf.vte_lookup("Value");
				// but Value might be passed in. If it is, discard value
				if (result_index != null) {
					@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
					if (vte.getVtt() != VariableTableType.RESULT) {
						result_index = null;
					}
				}
			}
			if (result_index != null) {
				@NotNull VariableTableEntry vte = ((IntegerIA) result_index).getEntry();
				if (vte.resolvedType() == null) {
					GenType b = vte.getGenType();
					OS_Type a = vte.getType().getAttached();
					if (a != null) {
						// see resolve_function_return_type
						switch (a.getType()) {
						case USER_CLASS:
							dof_uc(vte, a);
							break;
						case USER:
							b.setTypeName(a);
							try {
								@NotNull GenType rt = deduceTypes2.resolve_type(a, a.getTypeName().getContext());
								if (rt.getResolved() != null && rt.getResolved().getType() == OS_Type.Type.USER_CLASS) {
									if (rt.getResolved().getClassOf().getGenericPart().size() > 0)
										b.setNonGenericTypeName(a.getTypeName()); // TODO might be wrong
									dof_uc(vte, rt.getResolved());
								}
							} catch (ResolveError aResolveError) {
								deduceTypes2._errSink().reportDiagnostic(aResolveError);
							}
							break;
						default:
							// TODO do nothing for now
							int y3 = 2;
							break;
						}
					} /*else
							throw new NotImplementedException();*/
				}
			}
		}
//		aDeducePhase.addFunction(aGeneratedConstructor, (FunctionDef) aGeneratedConstructor.getFD()); // TODO do we need this?
	}

	@Override
	public DeduceTypes2 deduceTypes2() {
		return deduceTypes2;
	}

	private void dof_uc(@NotNull VariableTableEntry aVte, @NotNull OS_Type aOSType) {
		// we really want a ci from somewhere
		assert aOSType.getClassOf().getGenericPart().size() == 0;

		@Nullable ClassInvocation ci = new ClassInvocation(aOSType.getClassOf(), null, new ReadySupplier_1<>(deduceTypes2));
		ci = deduceTypes2.phase.registerClassInvocation(ci);

		aVte.getGenType().setResolved(aOSType); // README assuming OS_Type cannot represent namespaces
		aVte.getGenType().setCi(ci);

		ci.resolvePromise().done(aVte::resolveTypeToClass);
	}

	@Override
	public BaseEvaFunction generatedFunction() {
		return evaConstructor;
	}

	@Override
	public GenType genType() {
		return null;
	}

	@Override
	public OS_Element getPrincipal() {
		return evaConstructor.getFD();
	}

	@Override
	public DeduceElement3_Kind kind() {
		return null;
	}

	@Override
	public void mvState(final State aO, final State aCheckEvaClassVarTable) {
		throw new NotImplementedException();
	}

	@Override
	public void resolve(final Context aContext, final DeduceTypes2 dt2) {

	}

	@Override
	public void resolve(final IdentIA aIdentIA, final Context aContext, final FoundElement aFoundElement) {

	}
}
