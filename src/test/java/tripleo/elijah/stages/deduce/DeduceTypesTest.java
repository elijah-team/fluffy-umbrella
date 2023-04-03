/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

//import org.graalvm.compiler.nodes.NodeView;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.Operation2;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.contexts.ModuleContext;
import tripleo.elijah.lang.ClassHeader;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.NormalTypeName;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.lang.RegularTypeName;
import tripleo.elijah.lang.Scope3;
import tripleo.elijah.lang.VariableSequence;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.lang.VariableTypeName;
import tripleo.elijah.lang.types.OS_BuiltinType;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.lang2.BuiltInTypes;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.stages.deduce.post_bytecode.DeduceElement3_IdentTableEntry;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.test_help.Boilerplate;
import tripleo.elijah.util.Helpers;

import static org.easymock.EasyMock.mock;
import static tripleo.elijah.util.Helpers.List_of;

/**
 * Useless tests. We really want to know if a TypeName will resolve to the same types
 */
@Ignore
public class DeduceTypesTest {

	private GenType x;

	@Before
	public void setUp() {
		final Boilerplate boilerplate = new Boilerplate();
		boilerplate.get();
		boilerplate.getGenerateFiles(boilerplate.defaultMod());

		final OS_Module     mod  = boilerplate.defaultMod();
		final ModuleContext mctx = new ModuleContext(mod);
		mod.setContext(mctx);

		final ClassStatement cs = new ClassStatement(mod, mod.getContext());
		final ClassHeader    ch = new ClassHeader(false, List_of());
		ch.setName(Helpers.string_to_ident("Test"));
		cs.setHeader(ch);
		final FunctionDef fd = cs.funcDef();
		fd.setName(Helpers.string_to_ident("test"));
		final Scope3 scope3 = new Scope3(fd);
		final VariableSequence vss = scope3.statementClosure().varSeq(fd.getContext());
		final VariableStatement vs = vss.next();
		final IdentExpression x_ident = Helpers.string_to_ident("x");
		x_ident.setContext(fd.getContext());
		vs.setName(x_ident);
		final Qualident qu = new Qualident();
		qu.append(Helpers.string_to_ident("Integer"));
		((NormalTypeName)vs.typeName()).setName(qu);
		((NormalTypeName)vs.typeName()).setContext(fd.getContext());
		fd.scope(scope3);
		fd.postConstruct();
		cs.postConstruct();
		mod.postConstruct();
		final FunctionContext fc = (FunctionContext) fd.getContext(); // TODO needs to be mocked
		final IdentExpression x1 = Helpers.string_to_ident("x");
		x1.setContext(fc);

		mod.prelude = mod.getCompilation().findPrelude("c").success();

		//final PipelineLogic pl           = boilerplate.pipelineLogic;


		final ElLog.Verbosity verbosity     = Compilation.gitlabCIVerbosity();
		final DeducePhase     dp            = boilerplate.pr.pipelineLogic.dp;
		final DeduceTypes2    d             = dp.deduceModule(mod, dp.generatedClasses, verbosity);

		//final @NotNull GenerateFunctions gf = boilerplate.pr.pipelineLogic.generatePhase.getGenerateFunctions(mod);

		final BaseEvaFunction bgf = mock(BaseEvaFunction.class);

		final IdentTableEntry                ite     = new IdentTableEntry(0, x1, x1.getContext());
		final DeduceElementIdent             dei     = new DeduceElementIdent(ite);
		final DeduceElement3_IdentTableEntry de3_ite = (DeduceElement3_IdentTableEntry) ite.getDeduceElement3(d, bgf);


		final Operation2<OS_Module> fpl = boilerplate.comp.findPrelude("c");
		assert fpl.mode() == Mode.SUCCESS;
		mod.prelude = fpl.success();

		final DeduceElement3_IdentTableEntry xxx = DeduceLookupUtils.deduceExpression2(de3_ite, fc);
		this.x = xxx.genType();
		System.out.println(this.x);
	}

	/** TODO This test fails beacause we are comparing a BUILT_IN vs a USER OS_Type.
	 *   It fails because Integer is an interface and not a BUILT_IN
	 */
	@Test(expected = ResolveError.class)
	public void testDeduceIdentExpression1() {
		final BuiltInTypes bi_integer = new OS_BuiltinType(BuiltInTypes.SystemInteger).getBType();
		final BuiltInTypes inferred_t = x.resolved.getBType();

		Assert.assertEquals(bi_integer, inferred_t);
	}

	/**
	 * Now comparing {@link RegularTypeName} to {@link VariableTypeName} works
	 */
	@Test
	public void testDeduceIdentExpression2() {
		final RegularTypeName tn = new RegularTypeName();
		final Qualident tnq = new Qualident();
		tnq.append(Helpers.string_to_ident("Integer"));
		tn.setName(tnq);
		Assert.assertTrue(genTypeTypenameEquals(new OS_UserType(tn), x/*.getTypeName()*/));
	}
	@Test
	public void testDeduceIdentExpression3() {
		final VariableTypeName tn = new VariableTypeName();
		final Qualident tnq = new Qualident();
		tnq.append(Helpers.string_to_ident("Integer"));
		tn.setName(tnq);
		Assert.assertEquals(new OS_UserType(tn).getTypeName(), x.typeName.getTypeName());
		Assert.assertTrue(genTypeTypenameEquals(new OS_UserType(tn), x));
	}
	@Test
	public void testDeduceIdentExpression4() {
		final VariableTypeName tn = new VariableTypeName();
		final Qualident tnq = new Qualident();
		tnq.append(Helpers.string_to_ident("Integer"));
		tn.setName(tnq);
		Assert.assertEquals(new OS_UserType(tn).getTypeName(), x.typeName.getTypeName());
		Assert.assertTrue(genTypeTypenameEquals(new OS_UserType(tn), x));
		Assert.assertEquals(new OS_UserType(tn).toString(), x.typeName.toString());
	}

	private boolean genTypeTypenameEquals(OS_Type aType, GenType genType) {
		return genType.typeName.equals(aType);
	}

}

//
//
//
