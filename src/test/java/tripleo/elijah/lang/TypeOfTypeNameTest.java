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
import org.junit.Ignore;
import org.junit.Test;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.test_help.Boilerplate;
import tripleo.elijah.util.Helpers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tripleo.elijah.util.Helpers.List_of;

public class TypeOfTypeNameTest {

	@Ignore
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
		when(mod.getFileName()).thenReturn("foo.elijah");
		when(c.getErrSink()).thenReturn(e);
		when(mod.getCompilation()).thenReturn(c);
		when(ctx.lookup(var_x.getName())).thenReturn(lrl);

		//
		// VERIFY EXPECTATIONS
		//

		Boilerplate boilerplate = new Boilerplate();
		boilerplate.get();
		boilerplate.getGenerateFiles(boilerplate.defaultMod());

		final PipelineLogic   pl            = boilerplate.pipelineLogic;
		final DeduceTypes2    deduceTypes2  = new DeduceTypes2(mod, pl.dp);
		final TypeName        tn            = t.resolve(ctx, deduceTypes2);
//		tripleo.elijah.util.Stupidity.println_out_2(tn);
		Assert.assertEquals(typeNameString, tn.toString());
	}

	@Ignore
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
		when(mod.getFileName()).thenReturn("foo.elijah");
		when(mod.getCompilation()).thenReturn(c);
		when(c.getErrSink()).thenReturn(e);
		when(ctx.lookup("x")).thenReturn(lrl);

		//
		// VERIFY EXPECTATIONS
		//
		Boilerplate boilerplate = new Boilerplate();
		boilerplate.get();
		boilerplate.getGenerateFiles(boilerplate.defaultMod());

		final PipelineLogic   pl            = boilerplate.pipelineLogic;
		final DeduceTypes2    deduceTypes2  = new DeduceTypes2(mod, pl.dp);
		TypeName tn = t.resolve(ctx, deduceTypes2);
//		tripleo.elijah.util.Stupidity.println_out_2(tn);
		Assert.assertEquals(typeNameString, tn.toString());
	}

	@Ignore
	@Test
	public void typeOfComplexQualident3() throws ResolveError {
		//
		// CREATE MOCK
		//
		Context ctx = mock(Context.class);

		//
		// CREATE VARIABLES
		//
		String typeNameString1 = "package1.AbstractFactory";
		final String typeNameString = "SystemInteger";

		OS_Module mod = new OS_Module();
		Context mod_ctx = mod.getContext();

		ClassStatement st_af = new ClassStatement(mod, mod_ctx);
		st_af.setName(IdentExpression.forString("AbstractFactory"));
		final OS_Package package1 = new OS_Package(Helpers.string_to_qualident("package1"), 1);
		st_af.setPackageName(package1);

		VariableSequence vs = new VariableSequence(st_af.getContext());
		VariableStatement var_y = new VariableStatement(vs);
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
		lrl2.add("package1", 1, null, ctx);

		//
		// CREATE VARIABLE UNDER TEST
		//
		TypeOfTypeName t = new TypeOfTypeName(ctx);
		t.typeOf(Helpers.string_to_qualident("x.y"));

		//
		// SET UP EXPECTATIONS
		//
		when(ctx.lookup("x")).thenReturn(lrl);
		when(ctx.lookup("package1")).thenReturn(lrl2);

		//
		// VERIFY EXPECTATIONS
		//
		Boilerplate boilerplate = new Boilerplate();
		boilerplate.get();
		boilerplate.getGenerateFiles(boilerplate.defaultMod());

		final PipelineLogic   pl            = boilerplate.pipelineLogic;
		final DeduceTypes2    deduceTypes2  = new DeduceTypes2(mod, pl.dp);

		TypeName tn = t.resolve(ctx, deduceTypes2);
//		tripleo.elijah.util.Stupidity.println_out_2(tn);
		Assert.assertEquals(typeNameString, tn.toString());
	}

	@Ignore
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
		mod.setParent(mock(Compilation.class));
		Context mod_ctx = mod.getContext();

		ClassStatement st_af = new ClassStatement(mod, mod_ctx);
		ClassHeader ch = new ClassHeader(false, List_of());
		ch.setName(IdentExpression.forString("AbstractFactory"));
		st_af.setHeader(ch);
		ClassStatement sysint = new ClassStatement(mod, mod_ctx);
		ClassHeader ch2 = new ClassHeader(false, List_of());
		ch2.setName(IdentExpression.forString("SystemInteger"));
		sysint.setHeader(ch2);

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
		Boilerplate boilerplate = new Boilerplate();
		boilerplate.get();
		boilerplate.getGenerateFiles(boilerplate.defaultMod());

		final PipelineLogic   pl            = boilerplate.pipelineLogic;
		final DeduceTypes2    deduceTypes2  = new DeduceTypes2(mod, pl.dp);

//		when(mod.getFileName()).thenReturn("foo.elijah");
		when(ctx.lookup("x")).thenReturn(lrl);
//		when(ctx.lookup("y")).thenReturn(lrl4);
		when(ctx.lookup(typeNameString1)).thenReturn(lrl2);
//		when(ctx.lookup("SystemInteger")).thenReturn(lrl3);

		//
		// VERIFY EXPECTATIONS
		//
		TypeName tn = t.resolve(ctx, deduceTypes2);
//		tripleo.elijah.util.Stupidity.println_out_2(tn);
		Assert.assertEquals(typeNameString, tn.toString());
	}

}

//
//
//
