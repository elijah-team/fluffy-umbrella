/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.contexts.ModuleContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratePhase;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;

/**
 * Useless tests. We really want to know if a TypeName will resolve to the same types
 */
public class DeduceTypesTest {

	private @Nullable GenType x;

	@Before
	public void setUp() throws ResolveError {
		final @NotNull OS_Module mod = new OS_Module();
		mod.parent = new Compilation(new StdErrSink(), new IO());
		final @NotNull ModuleContext mctx = new ModuleContext(mod);
		mod.setContext(mctx);
		final @NotNull ClassStatement cs = new ClassStatement(mod, mctx);
		cs.setName(Helpers.string_to_ident("Test"));
		final @NotNull FunctionDef fd = cs.funcDef();
		fd.setName(Helpers.string_to_ident("test"));
		@NotNull Scope3 scope3 = new Scope3(fd);
		final VariableSequence vss = scope3.statementClosure().varSeq(fd.getContext());
		final @NotNull VariableStatement vs = vss.next();
		final @NotNull IdentExpression x = Helpers.string_to_ident("x");
		x.setContext(fd.getContext());
		vs.setName(x);
		final @NotNull Qualident qu = new Qualident();
		qu.append(Helpers.string_to_ident("Integer"));
		((NormalTypeName)vs.typeName()).setName(qu);
		((NormalTypeName)vs.typeName()).setContext(fd.getContext());
		fd.scope(scope3);
		fd.postConstruct();
		cs.postConstruct();
		mod.postConstruct();
		final FunctionContext fc = (FunctionContext) fd.getContext(); // TODO needs to be mocked
		final @NotNull IdentExpression x1 = Helpers.string_to_ident("x");
		x1.setContext(fc);
		//
		mod.prelude = mod.parent.findPrelude("c");
		//
		//
		//
		final ElLog.@NotNull Verbosity verbosity = mod.parent.gitlabCIVerbosity();
		final @NotNull PipelineLogic pl = new PipelineLogic(verbosity);
		final @NotNull GeneratePhase generatePhase = new GeneratePhase(verbosity, pl);
		@NotNull DeducePhase dp = new DeducePhase(generatePhase, pl, verbosity);
		@NotNull DeduceTypes2 d = dp.deduceModule(mod, verbosity);
//		final DeduceTypes d = new DeduceTypes(mod);
		this.x = DeduceLookupUtils.deduceExpression(d, x1, fc);
		System.out.println(this.x);
	}
	/** TODO This test fails beacause we are comparing a BUILT_IN vs a USER OS_Type.
	 *   It fails because Integer is an interface and not a BUILT_IN
	 */
//	@Test
//	public void testDeduceIdentExpression1() {
//		Assert.assertEquals(new OS_Type(BuiltInTypes.SystemInteger).getBType(), x.getBType());
//	}
	/**
	 * Now comparing {@link RegularTypeName} to {@link VariableTypeName} works
	 */
	@Test
	public void testDeduceIdentExpression2() {
		final @NotNull RegularTypeName tn = new RegularTypeName();
		final @NotNull Qualident tnq = new Qualident();
		tnq.append(Helpers.string_to_ident("Integer"));
		tn.setName(tnq);
		Assert.assertTrue(genTypeTypenameEquals(new OS_Type(tn), x/*.getTypeName()*/));
	}
	@Test
	public void testDeduceIdentExpression3() {
		final @NotNull VariableTypeName tn = new VariableTypeName();
		final @NotNull Qualident tnq = new Qualident();
		tnq.append(Helpers.string_to_ident("Integer"));
		tn.setName(tnq);
		Assert.assertEquals(new OS_Type(tn).getTypeName(), x.typeName.getTypeName());
		Assert.assertTrue(genTypeTypenameEquals(new OS_Type(tn), x));
	}
	@Test
	public void testDeduceIdentExpression4() {
		final @NotNull VariableTypeName tn = new VariableTypeName();
		final @NotNull Qualident tnq = new Qualident();
		tnq.append(Helpers.string_to_ident("Integer"));
		tn.setName(tnq);
		Assert.assertEquals(new OS_Type(tn).getTypeName(), x.typeName.getTypeName());
		Assert.assertTrue(genTypeTypenameEquals(new OS_Type(tn), x));
		Assert.assertEquals(new OS_Type(tn).toString(), x.typeName.toString());
	}

	private boolean genTypeTypenameEquals(OS_Type aType, @NotNull GenType genType) {
		return genType.typeName.equals(aType);
	}

}

//
//
//
