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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_c.CReference;
import tripleo.elijah.stages.gen_c.Emit;
import tripleo.elijah.stages.gen_c.Generate_Code_For_Method;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.InstructionArgument;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;

import static org.easymock.EasyMock.*;

public class GetIdentIAPathTest_ForC {

	GeneratedFunction gf;
	OS_Module mod;

	@Before
	public void setUp() throws Exception {
		mod = mock(OS_Module.class);
		final FunctionDef fd = mock(FunctionDef.class);
		gf = new GeneratedFunction(fd);

		Emit.emitting = false;
	}

	@Test
	public void testManualXDotFoo() {
		@NotNull final IdentExpression x_ident = IdentExpression.forString("X");
		@NotNull final IdentExpression foo_ident = IdentExpression.forString("foo");
		//
		final VariableSequence vsq = new VariableSequence(null);
		vsq.setParent(mock(ClassStatement.class));
		final VariableStatement foo_vs = new VariableStatement(vsq);
		foo_vs.setName(foo_ident);
		//
		final OS_Type type = null;
		final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, type, x_ident);
		final int int_index = gf.addVariableTableEntry("x", VariableTableType.VAR, tte, mock(VariableStatement.class));
		final int ite_index = gf.addIdentTableEntry(foo_ident, null);
		final IdentTableEntry ite = gf.getIdentTableEntry(ite_index);
		ite.setResolvedElement(foo_vs);
		ite.backlink = new IntegerIA(int_index, gf);
		final IdentIA ident_ia = new IdentIA(ite_index, gf);
		final String x = getIdentIAPath(ident_ia, gf);
		Assert.assertEquals("vvx->vmfoo", x);
	}

	@Test
	public void testManualXDotFoo2() {
		@NotNull final IdentExpression x_ident = IdentExpression.forString("x");
		@NotNull final IdentExpression foo_ident = IdentExpression.forString("foo");
		//
		final OS_Element mock_class = mock(ClassStatement.class);
		expect(gf.getFD().getParent()).andReturn(mock_class);
		expect(gf.getFD().getParent()).andReturn(mock_class);
		replay(gf.getFD());

		final VariableSequence vsq = new VariableSequence(null);
		vsq.setParent(mock(ClassStatement.class));
		final VariableStatement foo_vs = new VariableStatement(vsq);
		foo_vs.setName(foo_ident);
		final VariableSequence vsq2 = new VariableSequence(null);
		vsq.setParent(mock(ClassStatement.class));
		final VariableStatement x_vs = new VariableStatement(vsq2);
		x_vs.setName(x_ident);

/*
		expect(mod.pullPackageName()).andReturn(OS_Package.default_package);
		mod.add(anyObject(ClassStatement.class));
		replay(mod);
		ClassStatement el1 = new ClassStatement(mod, null);
*/

		//		el1.add(vsq);
		//
		final ElLog.Verbosity verbosity1 = new CompilationImpl(new StdErrSink(), new IO()).gitlabCIVerbosity();
		final AccessBus ab = new AccessBus(mock(Compilation.class));
		final PipelineLogic pl = new PipelineLogic(ab);
		final GeneratePhase generatePhase = new GeneratePhase(verbosity1, pl);
		final GenerateFunctions gen = generatePhase.getGenerateFunctions(mod);
		final Context ctx = mock(Context.class);
		//
		final DotExpression expr = new DotExpression(x_ident, foo_ident);
		final InstructionArgument xx = gen.simplify_expression(expr, gf, ctx);
		//
		@NotNull final IdentTableEntry x_ite = gf.getIdentTableEntry(0); // x
		x_ite.setResolvedElement(x_vs);
		@NotNull final IdentTableEntry foo_ite = gf.getIdentTableEntry(1); // foo
		foo_ite.setResolvedElement(foo_vs);
		//
		final IdentIA ident_ia = (IdentIA) xx;
		final String x = getIdentIAPath(ident_ia, gf);
//		Assert.assertEquals("vvx->vmfoo", x);  // TODO real expectation, IOW output below is wrong
		// TODO actually compiler should comlain that it can't find x
		Assert.assertEquals("->vmx->vmfoo", x);
	}

	@Test
	public void testManualXDotFoo3() {
		final IdentExpression x_ident = Helpers.string_to_ident("x");
		@NotNull final IdentExpression foo_ident = Helpers.string_to_ident("foo");
		//
		final ElLog.Verbosity verbosity1 = new CompilationImpl(new StdErrSink(), new IO()).gitlabCIVerbosity();
		final AccessBus ab = new AccessBus(mock(Compilation.class));
		final PipelineLogic pl = new PipelineLogic(ab);
		final GeneratePhase generatePhase = new GeneratePhase(verbosity1, pl);
		final GenerateFunctions gen = generatePhase.getGenerateFunctions(mod);
		final Context ctx = mock(Context.class);
		//
		final OS_Type type = null;
		final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, type, x_ident);
		final int int_index = gf.addVariableTableEntry("x", VariableTableType.VAR, tte, mock(VariableStatement.class));
		//
		final DotExpression expr = new DotExpression(x_ident, foo_ident);
		final InstructionArgument xx = gen.simplify_expression(expr, gf, ctx);
		//
/*
		int ite_index = gf.addIdentTableEntry(foo_ident);
		IdentTableEntry ite = gf.getIdentTableEntry(ite_index);
		ite.backlink = new IntegerIA(int_index);
*/
		final VariableSequence vsq = new VariableSequence(null);
		vsq.setParent(mock(ClassStatement.class));
		final VariableStatement foo_vs = new VariableStatement(vsq);
		foo_vs.setName(foo_ident);

		final IdentIA ident_ia = (IdentIA) xx;
		@NotNull final IdentTableEntry ite = ((IdentIA) xx).getEntry();
		ite.setResolvedElement(foo_vs);

		final String x = getIdentIAPath(ident_ia, gf);
//		Assert.assertEquals("vvx->vmfoo", x); // TODO real expectation
		Assert.assertEquals("vvx->vmfoo", x);
	}

	@Test
	public void testManualXDotFooWithFooBeingFunction() {
		@NotNull final IdentExpression x_ident = Helpers.string_to_ident("x");
		@NotNull final IdentExpression foo_ident = Helpers.string_to_ident("foo");
		//
		final Context ctx = mock(Context.class);
		final Context mockContext = mock(Context.class);

		final LookupResultList lrl = new LookupResultList();
		final LookupResultList lrl2 = new LookupResultList();

		expect(mod.pullPackageName()).andReturn(OS_Package.default_package);
		expect(mod.getFileName()).andReturn("filename.elijah");
//		expect(mod.add(classStatement)); // really want this but cant mock void functions
		mod.add(anyObject(ClassStatement.class));
		replay(mod);

		final ClassStatement classStatement = new ClassStatement(mod, ctx);
		classStatement.setName(Helpers.string_to_ident("X")); // README not explicitly necessary

//		expect(mockContext.lookup(foo_ident.getText())).andReturn(lrl2);

//		expect(classStatement.getContext().lookup(foo_ident.getText())).andReturn(lrl2);

		lrl.add(x_ident.getText(), 1, classStatement, ctx);
		expect(ctx.lookup(x_ident.getText())).andReturn(lrl);

		final FunctionDef functionDef = new FunctionDef(classStatement, classStatement.getContext());
		functionDef.setName(foo_ident);
		lrl2.add(foo_ident.getText(), 1, functionDef, mockContext);

		//
		// SET UP EXPECTATIONS
		//
		replay(ctx, mockContext);

		final LookupResultList lrl_expected = ctx.lookup(x_ident.getText());

		//
		// VERIFY EXPECTATIONS
		//

		//
		final OS_Type type = new OS_Type(classStatement);
		final TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, type, x_ident);
		final int int_index = gf.addVariableTableEntry("x", VariableTableType.VAR, tte, mock(VariableStatement.class));
		//
		final DotExpression expr = new DotExpression(x_ident, foo_ident);
		//
		final ElLog.Verbosity verbosity1 = new CompilationImpl(new StdErrSink(), new IO()).gitlabCIVerbosity();
		final AccessBus ab = new AccessBus(mock(Compilation.class));
		final PipelineLogic pl = new PipelineLogic(ab);
		final GeneratePhase generatePhase = new GeneratePhase(verbosity1, pl);
		final GenerateFunctions gen = generatePhase.getGenerateFunctions(mod);
		final InstructionArgument xx = gen.simplify_expression(expr, gf, ctx);

		//
		// This is the Deduce portion.
		// Not very extensive is it?
		//
		final IdentIA ident_ia = (IdentIA) xx;
		final IdentTableEntry ite = ident_ia.getEntry();
		ite.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(functionDef));

		// This assumes we want a function call
		// but what if we want a function pointer or a curry or function reference?
		// IOW, a ProcedureCall is not specified
		final String x = getIdentIAPath(ident_ia, gf);

		verify(mod, ctx, mockContext);

		Assert.assertEquals("Z-1foo(vvx)", x);
	}

	String getIdentIAPath(final IdentIA ia2, final GeneratedFunction generatedFunction) {
		final CReference reference = new CReference();
		reference.getIdentIAPath(ia2, generatedFunction, Generate_Code_For_Method.AOG.GET, null); // TODO is null correct?
		return reference.build();
	}


}

//
//
//
