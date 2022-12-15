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
import org.junit.Test;
import tripleo.elijah.comp.*;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.contexts.ModuleContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratePhase;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;

public class DeduceTypesTest2 {

	@Test
	public void testDeduceIdentExpression() throws ResolveError {
		final @NotNull OS_Module mod = new OS_Module();
		final @NotNull Compilation c = new Compilation(new StdErrSink(), new IO());
		mod.parent = c;
		mod.prelude = mod.parent.findPrelude("c");
		final @NotNull ModuleContext mctx = new ModuleContext(mod);
		mod.setContext(mctx);
		final @NotNull ClassStatement cs = new ClassStatement(mod, mctx);
		cs.setName(Helpers.string_to_ident("Test"));
		final @NotNull FunctionDef fd = cs.funcDef();
		fd.setName((Helpers.string_to_ident("test")));
		@NotNull Scope3 scope3 = new Scope3(fd);
		final @NotNull VariableSequence vss = scope3.varSeq();
		final @NotNull VariableStatement vs = vss.next();
		vs.setName((Helpers.string_to_ident("x")));
		final @NotNull Qualident qu = new Qualident();
		qu.append(Helpers.string_to_ident("SystemInteger"));
		((NormalTypeName)vs.typeName()).setName(qu);
		final FunctionContext fc = (FunctionContext) fd.getContext();
		vs.typeName().setContext(fc);
		final @NotNull IdentExpression x1 = Helpers.string_to_ident("x");
		x1.setContext(fc);
		fd.scope(scope3);
		fd.postConstruct();
		cs.postConstruct();
		mod.postConstruct();

		//
		//
		//
		final ElLog.@NotNull Verbosity verbosity1 = c.gitlabCIVerbosity();
		final AccessBus ab = new AccessBus(c);
		final @NotNull PipelineLogic pl = new PipelineLogic(verbosity1, ab);
		final @NotNull GeneratePhase generatePhase = new GeneratePhase(verbosity1, pl);
		@NotNull DeducePhase dp = new DeducePhase(generatePhase, pl, verbosity1);
		@NotNull DeduceTypes2 d = dp.deduceModule(mod, verbosity1);
//		final DeduceTypes d = new DeduceTypes(mod);
		final @Nullable GenType x = DeduceLookupUtils.deduceExpression(d, x1, fc);
		System.out.println(x);
//		Assert.assertEquals(new OS_Type(BuiltInTypes.SystemInteger).getBType(), x.getBType());
//		final RegularTypeName tn = new RegularTypeName();
		final @NotNull VariableTypeName tn = new VariableTypeName();
		final @NotNull Qualident tnq = new Qualident();
		tnq.append(Helpers.string_to_ident("SystemInteger"));
		tn.setName(tnq);
		tn.setContext(fd.getContext());

//		Assert.assertEquals(new OS_Type(tn).getTypeName(), x.getTypeName());
		Assert.assertTrue(genTypeEquals(d.resolve_type(new OS_Type(tn), tn.getContext()), x));
//		Assert.assertEquals(new OS_Type(tn).toString(), x.toString());
	}

	private boolean genTypeEquals(@NotNull GenType a, @NotNull GenType b) {
		// TODO hack
		return a.typeName.equals(b.typeName) &&
				a.resolved.equals(b.resolved);
	}
}
