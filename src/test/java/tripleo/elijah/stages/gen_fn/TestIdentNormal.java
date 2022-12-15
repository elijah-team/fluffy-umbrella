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
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FoundElement;
import tripleo.elijah.stages.deduce.FunctionInvocation;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.logging.ElLog;

import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * Created 3/4/21 3:53 AM
 */
public class TestIdentNormal {

//	@Test(expected = IllegalStateException.class) // TODO proves nothing
	public void test() {
		@NotNull Compilation comp = new Compilation(new StdErrSink(), new IO());
		@NotNull OS_Module mod = new OS_Module();//mock(OS_Module.class);
		mod.setParent(comp);
		FunctionDef fd = mock(FunctionDef.class);
		Context ctx1 = mock(Context.class);
		Context ctx2 = mock(Context.class);

		final ElLog.@NotNull Verbosity verbosity1 = new Compilation(new StdErrSink(), new IO()).gitlabCIVerbosity();
		final @NotNull PipelineLogic pl = new PipelineLogic(verbosity1);
		final @NotNull GeneratePhase generatePhase = new GeneratePhase(verbosity1, pl);
//		GenerateFunctions generateFunctions = new GenerateFunctions(generatePhase, mod, pl);
		@NotNull GenerateFunctions generateFunctions = generatePhase.getGenerateFunctions(mod);
		@NotNull GeneratedFunction generatedFunction = new GeneratedFunction(fd);
		@NotNull VariableSequence seq = new VariableSequence(ctx1);
		@NotNull VariableStatement vs = new VariableStatement(seq);
		final @NotNull IdentExpression x = IdentExpression.forString("x");
		vs.setName(x);
		final @NotNull IdentExpression foo = IdentExpression.forString("foo");
		@NotNull ProcedureCallExpression pce = new ProcedureCallExpression();
		pce.setLeft(new DotExpression(x, foo));

		@NotNull InstructionArgument s = generateFunctions.simplify_expression(pce, generatedFunction, ctx2);
		@NotNull List<InstructionArgument> l = generatedFunction._getIdentIAPathList(s);
		System.out.println(l);
//      System.out.println(generatedFunction.getIdentIAPathNormal());

		//
		//
		//

		@NotNull LookupResultList lrl = new LookupResultList();
		lrl.add("x", 1, vs, ctx2);
		expect(ctx2.lookup("x")).andReturn(lrl);

		replay(ctx2);

		//
		//
		//

		@NotNull IdentIA identIA = new IdentIA(1, generatedFunction);

		@NotNull DeducePhase phase = new DeducePhase(generatePhase, pl, verbosity1);
		@NotNull DeduceTypes2 d2 = new DeduceTypes2(mod, phase);

		final @NotNull List<InstructionArgument> ss = generatedFunction._getIdentIAPathList(identIA);
		d2.resolveIdentIA2_(ctx2, null, ss/*identIA*/, generatedFunction, new FoundElement(phase) {
			@Override
			public void foundElement(OS_Element e) {
				System.out.println(e);
			}

			@Override
			public void noFoundElement() {
				int y=2;
			}
		});
	}

//	@Test // TODO just a mess
	public void test2() {
		@NotNull Compilation comp = new Compilation(new StdErrSink(), new IO());
		@NotNull OS_Module mod = new OS_Module();
		mod.setParent(comp);
//		FunctionDef fd = mock(FunctionDef.class);
		Context ctx2 = mock(Context.class);

		final ElLog.@NotNull Verbosity verbosity1 = new Compilation(new StdErrSink(), new IO()).gitlabCIVerbosity();
		final @NotNull PipelineLogic pl = new PipelineLogic(verbosity1);
		final @NotNull GeneratePhase generatePhase = new GeneratePhase(verbosity1, pl);
		@NotNull DeducePhase phase = new DeducePhase(generatePhase, pl, verbosity1);

		@NotNull GenerateFunctions generateFunctions = generatePhase.getGenerateFunctions(mod);

		//
		//
		//

		@NotNull ClassStatement cs = new ClassStatement(mod, mod.getContext());
		final @NotNull IdentExpression capitalX = IdentExpression.forString("X");
		cs.setName(capitalX);
		@NotNull FunctionDef fd = new FunctionDef(cs, cs.getContext());
		Context ctx1 = fd.getContext();
		fd.setName(IdentExpression.forString("main"));
		@NotNull FunctionDef fd2 = new FunctionDef(cs, cs.getContext());
		fd2.setName(IdentExpression.forString("foo"));

//		GeneratedFunction generatedFunction = new GeneratedFunction(fd);
//		TypeTableEntry tte = generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, new OS_Type(cs));
//		generatedFunction.addVariableTableEntry("x", VariableTableType.VAR, tte, cs);

		//
		//
		//

		@NotNull VariableSequence seq = new VariableSequence(ctx1);
		@NotNull VariableStatement vs = seq.next();
		final @NotNull IdentExpression x = IdentExpression.forString("x");
		vs.setName(x);
		@NotNull ProcedureCallExpression pce2 = new ProcedureCallExpression();
		pce2.setLeft(capitalX);
		vs.initial(pce2);
		@NotNull IBinaryExpression e = ExpressionBuilder.build(x, ExpressionKind.ASSIGNMENT, pce2);

		final @NotNull IdentExpression foo = IdentExpression.forString("foo");
		@NotNull ProcedureCallExpression pce = new ProcedureCallExpression();
		pce.setLeft(new DotExpression(x, foo));

		fd.scope(new Scope3(fd));
		fd.add(seq);
		fd.add(new StatementWrapper(pce2, ctx1, fd));
		fd2.scope(new Scope3(fd2));
		fd2.add(new StatementWrapper(pce, ctx2, fd2));

		@Nullable ClassInvocation ci = new ClassInvocation(cs, null);
		ci = phase.registerClassInvocation(ci);
		@Nullable ProcTableEntry pte2 = null;
		@NotNull FunctionInvocation fi = new FunctionInvocation(fd, pte2, ci, generatePhase);
//		expect(fd.returnType()).andReturn(null);
		final @NotNull FormalArgList formalArgList = new FormalArgList();
//		expect(fd.fal()).andReturn(formalArgList);
//		expect(fd.fal()).andReturn(formalArgList);
//		expect(fd2.returnType()).andReturn(null);
		@NotNull GeneratedFunction generatedFunction = generateFunctions.generateFunction(fd, cs, fi);

/*
		InstructionArgument es = generateFunctions.simplify_expression(e, generatedFunction, ctx2);

		InstructionArgument s = generateFunctions.simplify_expression(pce, generatedFunction, ctx2);
*/

		//
		//
		//

		@NotNull LookupResultList lrl = new LookupResultList();
		lrl.add("foo", 1, fd2, ctx2);

		expect(ctx2.lookup("foo")).andReturn(lrl);

		@NotNull LookupResultList lrl2 = new LookupResultList();
		lrl2.add("X", 1, cs, ctx1);

		expect(ctx2.lookup("X")).andReturn(lrl2);

		//
		//
		//


		@Nullable ClassInvocation invocation2 = new ClassInvocation(cs, null);
		invocation2 = phase.registerClassInvocation(invocation2);
		@Nullable ProcTableEntry pte3 = null;
		@NotNull FunctionInvocation fi2 = new FunctionInvocation(fd2, pte3, invocation2, generatePhase);
		@NotNull GeneratedFunction generatedFunction2 = generateFunctions.generateFunction(fd2, fd2.getParent(), fi2);//new GeneratedFunction(fd2);
//		generatedFunction2.addVariableTableEntry("self", VariableTableType.SELF, null, null);
//		final TypeTableEntry type = null;
//		int res = generatedFunction2.addVariableTableEntry("Result", VariableTableType.RESULT, type, null);

		//
		//
		//

		replay(ctx2);

		//
		//
		//

		@NotNull IdentIA identIA = new IdentIA(0, generatedFunction);

		@NotNull DeduceTypes2 d2 = new DeduceTypes2(mod, phase);

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

		verify(ctx2);
	}

}

//
//
//
