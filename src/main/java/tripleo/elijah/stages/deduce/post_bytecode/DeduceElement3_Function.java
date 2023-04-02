package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;

public class DeduceElement3_Function implements IDeduceElement3 {
	private final DeduceTypes2    deduceTypes2;
	private final BaseEvaFunction generatedFunction;
	private       GenType         _gt = new GenType();

	public DeduceElement3_Function(final DeduceTypes2 aDeduceTypes2, final BaseEvaFunction aGeneratedFunction) {
		deduceTypes2 = aDeduceTypes2;
		generatedFunction = aGeneratedFunction;
	}

	@Override
	public void resolve(final IdentIA aIdentIA, final Context aContext, final FoundElement aFoundElement) {
		throw new NotImplementedException();
	}

	@Override
	public void resolve(final Context aContext, final DeduceTypes2 dt2) {
		throw new NotImplementedException();
	}

	@Override
	public OS_Element getPrincipal() {
		throw new NotImplementedException();
		//return null;
	}

	@Override
	public DED elementDiscriminator() {
		throw new NotImplementedException();
		//return null;
	}

	@Override
	public DeduceTypes2 deduceTypes2() {
		return deduceTypes2;
	}

	@Override
	public BaseEvaFunction generatedFunction() {
		return generatedFunction;
	}

	@Override
	public GenType genType() {
		return _gt;
	}

	@Override
	public DeduceElement3_Kind kind() {
		return DeduceElement3_Kind.FUNCTION;
	}

	// TODO what about resolved?
	public static final @NotNull GenType unitType = new GenType();
	static {
		unitType.typeName = new OS_BuiltinType(BuiltInTypes.Unit);
	}

	@Nullable
	public GenType resolve_function_return_type_int(final ErrSink errSink) {
		// MODERNIZATION Does this have any affinity with DeferredMember?
		@Nullable final InstructionArgument vte_index = generatedFunction.vte_lookup("Result");
		if (vte_index != null) {
			final @NotNull VariableTableEntry vte = generatedFunction.getVarTableEntry(DeduceTypes2.to_int(vte_index));

			if (vte.type.getAttached() != null) {
				vte.resolveType(vte.type.genType); // TODO doesn't fit pattern of returning and then setting
				return vte.type.genType;
			} else {
				@NotNull Collection<TypeTableEntry> pot1 = vte.potentialTypes();
				@NotNull ArrayList<TypeTableEntry>  pot  = new ArrayList<TypeTableEntry>(pot1);

				switch (pot.size()) {
				case 1:
					return pot.get(0).genType;
				case 0:
					return unitType;
				default:
					// TODO report some kind of error/diagnostic and/or let ForFunction know...
					errSink.reportWarning("Can't resolve type of `Result'. potentialTypes > 1 for " + vte);
					break;
				}
			}
		} else {
			if (generatedFunction instanceof EvaConstructor) {
				// cant set return type of constructors
			} else {
				// if Result is not present, then make function return Unit
				// TODO May not be correct in all cases, such as when Value is present
				// but works for current code structure, where Result is a always present
				return unitType;
			}
		}
		return null;
	}
}
