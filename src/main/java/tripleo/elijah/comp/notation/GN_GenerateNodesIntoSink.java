package tripleo.elijah.comp.notation;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaContainerNC;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.OutputFileFactory;
import tripleo.elijah.stages.gen_generic.OutputFileFactoryParams;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.work.WorkManager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GN_GenerateNodesIntoSink implements GN_Notable {
	private final GenerateResultSink resultSink;
	private final EIT_ModuleList     mods;
	private final List<EvaNode>      lgc;
	private final GenerateResult     gr;
	private final ElLog.Verbosity    verbosity;
	private final PipelineLogic pipelineLogic;
	private final IPipelineAccess pa;

	public GN_GenerateNodesIntoSink(final List<EvaNode> algc, final GenerateResultSink aResultSink, final EIT_ModuleList aModuleList, final ElLog.Verbosity aVerbosity, final GenerateResult agr, final PipelineLogic aPipelineLogic, final IPipelineAccess aPa) {
		mods       = aModuleList;
		lgc        = algc;
		resultSink = aResultSink;
		gr         = agr;
		verbosity  = aVerbosity;
		pipelineLogic = aPipelineLogic;
		pa = aPa;
	}

	/*
	 * See AccessBus#doModule
	 */
	@Override
	public void run() {
		final WorkManager wm = new WorkManager();

		for (final OS_Module mod : mods.stream().collect(Collectors.toList())) {
			// README use any errSink, they should all be the same
			final ErrSink errSink = mod.getCompilation().getErrSink();

			final LibraryStatementPart lsp = mod.getLsp();


			if (lsp == null) {
				tripleo.elijah.util.Stupidity.println_err_2("7777777777777777777 mod.getFilename " + mod.getFileName());
				continue;
			}


			final CompilerInstructions ci    = lsp.getInstructions();
			final @Nullable String     lang2 = ci.genLang();


			final @Nullable String lang = lang2 == null ? "c" : lang2;


			final OutputFileFactoryParams params        = new OutputFileFactoryParams(mod, errSink, verbosity, pipelineLogic);
			final GenerateFiles           generateFiles = OutputFileFactory.create(lang, params);
			//final GenerateC               generateC     = new GenerateC(mod, errSink, verbosity, this);
			final GenerateResult ggr = run3(mod, lgc, wm, generateFiles, resultSink);
			wm.drain();
			gr.results().addAll(ggr.results());


			//final EIT_ModuleInput emi = new EIT_ModuleInput(mod, mod.getCompilation());
			//emi.doGenerate(lgc, errSink, verbosity, params.getPipelineLogic(), wm, x->{});
		}

		wm.drain(); // TODO here??








		pa.getAccessBus().resolveGenerateResult(gr);
	}

	protected GenerateResult run3(OS_Module mod, @NotNull List<EvaNode> lgc, WorkManager wm, GenerateFiles ggc, final GenerateResultSink aResultSink) {
		GenerateResult gr = new GenerateResult();

		for (EvaNode evaNode : lgc) {
			if (evaNode.module() != mod) continue; // README curious

			if (evaNode instanceof EvaContainerNC) {
				final EvaContainerNC nc = (EvaContainerNC) evaNode;

				nc.generateCode(ggc, gr, aResultSink);
				if (nc instanceof EvaClass) {
					final EvaClass evaClass = (EvaClass) nc;

					final @NotNull Collection<EvaNode> gn2 = GenerateFiles.constructors_to_list_of_generated_nodes(evaClass.constructors.values());
					GenerateResult                     gr3 = ggc.generateCode(gn2, wm, aResultSink);
					gr.additional(gr3);
					aResultSink.additional(gr3);
				}

				final @NotNull Collection<EvaNode> gn1 = GenerateFiles.functions_to_list_of_generated_nodes(nc.functionMap.values());
				GenerateResult                     gr2 = ggc.generateCode(gn1, wm, aResultSink);
				gr.additional(gr2);
				aResultSink.additional(gr2);

				final @NotNull Collection<EvaNode> gn2 = GenerateFiles.classes_to_list_of_generated_nodes(nc.classMap.values());
				GenerateResult                     gr4 = ggc.generateCode(gn2, wm, aResultSink);
				gr.additional(gr4);
				aResultSink.additional(gr4);
			} else {
				tripleo.elijah.util.Stupidity.println_out_2("2009 " + evaNode.getClass().getName());
			}
		}

		return gr;
	}
}
