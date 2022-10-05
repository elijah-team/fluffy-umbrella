/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import tripleo.elijah.comp.Compilation;
//import tripleo.elijah.comp.IO;
//import tripleo.elijah.comp.StdErrSink;
//import tripleo.elijah.contexts.FunctionContext;
//import tripleo.elijah.contexts.ModuleContext;
//import tripleo.elijah.lang.*;
//import tripleo.elijah.util.Helpers;
//

/**
 * deduceExpression now returns USER_CLASSes so this is basically void
 */
public class DeduceTypesTest3 {
//
//	private OS_Type x;
//
//	@Before
//	public void setUp() {
//		final OS_Module mod = new OS_Module();
//		mod.parent = new Compilation(new StdErrSink(), new IO());
////		final DeduceTypes d = new DeduceTypes(mod);
//		final ModuleContext mctx = new ModuleContext(mod);
//		mod.setContext(mctx);
//		final ClassStatement cs = new ClassStatement(mod, mctx);
//		cs.setName(Helpers.string_to_ident("Test"));
//		final ClassStatement cs_foo = new ClassStatement(mod, mctx);
//		cs_foo.setName(Helpers.string_to_ident("Foo"));
//		final FunctionDef fd = cs.funcDef();
//		fd.setName(Helpers.string_to_ident("test"));
//		Scope3 scope3 = new Scope3(fd);
//		final VariableSequence vss = scope3.statementClosure().varSeq(fd.getContext());
//		final VariableStatement vs = vss.next();
//		vs.setName(Helpers.string_to_ident("x"));
//		final Qualident qu = new Qualident();
//		qu.append(Helpers.string_to_ident("Foo"));
//		((NormalTypeName)vs.typeName()).setName(qu);
//		((NormalTypeName)vs.typeName()).setContext(fd.getContext());
//		fd.scope(scope3);
//		fd.postConstruct();
//		cs_foo.postConstruct();
//		cs.postConstruct();
//		mod.postConstruct();
//		final FunctionContext fc = (FunctionContext) fd.getContext(); // TODO needs to be mocked
//		final IdentExpression x1 = Helpers.string_to_ident("x");
//		x1.setContext(fc);
//		//
//		//
//		//
//		DeducePhase dp = new DeducePhase();
//		DeduceTypes2 d = dp.deduceModule(mod);
////		final DeduceTypes d = new DeduceTypes(mod);
//		this.x = DeduceLookupUtils.deduceExpression(x1, fc);
//		System.out.println(this.x);
//	}
//
////	/**
////	 * Don't test BType here because its not a BuiltInType
////	 */
////	@Test
////	public void testDeduceIdentExpression1() {
////		Assert.assertEquals(new OS_Type(BuiltInTypes.SystemInteger).getBType(), x.getBType());
////	}
//	/** TODO This test fails because we are comparing RegularTypeName and VariableTypeName */
//	@Test
//	public void testDeduceIdentExpression2() {
//		final RegularTypeName tn = new RegularTypeName();
//		Qualident tnq = new Qualident();
//		tnq.append(Helpers.string_to_ident("Foo"));
//		tn.setName(tnq);
//		Assert.assertEquals(new OS_Type(tn), x/*.getTypeName()*/);
//	}
//	@Test
//	public void testDeduceIdentExpression3() {
//		final VariableTypeName tn = new VariableTypeName();
//		final Qualident tnq = new Qualident();
//		tnq.append(tripleo.elijah.util.Helpers.string_to_ident("Foo"));
//		tn.setName(tnq);
//		Assert.assertEquals(new OS_Type(tn), x);
//	}
//	@Test
//	public void testDeduceIdentExpression3_5() {
//		final VariableTypeName tn = new VariableTypeName();
//		final Qualident tnq = new Qualident();
//		tnq.append(tripleo.elijah.util.Helpers.string_to_ident("Foo"));
//		tn.setName(tnq);
//		Assert.assertEquals(new OS_Type(tn).getTypeName(), x.getTypeName());
//	}
//	@Test
//	public void testDeduceIdentExpression4() {
//		final VariableTypeName tn = new VariableTypeName();
//		final Qualident tnq = new Qualident();
//		tnq.append(tripleo.elijah.util.Helpers.string_to_ident("Foo"));
//		tn.setName(tnq);
//		Assert.assertEquals(new OS_Type(tn).toString(), x.toString());
//	}
//
}
