/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.junit.Assert;
import org.junit.Test;
import tripleo.elijah.comp.AccessBus;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.contexts.ModuleContext;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.NormalTypeName;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.Qualident;
import tripleo.elijah.lang.Scope3;
import tripleo.elijah.lang.VariableSequence;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.lang.VariableTypeName;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.test_help.Boilerplate;
import tripleo.elijah.util.Helpers;

public class DeduceTypesTest2 {

	@Test
	public void testDeduceIdentExpression() throws ResolveError {
		final Boilerplate b = new Boilerplate();
		b.get();
		final Compilation c   = b.comp;
		final OS_Module   mod = b.defaultMod();

		mod.prelude = mod.parent.findPrelude("c").success();
		final ModuleContext mctx = new ModuleContext(mod);
		mod.setContext(mctx);
		final ClassStatement cs = new ClassStatement(mod, mctx);
		cs.setName(Helpers.string_to_ident("Test"));
		final FunctionDef fd = cs.funcDef();
		fd.setName((Helpers.string_to_ident("test")));
		final Scope3            scope3 = new Scope3(fd);
		final VariableSequence  vss    = scope3.varSeq();
		final VariableStatement vs     = vss.next();
		vs.setName((Helpers.string_to_ident("x")));
		final Qualident qu = new Qualident();
		qu.append(Helpers.string_to_ident("SystemInteger"));
		((NormalTypeName) vs.typeName()).setName(qu);
		final FunctionContext fc = (FunctionContext) fd.getContext();
		vs.typeName().setContext(fc);
		final IdentExpression x1 = Helpers.string_to_ident("x");
		x1.setContext(fc);
		fd.scope(scope3);
		fd.postConstruct();
		cs.postConstruct();
		mod.postConstruct();

		//
		//
		//
		final ElLog.Verbosity verbosity1 = Compilation.gitlabCIVerbosity();
		final AccessBus       ab         = new AccessBus(c);
		final PipelineLogic   pl         = new PipelineLogic(ab);
		final DeducePhase     dp         = pl.dp;
		final DeduceTypes2    d          = dp.deduceModule(mod, verbosity1);
//		final DeduceTypes d = new DeduceTypes(mod);
		final GenType x = DeduceLookupUtils.deduceExpression(d, x1, fc);
		System.out.println(x);
//		Assert.assertEquals(new OS_Type(BuiltInTypes.SystemInteger).getBType(), x.getBType());
//		final RegularTypeName tn = new RegularTypeName();
		final VariableTypeName tn = new VariableTypeName();
		final Qualident tnq = new Qualident();
		tnq.append(Helpers.string_to_ident("SystemInteger"));
		tn.setName(tnq);
		tn.setContext(fd.getContext());

//		Assert.assertEquals(new OS_Type(tn).getTypeName(), x.getTypeName());
		Assert.assertTrue(genTypeEquals(d.resolve_type(new OS_UserType(tn), tn.getContext()), x));
//		Assert.assertEquals(new OS_Type(tn).toString(), x.toString());
	}

	private boolean genTypeEquals(final GenType a, final GenType b) {
		// TODO hack
		return a.typeName.isEqual(b.typeName) &&
				a.resolved.isEqual(b.resolved);
	}
}
