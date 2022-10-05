/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import org.junit.Assert;
import org.junit.Test;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.stages.gen_fn.GeneratePhase;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;

import static org.easymock.EasyMock.*;

public class TypeOfTypeNameTest {

	@Test
	public void typeOfSimpleQualident() throws ResolveError {
		//
		// CREATE MOCKS
		//
		Context ctx = mock(Context.class);
		OS_Module mod = mock(OS_Module.class);
		Compilation c = mock(Compilation.class);

		//
		// CREATE VARIABLES
		//
		ErrSink e = new StdErrSink();

		String typeNameString = "AbstractFactory";

		VariableStatement var_x = new VariableStatement(null);
		var_x.setName(Helpers.string_to_ident("x")); // not necessary
		RegularTypeName rtn = new RegularTypeName(ctx);
		rtn.setName(Helpers.string_to_qualident(typeNameString));
		var_x.setTypeName(rtn);

		LookupResultList lrl = new LookupResultList();
		lrl.add("x", 1, var_x, ctx);

		//
		// CREATE VARIABLE UNDER TEST
		//
		TypeOfTypeName t = new TypeOfTypeName(ctx);
		t.typeOf(Helpers.string_to_qualident(var_x.getName()));

		//
		// SET UP EXPECTATIONS
		//
		expect(mod.getFileName()).andReturn("foo.elijah");
		expect(c.getErrSink()).andReturn(e);
		expect(mod.getCompilation()).andReturn(c);
		expect(ctx.lookup(var_x.getName())).andReturn(lrl);
		replay(ctx, mod, c);

		//
		// VERIFY EXPECTATIONS
		//
		final ElLog.Verbosity verbosity1 = new Compilation(new StdErrSink(), new IO()).gitlabCIVerbosity();
		final PipelineLogic pl = new PipelineLogic(verbosity1);
		final GeneratePhase generatePhase = new GeneratePhase(verbosity1, pl);
		DeduceTypes2 deduceTypes2 = new DeduceTypes2(mod, new DeducePhase(generatePhase, pl, verbosity1));
		TypeName tn = t.resolve(ctx, deduceTypes2);
//		System.out.println(tn);
		verify(ctx, mod, c);
		Assert.assertEquals(typeNameString, tn.toString());
	}

	@Test
	public void typeOfComplexQualident() throws ResolveError {
		//
		// CREATE MOCKS
		//
		Context ctx = mock(Context.class);
		OS_Module mod = mock(OS_Module.class);
		Compilation c = mock(Compilation.class);

		//
		// CREATE VARIABLES
		//
		ErrSink e = new StdErrSink();

		String typeNameString = "package.AbstractFactory";

		VariableStatement var_x = new VariableStatement(null);
		var_x.setName(Helpers.string_to_ident("x")); // not necessary
		RegularTypeName rtn = new RegularTypeName(ctx);
		rtn.setName(Helpers.string_to_qualident(typeNameString));
		var_x.setTypeName(rtn);

		LookupResultList lrl = new LookupResultList();
		lrl.add("x", 1, var_x, ctx);

		//
		// CREATE VARIABLE UNDER TEST
		//
		TypeOfTypeName t = new TypeOfTypeName(ctx);
		t.typeOf(Helpers.string_to_qualident("x"));

		//
		// SET UP EXPECTATIONS
		//
		expect(mod.getFileName()).andReturn("foo.elijah");
		expect(mod.getCompilation()).andReturn(c);
		expect(c.getErrSink()).andReturn(e);
		expect(ctx.lookup("x")).andReturn(lrl);
		replay(ctx, mod, c);

		//
		// VERIFY EXPECTATIONS
		//
		final ElLog.Verbosity verbosity1 = new Compilation(new StdErrSink(), new IO()).gitlabCIVerbosity();
		final PipelineLogic pl = new PipelineLogic(verbosity1);
		final GeneratePhase generatePhase = new GeneratePhase(verbosity1, pl);
		DeduceTypes2 deduceTypes2 = new DeduceTypes2(mod, new DeducePhase(generatePhase, pl, verbosity1));
		TypeName tn = t.resolve(ctx, deduceTypes2);
//		System.out.println(tn);
		verify(ctx, mod, c);
		Assert.assertEquals(typeNameString, tn.toString());
	}

//	@Test
//	public void typeOfComplexQualident3() {
//		//
//		// CREATE MOCK
//		//
//		Context ctx = mock(Context.class);
//
//		//
//		// CREATE VARIABLES
//		//
//		String typeNameString1 = "package1.AbstractFactory";
//		final String typeNameString = "SystemInteger";
//
//		OS_Module mod = new OS_Module();
//		Context mod_ctx = mod.getContext();
//
//		ClassStatement st_af = new ClassStatement(mod, mod_ctx);
//		st_af.setName(IdentExpression.forString("AbstractFactory"));
//		final OS_Package package1 = new OS_Package(Helpers.string_to_qualident("package1"), 1);
//		st_af.setPackageName(package1);
//
//		VariableSequence vs = new VariableSequence(st_af.getContext());
//		VariableStatement var_y = new VariableStatement(vs);
//		var_y.setName(IdentExpression.forString("y"));
//		RegularTypeName rtn_y = new RegularTypeName(ctx);
//		rtn_y.setName(Helpers.string_to_qualident(typeNameString));
//		var_y.setTypeName(rtn_y);
//
//		st_af.add(vs);
//
//		VariableStatement var_x = new VariableStatement(null);
//		var_x.setName(Helpers.string_to_ident("x")); // not necessary
//		RegularTypeName rtn_x = new RegularTypeName(ctx);
//		rtn_x.setName(Helpers.string_to_qualident(typeNameString1));
//		var_x.setTypeName(rtn_x);
//
//		LookupResultList lrl = new LookupResultList();
//		lrl.add("x", 1, var_x, ctx);
//		LookupResultList lrl2 = new LookupResultList();
//		lrl2.add("package1", 1, null, ctx);
//
//		//
//		// CREATE VARIABLE UNDER TEST
//		//
//		TypeOfTypeName t = new TypeOfTypeName(ctx);
//		t.typeOf(Helpers.string_to_qualident("x.y"));
//
//		//
//		// SET UP EXPECTATIONS
//		//
//		expect(ctx.lookup("x")).andReturn(lrl);
//		expect(ctx.lookup("package1")).andReturn(lrl2);
//		replay(ctx);
//
//		//
//		// VERIFY EXPECTATIONS
//		//
//		TypeName tn = t.resolve(ctx);
////		System.out.println(tn);
//		verify(ctx);
//		Assert.assertEquals(typeNameString, tn.toString());
//	}

	@Test
	public void typeOfComplexQualident2() throws ResolveError {
		//
		// CREATE MOCK
		//
		Context ctx = mock(Context.class);
		Context ctx4 = mock(Context.class);

		//
		// CREATE VARIABLES
		//
		String typeNameString1 = "AbstractFactory";
		final String typeNameString = "SystemInteger";

		OS_Module mod = new OS_Module();
		mod.parent = mock(Compilation.class);
		Context mod_ctx = mod.getContext();

		ClassStatement st_af = new ClassStatement(mod, mod_ctx);
		st_af.setName(IdentExpression.forString("AbstractFactory"));
		ClassStatement sysint = new ClassStatement(mod, mod_ctx);
		sysint.setName(IdentExpression.forString("SystemInteger"));

		VariableSequence vs = new VariableSequence(st_af.getContext());
		VariableStatement var_y = vs.next();
		var_y.setName(IdentExpression.forString("y"));
		RegularTypeName rtn_y = new RegularTypeName(ctx);
		rtn_y.setName(Helpers.string_to_qualident(typeNameString));
		var_y.setTypeName(rtn_y);

		st_af.add(vs);

		VariableStatement var_x = new VariableStatement(null);
		var_x.setName(Helpers.string_to_ident("x")); // not necessary
		RegularTypeName rtn_x = new RegularTypeName(ctx);
		rtn_x.setName(Helpers.string_to_qualident(typeNameString1));
		var_x.setTypeName(rtn_x);

		LookupResultList lrl = new LookupResultList();
		lrl.add("x", 1, var_x, ctx);
		LookupResultList lrl2 = new LookupResultList();
		lrl2.add(typeNameString1, 1, st_af, ctx);
		LookupResultList lrl3 = new LookupResultList();
		lrl3.add("SystemInteger", 1, sysint, ctx);
		LookupResultList lrl4 = new LookupResultList();
		lrl4.add("y", 1, var_y, ctx4);

		//
		// CREATE VARIABLE UNDER TEST
		//
		TypeOfTypeName t = new TypeOfTypeName(ctx);
		t.typeOf(Helpers.string_to_qualident("x.y"));

		//
		// SET UP EXPECTATIONS
		//
//		OS_Module mod = mock(OS_Module.class);
		final ElLog.Verbosity verbosity1 = new Compilation(new StdErrSink(), new IO()).gitlabCIVerbosity();
		final PipelineLogic pl = new PipelineLogic(verbosity1);
		final GeneratePhase generatePhase = new GeneratePhase(verbosity1, pl);
		DeduceTypes2 deduceTypes2 = new DeduceTypes2(mod, new DeducePhase(generatePhase, pl, verbosity1));
//		expect(mod.getFileName()).andReturn("foo.elijah");
		expect(ctx.lookup("x")).andReturn(lrl);
//		expect(ctx.lookup("y")).andReturn(lrl4);
		expect(ctx.lookup(typeNameString1)).andReturn(lrl2);
//		expect(ctx.lookup("SystemInteger")).andReturn(lrl3);
		replay(ctx);

		//
		// VERIFY EXPECTATIONS
		//
		TypeName tn = t.resolve(ctx, deduceTypes2);
//		System.out.println(tn);
		verify(ctx);
		Assert.assertEquals(typeNameString, tn.toString());
	}

}

//
//
//
