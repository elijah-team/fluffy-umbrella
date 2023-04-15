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
import org.junit.Ignore;
import org.junit.Test;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.ICompilationAccess;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.comp.internal.DefaultCompilationAccess;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.lang.types.OS_UserType;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;

import static tripleo.elijah.util.Helpers.List_of;

@Ignore
public class DeduceTypesTest2 {

	@Test
	public void testDeduceIdentExpression() throws ResolveError {
/*
		final OS_Module mod = new OS_Module();
		mod.parent = new CompilationImpl(new StdErrSink(), new IO());
		mod.prelude = mod.parent.findPrelude("c");
		final ModuleContext mctx = new ModuleContext(mod);
		mod.setContext(mctx);
		final ClassStatement cs = new ClassStatement(mod, mctx);
		cs.setName(Helpers.string_to_ident("Test"));
*/

		final Compilation c = new CompilationImpl(new StdErrSink(), new IO());
		final OS_Module mod = c.moduleBuilder()
				.withPrelude("c")
				.setContext()
				.build();
		final ClassStatement cs = new ClassStatement(mod, mod.getContext());
		final ClassHeader ch = new ClassHeader(false, List_of());
		ch.setName(Helpers.string_to_ident("Test"));
		cs.setHeader(ch);

		final FunctionDef fd = cs.funcDef();
		fd.setName((Helpers.string_to_ident("test")));
		Scope3 scope3 = new Scope3(fd);
		final VariableSequence vss = scope3.varSeq();
		final VariableStatement vs = vss.next();
		vs.setName((Helpers.string_to_ident("x")));
		final Qualident qu = new Qualident();
		qu.append(Helpers.string_to_ident("SystemInteger"));
		((NormalTypeName)vs.typeName()).setName(qu);
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
		//final PipelineLogic pl = new PipelineLogic(verbosity1);
		//final GeneratePhase generatePhase = new GeneratePhase(verbosity1, pl);

		final ICompilationAccess aca = new DefaultCompilationAccess(c);
		final ProcessRecord      pr  = new ProcessRecord(aca);

		DeducePhase dp = pr.pipelineLogic.dp; //new DeducePhase(generatePhase, pl, verbosity1, aca);
		DeduceTypes2 d = dp.deduceModule(mod, dp.generatedClasses, verbosity1);

		final GenType x = DeduceLookupUtils.deduceExpression(d, x1, fc);
		tripleo.elijah.util.Stupidity.println_out_2(""+x);
//		Assert.assertEquals(new OS_BuiltInType(BuiltInTypes..SystemInteger).getBType(), x.getBType());
//		final RegularTypeName tn = new RegularTypeName();
		final VariableTypeName tn = new VariableTypeName();
		final Qualident tnq = new Qualident();
		tnq.append(Helpers.string_to_ident("SystemInteger"));
		tn.setName(tnq);
		tn.setContext(fd.getContext());

//		Assert.assertEquals(new OS_UserType(tn).getTypeName(), x.getTypeName());
		Assert.assertTrue(genTypeEquals(d.resolve_type(new OS_UserType(tn), tn.getContext()), x));
//		Assert.assertEquals(new OS_UserType(tn).toString(), x.toString());
	}

	private boolean genTypeEquals(GenType a, GenType b) {
		// TODO hack
		return a.typeName.equals(b.typeName) &&
				a.resolved.equals(b.resolved);
	}
}
