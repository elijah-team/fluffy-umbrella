package tripleo.elijah.stages.deduce;

import org.junit.Test;
import tripleo.elijah.comp.AccessBus;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.LookupResultList;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.VariableSequence;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_UserClassType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.GeneratePhase;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.util.Helpers;

import java.util.List;

import static org.easymock.EasyMock.*;
import static tripleo.elijah.util.Helpers.List_of;

public class DoAssignCall_ArgsIdent1_Test {
	/*
	    model and test

	    var f1 = factorial(b1)
	 */

	@Test
	public void f1_eq_factorial_b1() {
		final CompilationImpl c = new CompilationImpl(new StdErrSink(), new IO());

		final OS_Module mod = new OS_Module();
		mod.setParent(c);
		mod.setFileName("foo.elijah");

		CompilationEnclosure ce = c.getCompilationEnclosure();

		final AccessBus     accessBus           = new AccessBus(c);
//		ce.provideAccessBus(accessBus);

		final PipelineLogic pipelineLogic = new PipelineLogic(accessBus);
		ce.providePipelineLogic(pipelineLogic);

		final GeneratePhase generatePhase = new GeneratePhase(ce, pipelineLogic);
		final DeducePhase   phase         = new DeducePhase(ce);

		final DeduceTypes2 d = new DeduceTypes2(mod, phase);

		final FunctionDef fd = new FunctionDef(mod, new FunctionContext(null, null));
		fd.setName(Helpers.string_to_ident("no_function_name"));

		final EvaFunction generatedFunction = new EvaFunction(fd);

		final TypeTableEntry self_type    = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, new OS_UserClassType(mock(ClassStatement.class)));
		final int            index_self   = generatedFunction.addVariableTableEntry("self", VariableTableType.SELF, self_type, null);
		final TypeTableEntry result_type  = null;
		final int            index_result = generatedFunction.addVariableTableEntry("Result", VariableTableType.RESULT, result_type, null);
		final OS_Type        b1_attached  = new OS_BuiltinType(BuiltInTypes.SystemInteger);
		final TypeTableEntry b1_type      = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, b1_attached);
		final int             index_b1 = generatedFunction.addVariableTableEntry("b1", VariableTableType.VAR, b1_type, null);
		final FunctionContext ctx      = mock(FunctionContext.class);

		final LookupResultList  lrl_b1 = new LookupResultList();
		final VariableSequence  vs     = new VariableSequence();
		final VariableStatement b1_var = new VariableStatement(vs);
		b1_var.setName(Helpers.string_to_ident("b1"));
		final Context b1_ctx = mock(Context.class);
		lrl_b1.add("b1", 1, b1_var, b1_ctx);

		expect(ctx.lookup("b1")).andReturn(lrl_b1);

		replay(ctx, b1_ctx);

//		final TypeTableEntry vte_tte = null;
//		final OS_Element     el      = null;

		final VariableTableEntry   vte              = generatedFunction.getVarTableEntry(index_self);
		final int                  instructionIndex = -1;
		final List<TypeTableEntry> objects          = List_of();
		final ProcTableEntry       pte              = new ProcTableEntry(-2, null, null, objects);
		final int                  i                = 0;
		final TypeTableEntry       tte              = new TypeTableEntry(-3, TypeTableEntry.Type.SPECIFIED, null, null, null);
		final IdentExpression      identExpression  = Helpers.string_to_ident("b1"); // TODO ctx

		d.do_assign_call_args_ident(generatedFunction, ctx, vte, instructionIndex, pte, i, tte, identExpression);

		d.onExitFunction(generatedFunction, ctx, ctx);

		verify(ctx, b1_ctx);
	}

}
