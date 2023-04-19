package tripleo.elijah.stages.deduce.tastic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_UserClassType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.stages.deduce.*;
import tripleo.elijah.stages.gen_fn.*;
import tripleo.elijah.stages.instructions.FnCallArgs;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;

import static tripleo.elijah.stages.deduce.DeduceTypes2.to_int;

public class FT_FnCallArgs implements ITastic {
	private final DeduceTypes2 deduceTypes2;
	private final FnCallArgs   fca;
	private final ElLog LOG;

	public FT_FnCallArgs(final DeduceTypes2 aDeduceTypes2, final FnCallArgs aO) {
		deduceTypes2 = aDeduceTypes2;
		fca            = aO;
		//
		LOG = aDeduceTypes2.LOG;
	}


	@Override
	public void do_assign_call(final @NotNull BaseEvaFunction generatedFunction,
							   final @NotNull Context ctx,
							   final @NotNull VariableTableEntry vte,
							   final @NotNull Instruction instruction) {
		// NOTE Java is crazy 
		final DeduceTypes2.DeduceClient4 client4 = deduceTypes2.new DeduceClient4(deduceTypes2);
		final DoAssignCall               dac     = new DoAssignCall(client4, generatedFunction);
		dac.do_assign_call(instruction, vte, fca, ctx);
	}

	@Override
	public void do_assign_call(final @NotNull BaseEvaFunction generatedFunction,
							   final @NotNull Context ctx,
							   final @NotNull IdentTableEntry idte,
							   final int instructionIndex) {
		final @NotNull ProcTableEntry pte = generatedFunction.getProcTableEntry(to_int(fca.getArg(0)));
		for (final @NotNull TypeTableEntry tte : pte.getArgs()) {
			LOG.info("771 "+tte);
			final IExpression e = tte.expression;
			if (e == null) continue;
			switch (e.getKind()) {
			case NUMERIC:
			{
				tte.setAttached(new OS_BuiltinType(BuiltInTypes.SystemInteger));
				idte.type = tte; // TODO why not addPotentialType ? see below for example
			}
			break;
			case IDENT:
			{
				final @Nullable InstructionArgument vte_ia = generatedFunction.vte_lookup(((IdentExpression) e).getText());
				final @NotNull List<TypeTableEntry> ll     = deduceTypes2.getPotentialTypesVte((EvaFunction) generatedFunction, vte_ia);
				if (ll.size() == 1) {
					tte.setAttached(ll.get(0).getAttached());
					idte.addPotentialType(instructionIndex, ll.get(0));
				} else
					throw new NotImplementedException();
			}
			break;
			default:
			{
				throw new NotImplementedException();
			}
			}
		}
		{
			final String               s    = ((IdentExpression) pte.expression).getText();
			final LookupResultList     lrl  = ctx.lookup(s);
			final @Nullable OS_Element best = lrl.chooseBest(null);
			if (best != null) {
				pte.setResolvedElement(best);

				// TODO do we need to add a dependency for class, etc?
				if (true || false) {
					if (best instanceof ConstructorDef) {
						// TODO Dont know how to handle this
						int y=2;
					} else if (best instanceof FunctionDef || best instanceof DefFunctionDef) {
						final OS_Element parent = best.getParent();
						IInvocation      invocation;
						if (parent instanceof NamespaceStatement) {
							invocation = new NamespaceInvocation((NamespaceStatement) parent);
						} else if (parent instanceof ClassStatement) {
							invocation = new ClassInvocation((ClassStatement) parent, null);
						} else
							throw new NotImplementedException();

						FunctionInvocation fi = deduceTypes2.newFunctionInvocation((BaseFunctionDef) best, pte, invocation, deduceTypes2.phase);
						generatedFunction.addDependentFunction(fi);
					} else if (best instanceof ClassStatement) {
						GenType genType = new GenType();
						genType.resolved = new OS_UserClassType((ClassStatement) best);
						// ci, typeName, node
						//					genType.
						genType.genCI(null, deduceTypes2, deduceTypes2._errSink(), deduceTypes2._phase());
						generatedFunction.addDependentType(genType);
					}
				}
			} else
				throw new NotImplementedException();
		}
	}
}
