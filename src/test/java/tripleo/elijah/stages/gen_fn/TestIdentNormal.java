/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.test_help.Boilerplate;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 3/4/21 3:53 AM
 */
public class TestIdentNormal {

	@Test(expected = IllegalStateException.class) // TODO proves nothing
	public void test() {

		final FunctionDef fd   = mock(FunctionDef.class);
		final Context ctx1 = mock(Context.class);
		final Context ctx2 = mock(Context.class);

		final Boilerplate boilerplate = new Boilerplate();
		boilerplate.get();
		boilerplate.getGenerateFiles(boilerplate.defaultMod());

		final GeneratePhase   generatePhase = boilerplate.pipelineLogic.generatePhase;

		final GenerateFunctions generateFunctions = new GenerateFunctions(generatePhase, boilerplate.defaultMod(), boilerplate.pipelineLogic);

		final EvaFunction      generatedFunction = new EvaFunction(fd);
		final VariableSequence seq               = new VariableSequence(ctx1);
		final VariableStatement vs  = new VariableStatement(seq);
		final IdentExpression   x   = IdentExpression.forString("x");
		vs.setName(x);
		final IdentExpression         foo = IdentExpression.forString("foo");
		final ProcedureCallExpression pce = new ProcedureCallExpression();
		pce.setLeft(new DotExpression(x, foo));

		final InstructionArgument                s = generateFunctions.simplify_expression(pce, generatedFunction, ctx2);
		@NotNull final List<InstructionArgument> l = BaseEvaFunction._getIdentIAPathList(s);
		tripleo.elijah.util.Stupidity.println_out_2(""+l);
//      tripleo.elijah.util.Stupidity.println_out_2(generatedFunction.getIdentIAPathNormal());

		//
		//
		//

		final LookupResultList lrl = new LookupResultList();
		lrl.add("x", 1, vs, ctx2);
		when(ctx2.lookup("x")).thenReturn(lrl);

		//
		//
		//

		final IdentIA identIA = new IdentIA(1, generatedFunction);

		final DeducePhase  phase = boilerplate.pr.pipelineLogic.dp;
		final DeduceTypes2 d2    = new DeduceTypes2(boilerplate.defaultMod(), phase);

		final List<InstructionArgument> ss = BaseEvaFunction._getIdentIAPathList(identIA);
		d2.resolveIdentIA2_(ctx2, null, ss/*identIA*/, generatedFunction, new FoundElement(phase) {
			@Override
			public void foundElement(final OS_Element e) {
				tripleo.elijah.util.Stupidity.println_out_2(""+e);
			}

			@Override
			public void noFoundElement() {
				NotImplementedException.raise();
			}
		});
	}

	@Ignore
	@Test // TODO just a mess
	public void test2() {
		final Boilerplate boilerplate = new Boilerplate();
		boilerplate.get();
		final OS_Module mod = boilerplate.defaultMod();
		boilerplate.getGenerateFiles(mod);

		final Context ctx2 = mock(Context.class);
		final DeducePhase phase = boilerplate.pipelineLogic.dp;

		//
		//
		//

		ClassStatement cs = new ClassStatement(mod, mod.getContext());
		final IdentExpression capitalX = IdentExpression.forString("X");
		cs.setName(capitalX);
		FunctionDef fd = new FunctionDef(cs, cs.getContext());
		Context ctx1 = fd.getContext();
		fd.setName(IdentExpression.forString("main"));
		FunctionDef fd2 = new FunctionDef(cs, cs.getContext());
		fd2.setName(IdentExpression.forString("foo"));

//		EvaFunction generatedFunction = new EvaFunction(fd);
//		TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, new OS_UserType(cs));
//		generatedFunction.addVariableTableEntry("x", VariableTableType.VAR, tte, cs);

		//
		//
		//

		VariableSequence seq = new VariableSequence(ctx1);
		VariableStatement vs = seq.next();
		final IdentExpression x = IdentExpression.forString("x");
		vs.setName(x);
		ProcedureCallExpression pce2 = new ProcedureCallExpression();
		pce2.setLeft(capitalX);
		vs.initial(pce2);
		IBinaryExpression e = ExpressionBuilder.build(x, ExpressionKind.ASSIGNMENT, pce2);

		final IdentExpression foo = IdentExpression.forString("foo");
		ProcedureCallExpression pce = new ProcedureCallExpression();
		pce.setLeft(new DotExpression(x, foo));

		fd.scope(new Scope3(fd));
		fd.add(seq);
		fd.add(new StatementWrapper(pce2, ctx1, fd));
		fd2.scope(new Scope3(fd2));

		final GeneratePhase     generatePhase     = boilerplate.pipelineLogic.generatePhase;
		final GenerateFunctions generateFunctions = boilerplate.pipelineLogic.generatePhase.getGenerateFunctions(mod);

		fd2.add(new StatementWrapper(pce, ctx2, fd2));

		final ClassHeader ch = new ClassHeader(false, List_of());
		cs.setHeader(ch);

		ClassInvocation ci = phase.registerClassInvocation(cs);
		ProcTableEntry pte2 = null;
		FunctionInvocation fi = new FunctionInvocation(fd, pte2, ci, generatePhase);
//		when(fd.returnType()).thenReturn(null);
		final FormalArgList formalArgList = new FormalArgList();
//		when(fd.fal()).thenReturn(formalArgList);
//		when(fd.fal()).thenReturn(formalArgList);
//		when(fd2.returnType()).thenReturn(null);
		EvaFunction generatedFunction = generateFunctions.generateFunction(fd, cs, fi);

/*
		InstructionArgument es = generateFunctions.simplify_expression(e, generatedFunction, ctx2);

		InstructionArgument s = generateFunctions.simplify_expression(pce, generatedFunction, ctx2);
*/

		//
		//
		//

		LookupResultList lrl = new LookupResultList();
		lrl.add("foo", 1, fd2, ctx2);

		when(ctx2.lookup("foo")).thenReturn(lrl);

		LookupResultList lrl2 = new LookupResultList();
		lrl2.add("X", 1, cs, ctx1);

		when(ctx2.lookup("X")).thenReturn(lrl2);

		//
		//
		//


		ClassInvocation invocation2 = new ClassInvocation(cs, null);
		invocation2 = phase.registerClassInvocation(invocation2);
		ProcTableEntry pte3 = null;
		FunctionInvocation fi2                = new FunctionInvocation(fd2, pte3, invocation2, generatePhase);
		EvaFunction        generatedFunction2 = generateFunctions.generateFunction(fd2, fd2.getParent(), fi2);//new EvaFunction(fd2);
//		generatedFunction2.addVariableTableEntry("self", VariableTableType.SELF, null, null);
//		final TypeTableEntry type = null;
//		int res = generatedFunction2.addVariableTableEntry("Result", VariableTableType.RESULT, type, null);

		//
		//
		//

		IdentIA identIA = new IdentIA(0, generatedFunction);

		DeduceTypes2 d2 = new DeduceTypes2(mod, phase);

		generatedFunction.getVarTableEntry(0).setConstructable(generatedFunction.getProcTableEntry(0));
		identIA.getEntry().setCallablePTE(generatedFunction.getProcTableEntry(1));

		d2.resolveIdentIA2_(ctx2, identIA, generatedFunction, new FoundElement(phase) {
			@Override
			public void foundElement(OS_Element e) {
				assert e == fd2;
			}

			@Override
			public void noFoundElement() {
				assert false;
			}
		});
	}

}

//
//
//
