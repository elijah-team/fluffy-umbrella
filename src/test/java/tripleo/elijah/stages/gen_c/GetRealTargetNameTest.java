/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */

package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import tripleo.elijah.comp.*;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;

import static org.easymock.EasyMock.*;

public class GetRealTargetNameTest {

	GeneratedFunction gf;
	OS_Module mod;

	@Before
	public void setUp() throws Exception {
		mod = mock(OS_Module.class);
		FunctionDef fd = mock(FunctionDef.class);
		gf = new GeneratedFunction(fd);
	}

//	@Test // too complicated
	@SuppressWarnings("JUnit3StyleTestMethodInJUnit4Class")
	public void testManualXDotFoo() {
		IdentExpression x_ident = Helpers.string_to_ident("x");
		@NotNull IdentExpression foo_ident = Helpers.string_to_ident("foo");
		//
		// create x.foo, where x is a VAR and foo is unknown
		// neither has type information
		// GenerateC#getRealTargetName doesn't use type information
		// TODO but what if foo was a property instead of a member
		//
		final RegularTypeName typeName = new RegularTypeName(null);
		typeName.setName(Helpers.string_to_qualident("X_Type"));
 		OS_Type type = new OS_Type(typeName);
		TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, type, x_ident);

		final VariableSequence seq = new VariableSequence();
		final VariableStatement x_var = //mock(VariableStatement.class); // can't replay visitGen
			new VariableStatement(seq);

		int int_index = gf.addVariableTableEntry("x", VariableTableType.VAR, tte, x_var);
		int ite_index = gf.addIdentTableEntry(foo_ident, null);
		IdentIA ident_ia = new IdentIA(ite_index, gf);
		final IntegerIA integerIA = new IntegerIA(int_index, gf);
		ident_ia.setPrev(integerIA);

		Emit.emitting = false;

		//
		//
		//

		PipelineLogic pipelineLogic = new PipelineLogic(ElLog.Verbosity.VERBOSE);
		GenerateC c = new GenerateC(mod, new StdErrSink(), ElLog.Verbosity.VERBOSE, pipelineLogic); // TODO do we want silent?

		//
		//
		//

		Compilation comp = new Compilation(new StdErrSink(), new IO());
		OS_Module mod = new OS_Module(); // hard to mock
		mod.setParent(comp);

		final DeducePhase phase = new DeducePhase(null, pipelineLogic, null);

		final DeduceTypes2 deduceTypes2 = new DeduceTypes2(mod, phase);
		final Context ctx = mock(Context.class);
		(gf.getIdentTableEntry(0)).setDeduceTypes2(deduceTypes2, ctx, gf);

		final LookupResultList lrl = new LookupResultList();
		lrl.add(x_ident.getText(), 1,x_var, null);

//		expect(ctx.lookup(foo_ident.getText())).andReturn(lrl);
		expect(ctx.lookup(x_ident.getText())).andReturn(lrl);
		expect(ctx.lookup(x_ident.getText())).andReturn(lrl);

		replay(ctx);

		final GenType genType = new GenType();
		genType.typeName = type;
		integerIA.getEntry().resolveType(genType);
		//
		//
		//

		String x = c.getRealTargetName(gf, ident_ia, Generate_Code_For_Method.AOG.GET, null);
		Assert.assertEquals("vvx->vmfoo", x);
	}
}

//
//
//
