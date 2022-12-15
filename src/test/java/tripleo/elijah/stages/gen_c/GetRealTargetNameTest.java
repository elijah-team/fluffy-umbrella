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
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.stages.instructions.IntegerIA;
import tripleo.elijah.stages.instructions.VariableTableType;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.Helpers;

import static org.easymock.EasyMock.mock;

public class GetRealTargetNameTest {

	GeneratedFunction gf;
	OS_Module mod;

	@Before
	public void setUp() throws Exception {
		mod = mock(OS_Module.class);
		FunctionDef fd = mock(FunctionDef.class);
		gf = new GeneratedFunction(fd);
	}

	@Test
	public void testManualXDotFoo() {
		@NotNull IdentExpression x_ident = Helpers.string_to_ident("x");
		@NotNull IdentExpression foo_ident = Helpers.string_to_ident("foo");
		//
		// create x.foo, where x is a VAR and foo is unknown
		// neither has type information
		// GenerateC#getRealTargetName doesn't use type information
		// TODO but what if foo was a property instead of a member
		//
		@Nullable OS_Type type = null;
		@NotNull TypeTableEntry tte = gf.newTypeTableEntry(TypeTableEntry.Type.SPECIFIED, type, x_ident);
		int int_index = gf.addVariableTableEntry("x", VariableTableType.VAR, tte, mock(VariableStatement.class));
		int ite_index = gf.addIdentTableEntry(foo_ident, null);
		@NotNull IdentIA ident_ia = new IdentIA(ite_index, gf);
		ident_ia.setPrev(new IntegerIA(int_index, gf));
		//
		@NotNull PipelineLogic pipelineLogic = new PipelineLogic(Compilation.gitlabCIVerbosity());
		@NotNull GenerateC c = new GenerateC(mod, new StdErrSink(), ElLog.Verbosity.SILENT, pipelineLogic); // TODO do we want silent?
		//
		Emit.emitting = false;
		@NotNull String x = c.getRealTargetName(gf, ident_ia);
		Assert.assertEquals("vvx->vmfoo", x);
	}
}

//
//
//
