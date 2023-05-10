/*  -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp.notation;

import org.checkerframework.checker.nullness.qual.Nullable;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.LibraryStatementPart;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateFiles;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.OutputFileFactory;
import tripleo.elijah.stages.gen_generic.OutputFileFactoryParams;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.stages.gen_generic.pipeline_impl.ProcessedNode;
import tripleo.elijah.stages.gen_generic.pipeline_impl.ProcessedNode1;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.work.WorkManager;

import java.util.List;
import java.util.stream.Collectors;

public class GN_GenerateNodesIntoSink implements GN_Notable {
	private final GenerateResultSink resultSink;
	private final EIT_ModuleList       mods;
	private final List<ProcessedNode>  processedNodes;
	private final GenerateResult       gr;
	private final ElLog.Verbosity      verbosity;
	private final IPipelineAccess      pa;
	private final CompilationEnclosure ce;

	public GN_GenerateNodesIntoSink(final List<ProcessedNode> algc,
									final GenerateResultSink aResultSink,
									final EIT_ModuleList aModuleList,
									final ElLog.Verbosity aVerbosity,
									final GenerateResult agr,
									final IPipelineAccess aPa,
									final CompilationEnclosure aCe) {
		mods           = aModuleList;
		processedNodes = algc;
		resultSink     = aResultSink;
		gr             = agr;
		verbosity      = aVerbosity;
		pa             = aPa;
		ce             = aCe;
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


			final OutputFileFactoryParams params        = new OutputFileFactoryParams(mod, errSink, verbosity, ce);
			final GenerateFiles           generateFiles = OutputFileFactory.create(lang, params);
			//final GenerateC               generateC     = new GenerateC(mod, errSink, verbosity, this);
			final GenerateResult ggr = run3(mod, processedNodes, wm, generateFiles, resultSink);
			wm.drain();
			gr.results().addAll(ggr.results());


			//final EIT_ModuleInput emi = new EIT_ModuleInput(mod, mod.getCompilation());
			//emi.doGenerate(lgc, errSink, verbosity, params.getPipelineLogic(), wm, x->{});
		}

		wm.drain(); // TODO here??


		pa.getAccessBus().resolveGenerateResult(gr);
	}

	protected GenerateResult run3(OS_Module mod, List<ProcessedNode> lgc, WorkManager wm, GenerateFiles ggc, final GenerateResultSink aResultSink) {
		GenerateResult gr1 = new GenerateResult();

		for (ProcessedNode processedNode : processedNodes) {
			final EvaNode evaNode = ((ProcessedNode1) processedNode).getEvaNode();

			if (! (processedNode.matchModule(mod))) continue; // README curious

			if (processedNode.isContainerNode()) {
				processedNode.processContainer(ggc, gr1, aResultSink);

				processedNode.processConstructors(ggc, gr1, aResultSink, wm);
				processedNode.processFunctions(ggc, gr1, aResultSink, wm);
				processedNode.processClassMap(ggc, gr1, aResultSink, wm);

			} else {
				tripleo.elijah.util.Stupidity.println_out_2("2009 " + evaNode.getClass().getName());
			}
		}

		return gr1;
	}
}
