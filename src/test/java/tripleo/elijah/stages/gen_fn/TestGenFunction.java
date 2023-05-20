/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.Compilation.CompilationAlways;
import tripleo.elijah.comp.i.CompilationFlow;
import tripleo.elijah.comp.impl.DefaultCompilationFlow;
import tripleo.elijah.comp.internal.CompilationImpl;
import tripleo.elijah.comp.internal.DefaultCompilationAccess;
import tripleo.elijah.comp.internal.DefaultCompilerController;
import tripleo.elijah.factory.comp.CompilationFactory;
import tripleo.elijah.lang.*;
import tripleo.elijah.nextgen.query.Mode;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.deduce.DeducePhase;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.IFunctionMapHook;
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

	public static CompilationFlow.CompilationFlowMember parseElijah() {
		return new CompilationFlow.CompilationFlowMember() {
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {
				int y = 2;
			}
		};
	}

	@Test
	@SuppressWarnings("JUnit3StyleTestMethodInJUnit4Class")
	public void testDemoElNormalFact1Elijah() throws Exception {
		//final StdErrSink  eee = new StdErrSink();
		//final Compilation c   = CompilationFactory.mkCompilation(eee, new IO());


		final String        f    = "test/demo-el-normal/fact1.elijah";

		final File          file = new File(f);

		final CompilerInput ci_f = new CompilerInput(f); // TODO flesh this out
		final TGF_State st = new TGF_State();
		st.inputs = List_of(ci_f);

		final CompilationFlow flow = new DefaultCompilationFlow();

		flow.add(new CompilationFlow.CompilationFlowMember() {
			@Override
			public void doIt(final Compilation cc, final CompilationFlow flow) {
//				final Operation<OS_Module> om;
//				try {
//					// TODO model as query?
//					om = cc.use.realParseElijjahFile(f, file, false);
//				} catch (Exception aE) {
//					throw new RuntimeException(aE);
//				}
//				assertTrue(om.mode() == Mode.SUCCESS);
//				st.m          = om.success();
			}
		});
		flow.add(findPrelude(x -> {
			st.m.prelude = x.success();
		}));
		//flow.add(CompilationFlow.parseElijah());
		flow.add(findMainClass(st));
		flow.add(genFromEntrypoints(st));
		flow.add(getClasses(st));
		flow.add(runFunctionMapHooks(st));
		flow.add(deduceModuleWithClasses(st));
		flow.add(finishModule(st));
		flow.add(returnErrorCount(st));

		//st.c.fakeFlow(List_of(ci_f), flow);

		//st.p.then(ccc->flow.run((CompilationImpl) ccc));
		
		CompilationImpl cc = CompilationFactory.mkCompilation(new StdErrSink(), new IO());
		flow.run(cc);


		Assert.assertEquals("Not all hooks ran", 4, st.ran_hooks.size());
		Compilation c=null;
		Assert.assertEquals(16, c.errorCount());
	}

	@Contract(value = "_ -> new", pure = true)
	static CompilationFlow.@NotNull CompilationFlowMember findPrelude(final Consumer<Operation2<OS_Module>> aCopm) {
		return new CompilationFlow.CF_FindPrelude(aCopm);
	}

	@Contract(value = "_ -> new", pure = true)
	static CompilationFlow.@NotNull CompilationFlowMember findMainClass(final TGF_State st) {
		return new CF_FindPrelude(st);
	}

	public static CompilationFlow.CompilationFlowMember genFromEntrypoints(final TGF_State aSt) {
		return new CF_GenFromEntrypoints();
	}

	public static CompilationFlow.CompilationFlowMember runFunctionMapHooks(final TGF_State st) {
		return new CF_RunFunctionMapHooks(st);
	}

	public static CompilationFlow.CompilationFlowMember getClasses(final TGF_State st) {
		return new CF_GetClasses(st);
	}

	public static CompilationFlow.CompilationFlowMember deduceModuleWithClasses(final TGF_State st) {
		return new CF_DeduceModuleWithClasses(st);
	}

	public static CompilationFlow.CompilationFlowMember finishModule(final TGF_State aSt) {
		return new CF_FinishModule();
	}

	@Contract(value = "_ -> new", pure = true)
	public static CompilationFlow.@NotNull CompilationFlowMember returnErrorCount(final TGF_State aSt) {
		return new CF_ReturnErrorCount();
	}

	@Test
	@SuppressWarnings("JUnit3StyleTestMethodInJUnit4Class")
	public void testGenericA() throws Exception {
		final ErrSink     errSink = new StdErrSink();
		final Compilation c       = CompilationFactory.mkCompilation(errSink, new IO());

		final String f = "test/basic1/genericA/";

		c.feedCmdLine(List_of(f));
	}

	@Test
	public void testBasic1Backlink3Elijah() throws Exception {
		final StdErrSink  eee = new StdErrSink();
		final Compilation c   = CompilationFactory.mkCompilation(eee, new IO());

		final String ff = "test/basic1/backlink3/";
		c.feedCmdLine(List_of(ff));
	}

	@Ignore
	@Test // ignore because of generateAllTopLevelClasses
	@SuppressWarnings("JUnit3StyleTestMethodInJUnit4Class")
	public void testBasic1Backlink1Elijah() throws Exception {
		Boilerplate boilerplate = new Boilerplate();
		boilerplate.get();
		boilerplate.getGenerateFiles(boilerplate.defaultMod());

		final ErrSink     eee = new StdErrSink();
		final Compilation c   = boilerplate.comp;//CompilationFactory.mkCompilation(eee, new IO());

		final String               f    = "test/basic1/backlink1.elijah";
		final File                 file = new File(f);
		final Operation<OS_Module> om   = c.use.realParseElijjahFile(f, file, false);

		assertTrue("Method parsed correctly", om.mode() == Mode.SUCCESS);

		final OS_Module m = om.success();

		m.prelude = c.findPrelude(CompilationAlways.defaultPrelude()).success(); // TODO we dont know which prelude to find yet

		ElLog.Verbosity verbosity1 = c.gitlabCIVerbosity(); // FIXME ??

		DefaultCompilationAccess ca = new DefaultCompilationAccess(c);
		
		c.getCompilationEnclosure().setCompilationAccess(ca);
		final PipelineLogic pl = c.getCompilationEnclosure().getPipelineLogic();

		final GeneratePhase     generatePhase = pl.generatePhase;
		final GenerateFunctions gfm           = generatePhase.getGenerateFunctions(m);
		final List<EvaNode>     lgc           = new ArrayList<>();
		gfm.generateAllTopLevelClasses(lgc);

		final DeducePhase dp = new DeducePhase(generatePhase, pl, verbosity1, c.getCompilationEnclosure().getCompilationAccess());

		final WorkManager wm = new WorkManager();

		final List<EvaNode> lgf = new ArrayList<>();
		for (EvaNode generatedNode : lgc) {
			if (generatedNode instanceof EvaClass)
				lgf.addAll(((EvaClass) generatedNode).functionMap.values());
			if (generatedNode instanceof EvaNamespace)
				lgf.addAll(((EvaNamespace) generatedNode).functionMap.values());
			// TODO enum
		}

		//here
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

		//here
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

		c.getCompilationEnclosure().setPipelineLogic(pl);

		final GenerateC                 ggc = new GenerateC(m, eee, c.gitlabCIVerbosity(), boilerplate.comp.getCompilationEnclosure());
		final DefaultGenerateResultSink grs = new DefaultGenerateResultSink(null, c.get_pa());
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

	static class TGF_State {
		public    DeducePhase       dp;
		public    Iterable<EvaNode> lgc;
		public    OS_Module         m;
		public List<CompilerInput> inputs;
		public CompilationImpl c;
		protected ClassStatement        main_class;

		final DeferredObject<Compilation, Void, Void> p = new DeferredObject<>();

		List<IFunctionMapHook> ran_hooks = new ArrayList<>();
	}

	private static class CF_FindPrelude implements CompilationFlow.CompilationFlowMember {
		private final TGF_State st;

		public CF_FindPrelude(final TGF_State aSt) {
			st = aSt;
		}

		@Override
		public void doIt(final Compilation cc, final CompilationFlow flow) {
//			st.main_class = (ClassStatement) st.m.findClass("Main");
//			assert st.main_class != null;
//			st.m.entryPoints = List_of(new MainClassEntryPoint((ClassStatement) st.main_class));
		}
	}

	private static class CF_GenFromEntrypoints implements CompilationFlow.CompilationFlowMember {
		@Override
		public void doIt(final Compilation cc, final CompilationFlow flow) {

		}
	}

	private static class CF_GetClasses implements CompilationFlow.CompilationFlowMember {
		private final TGF_State st;

		public CF_GetClasses(final TGF_State aSt) {
			st = aSt;
		}

		@Override
		public void doIt(final Compilation cc, final CompilationFlow flow) {
			DefaultCompilerController dcc = new DefaultCompilerController() {
				@Override
				public void hook(final CompilationRunner cr) {
					st.c = (CompilationImpl) cr.compilation;
					st.p.resolve(cr.compilation);
				}
			};
			dcc._setInputs(cc, st.inputs);
			dcc.processOptions();
			dcc.runner();
		}
	}

	private static class CF_RunFunctionMapHooks implements CompilationFlow.CompilationFlowMember {
		private final TGF_State st;

		public CF_RunFunctionMapHooks(final TGF_State aSt) {
			st = aSt;
		}

		@Override
		public void doIt(final Compilation _cc, final CompilationFlow flow) {

			st.p.then(cc -> {
				cc.addFunctionMapHook(new IFunctionMapHook() {
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

				cc.addFunctionMapHook(new IFunctionMapHook() {
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

				cc.addFunctionMapHook(new IFunctionMapHook() {
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

				cc.addFunctionMapHook(new IFunctionMapHook() {
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

				//((CompilationImpl)cc).testMapHooks(cc.pipelineLogic.dp.functionMapHooks);

			});
		}
	}

	private static class CF_DeduceModuleWithClasses implements CompilationFlow.CompilationFlowMember {
		private final TGF_State st;

		public CF_DeduceModuleWithClasses(final TGF_State aSt) {
			st = aSt;
		}

		@Override
		public void doIt(final Compilation cc, final CompilationFlow flow) {
			st.dp.deduceModule(st.m, st.lgc, cc.gitlabCIVerbosity());
			st.dp.finish();
		}
	}

	private static class CF_FinishModule implements CompilationFlow.CompilationFlowMember {
		@Override
		public void doIt(final Compilation cc, final CompilationFlow flow) {

		}
	}

	private static class CF_ReturnErrorCount implements CompilationFlow.CompilationFlowMember {
		@Override
		public void doIt(final Compilation cc, final CompilationFlow flow) {

		}
	}
}

//
//
//
