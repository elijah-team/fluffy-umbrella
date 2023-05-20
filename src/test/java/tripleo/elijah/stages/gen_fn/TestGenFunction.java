/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.Compilation.CompilationAlways;
import tripleo.elijah.comp.i.CompilationFlow;
import tripleo.elijah.comp.impl.DefaultCompilationFlow;
import tripleo.elijah.comp.internal.CR_State;
import tripleo.elijah.comp.internal.CompilationBus;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.entrypoints.MainClassEntryPoint;
import tripleo.elijah.factory.comp.CompilationFactory;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.FunctionMapHook;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.pipeline_impl.DefaultGenerateResultSink;
import tripleo.elijah.stages.instructions.Instruction;
import tripleo.elijah.stages.instructions.InstructionName;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.test_help.Boilerplate;
import tripleo.elijah.work.WorkManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;
import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 9/10/20 2:20 PM
 */
public class TestGenFunction {

	@Ignore
	@Test
	@SuppressWarnings("JUnit3StyleTestMethodInJUnit4Class")
	public void testDemoElNormalFact1Elijah() throws Exception {
		final StdErrSink  eee = new StdErrSink();
		final Compilation c   = CompilationFactory.mkCompilation(eee, new IO());


		final String        f    = "test/demo-el-normal/fact1.elijah";
		final File          file = new File(f);
		final CompilerInput ci_f = new CompilerInput(f); // TODO flesh this out
		//c.feedInputs // FIXME this too

		final TGF_State st = new TGF_State();

		final CompilationFlow flow = new DefaultCompilationFlow();

		flow.add(new CompilationFlow.CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {
				final Operation<OS_Module> om;
				try {
					// TODO model as query?
					om = cc.use.realParseElijjahFile(f, file, false);
				} catch (Exception aE) {
					throw new RuntimeException(aE);
				}
				assertTrue(om.mode() == Mode.SUCCESS);
				st.m = om.success();
				st.main_class = om.success();
			}
		});
		flow.add(findPrelude(x -> {st.m.prelude = x.success();}));
		//flow.add(CompilationFlow.parseElijah());
		flow.add(findMainClass(st));
		flow.add(genFromEntrypoints(st));
		flow.add(getClasses(st));
		flow.add(runFunctionMapHooks(st));
		flow.add(deduceModuleWithClasses(st));
		flow.add(finishModule(st));
		flow.add(returnErrorCount(st));
		c.fakeFlow(List_of(ci_f), flow);

		flow.run((CompilationImpl) c);

		Assert.assertEquals("Not all hooks ran", 4, st.ran_hooks.size());
		Assert.assertEquals(16, c.errorCount());
	}

	@Test
	@SuppressWarnings("JUnit3StyleTestMethodInJUnit4Class")
	public void testGenericA() throws Exception {
		final ErrSink     errSink = new StdErrSink();
		final Compilation c       = CompilationFactory.mkCompilation(errSink, new IO());

		final String f = "test/basic1/genericA/";

		c.feedCmdLine(List_of(f));
	}

	@Ignore
	@Test // ignore because of generateAllTopLevelClasses
	@SuppressWarnings("JUnit3StyleTestMethodInJUnit4Class")
	public void testBasic1Backlink1Elijah() throws Exception {
		Boilerplate boilerplate = new Boilerplate();
		boilerplate.get();
		boilerplate.getGenerateFiles(boilerplate.defaultMod());

		final ErrSink     eee = new StdErrSink();
		final Compilation c   = CompilationFactory.mkCompilation(eee, new IO());

		final String f = "test/basic1/backlink1.elijah";
		final File                 file = new File(f);
		final Operation<OS_Module> om    = c.use.realParseElijjahFile(f, file, false);

		assertTrue("Method parsed correctly", om.mode() == Mode.SUCCESS);

		final OS_Module m = om.success();

		m.prelude = c.findPrelude(CompilationAlways.defaultPrelude()).success(); // TODO we dont know which prelude to find yet

		//c.findStdLib("c"); // FIXME/TODO this!!

		//for (final CompilerInstructions ci : c.cis) { // FIXME and this!!
		//	c.use(ci, false);
		//}

		ElLog.Verbosity verbosity1 = c.gitlabCIVerbosity(); // FIXME ??

		c.__cr = new CompilationRunner(/* c, null, new CompilationBus(c), */ c._ca);
		final CR_State  crState    = (c.__cr.crState);
		crState.ca();
		final PipelineLogic pl = crState.pr.pipelineLogic();

		final GeneratePhase generatePhase = crState.pr.pipelineLogic().generatePhase;
		final GenerateFunctions gfm = generatePhase.getGenerateFunctions(m);
		final List<EvaNode> lgc = new ArrayList<>();
		gfm.generateAllTopLevelClasses(lgc);

		final DeducePhase dp = new DeducePhase(generatePhase, pl, verbosity1, crState.ca());

		WorkManager wm = new WorkManager();

		List<EvaNode> lgf = new ArrayList<>();
		for (EvaNode generatedNode : lgc) {
			if (generatedNode instanceof EvaClass)
				lgf.addAll(((EvaClass) generatedNode).functionMap.values());
			if (generatedNode instanceof EvaNamespace)
				lgf.addAll(((EvaNamespace) generatedNode).functionMap.values());
			// TODO enum
		}

		for (final EvaNode gn : lgf) {
			if (gn instanceof EvaFunction) {
				EvaFunction gf = (EvaFunction) gn;
				for (final Instruction instruction : gf.instructions()) {
					tripleo.elijah.util.Stupidity.println_out_2("8100 " + instruction);
				}
			}
		}
		//
		dp.deduceModule(m, lgc, c.gitlabCIVerbosity());
		dp.finish();
		new DeduceTypes2(m, dp).deduceFunctions(lgf);
		//
		for (final EvaNode gn : lgf) {
			if (gn instanceof EvaFunction) {
				EvaFunction gf = (EvaFunction) gn;
				tripleo.elijah.util.Stupidity.println_out_2("----------------------------------------------------------");
				tripleo.elijah.util.Stupidity.println_out_2(gf.name());
				tripleo.elijah.util.Stupidity.println_out_2("----------------------------------------------------------");
				EvaFunction.printTables(gf);
				tripleo.elijah.util.Stupidity.println_out_2("----------------------------------------------------------");
			}
		}

		final GenerateC                 ggc = new GenerateC(m, eee, c.gitlabCIVerbosity(), boilerplate.comp.getCompilationEnclosure());
		final DefaultGenerateResultSink grs = new DefaultGenerateResultSink(null, crState.pr.pa());
		ggc.generateCode(lgf, wm, grs);

		GenerateResult gr = new GenerateResult();

		for (EvaNode generatedNode : lgc) {
			if (generatedNode instanceof EvaClass) {
				ggc.generate_class((EvaClass) generatedNode, gr, grs);
			} else {
				tripleo.elijah.util.Stupidity.println_out_2(lgc.getClass().getName());
			}
		}
	}

	@Test
	public void testBasic1Backlink3Elijah() throws Exception {
		final StdErrSink eee = new StdErrSink();
		final Compilation c = CompilationFactory.mkCompilation(eee, new IO());

		final String ff = "test/basic1/backlink3/";
		c.feedCmdLine(List_of(ff));
	}

	public static CompilationFlow.CompilationFlowMember parseElijah() {
		return new CompilationFlow.CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {
				int y=2;
			}
		};
	}
	public static CompilationFlow.CompilationFlowMember genFromEntrypoints(final TGF_State aSt) {
		return new CompilationFlow.CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}
	public static CompilationFlow.CompilationFlowMember getClasses(final TGF_State st) {
		return new CompilationFlow.CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {
				cc.__cr = new CompilationRunner(/* cc, null, new CompilationBus(cc), */ cc._ca);
				final CR_State crState = cc.__cr.crState;

				crState.ca();

				cc.pipelineLogic = crState.pr.pipelineLogic();

				final GeneratePhase     generatePhase1 = cc.pipelineLogic().generatePhase;
				final GenerateFunctions gfm            = generatePhase1.getGenerateFunctions(st.m);
				st.dp = cc.pipelineLogic().dp;
				gfm.generateFromEntryPoints(st.m.entryPoints, st.dp);

				st.lgc = st.dp.generatedClasses;
			}
		};
	}
	public static CompilationFlow.CompilationFlowMember runFunctionMapHooks(final TGF_State st) {
		return new CompilationFlow.CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {
				cc.addFunctionMapHook(new FunctionMapHook() {
					@Override
					public boolean matches(FunctionDef fd) {
						final boolean b = fd.name().equals("main") && fd.getParent() == st.main_class;
						return b;
					}

					@Override
					public void apply(Collection<EvaFunction> aGeneratedFunctions) {
						assert aGeneratedFunctions.size() == 1;

						EvaFunction gf = aGeneratedFunctions.iterator().next();

						int pc = 0;
						Assert.assertEquals(InstructionName.E, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.DECL, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.AGNK, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.DECL, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.AGN, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.CALL, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.X, gf.getInstruction(pc++).getName());

						st.ran_hooks.add(this);
					}
				});

				cc.addFunctionMapHook(new FunctionMapHook() {
					@Override
					public boolean matches(FunctionDef fd) {
						final boolean b = fd.name().equals("factorial") && fd.getParent() == st.main_class;
						return b;
					}

					@Override
					public void apply(Collection<EvaFunction> aGeneratedFunctions) {
						assert aGeneratedFunctions.size() == 1;

						EvaFunction gf = aGeneratedFunctions.iterator().next();

						int pc = 0;
						Assert.assertEquals(InstructionName.E, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.DECL, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.AGNK, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.ES, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.DECL, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.AGNK, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.JE, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.CALLS, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.CALLS, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.JMP, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.XS, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.AGN, gf.getInstruction(pc++).getName());
						Assert.assertEquals(InstructionName.X, gf.getInstruction(pc++).getName());

						st.ran_hooks.add(this);
					}
				});

				cc.addFunctionMapHook(new FunctionMapHook() {
					@Override
					public boolean matches(FunctionDef fd) {
						final boolean b = fd.name().equals("main") && fd.getParent() == st.main_class;
						return b;
					}

					@Override
					public void apply(Collection<EvaFunction> aGeneratedFunctions) {
						assert aGeneratedFunctions.size() == 1;

						EvaFunction gf = aGeneratedFunctions.iterator().next();

						tripleo.elijah.util.Stupidity.println_out_2("main\n====");
						for (int i = 0; i < gf.vte_list.size(); i++) {
							final VariableTableEntry vte = gf.getVarTableEntry(i);
							tripleo.elijah.util.Stupidity.println_out_2(String.format("8007 %s %s %s", vte.getName(), vte.type, vte.potentialTypes()));
							if (vte.type.getAttached() != null) {
								Assert.assertNotEquals(OS_Type.Type.BUILT_IN, vte.type.getAttached().getType());
								Assert.assertNotEquals(OS_Type.Type.USER, vte.type.getAttached().getType());
							}
						}
						tripleo.elijah.util.Stupidity.println_out_2("");

						st.ran_hooks.add(this);
					}
				});

				cc.addFunctionMapHook(new FunctionMapHook() {
					@Override
					public boolean matches(FunctionDef fd) {
						final boolean b = fd.name().equals("factorial") && fd.getParent() == st.main_class;
						return b;
					}

					@Override
					public void apply(Collection<EvaFunction> aGeneratedFunctions) {
						assert aGeneratedFunctions.size() == 1;

						EvaFunction gf = aGeneratedFunctions.iterator().next();

						tripleo.elijah.util.Stupidity.println_out_2("factorial\n=========");
						for (int i = 0; i < gf.vte_list.size(); i++) {
							final VariableTableEntry vte = gf.getVarTableEntry(i);
							tripleo.elijah.util.Stupidity.println_out_2(String.format("8008 %s %s %s", vte.getName(), vte.type, vte.potentialTypes()));
							if (vte.type.getAttached() != null) {
								Assert.assertNotEquals(OS_Type.Type.BUILT_IN, vte.type.getAttached().getType());
								Assert.assertNotEquals(OS_Type.Type.USER, vte.type.getAttached().getType());
							}
						}
						tripleo.elijah.util.Stupidity.println_out_2("");

						st.ran_hooks.add(this);
					}
				});
			}
		};
	}
	public static CompilationFlow.CompilationFlowMember deduceModuleWithClasses(final TGF_State st) {
		return new CompilationFlow.CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {
				st.dp.deduceModule(st.m, st.lgc, cc.gitlabCIVerbosity());
				st.dp.finish();
			}
		};
	}
	public static CompilationFlow.CompilationFlowMember finishModule(final TGF_State aSt) {
		return new CompilationFlow.CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}
	public static CompilationFlow.CompilationFlowMember returnErrorCount(final TGF_State aSt) {
		return new CompilationFlow.CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {

			}
		};
	}

	static class CF_FindPrelude implements CompilationFlow.CompilationFlowMember {
		private final Consumer<Operation2<OS_Module>> copm;

		public CF_FindPrelude(final Consumer<Operation2<OS_Module>> aCopm) {
			copm = aCopm;
		}

		@Override
		public void doIt(final Compilation cc, final CompilationFlow flow) {


















			//Assert.assertTrue("Method parsed correctly", m[0] != null);





















			// TODO we dont know which prelude to find yet

			final Operation2<OS_Module> prl = cc.findPrelude(Compilation.CompilationAlways.defaultPrelude());
			assertTrue(prl.mode() == Mode.SUCCESS);

			copm.accept(prl);
		}
	}

	static CompilationFlow.CompilationFlowMember findMainClass(final TGF_State st) {
		return new CompilationFlow.CompilationFlowMember(){
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {
				final ClassStatement main_class = (ClassStatement) st.m.findClass("Main");
				assert main_class != null;
				st.m.entryPoints = List_of(new MainClassEntryPoint(main_class));
			}
		};
	}

	static CompilationFlow.CompilationFlowMember findPrelude(final Consumer<Operation2<OS_Module>> aCopm) {
		return new CF_FindPrelude(aCopm);
	}

	static class TGF_State {
		public    DeducePhase       dp;
		public    Iterable<EvaNode> lgc;
		public    OS_Module         m;
		protected OS_Element        main_class;


		List<FunctionMapHook> ran_hooks = new ArrayList<>();
	}
}

//
//
//
