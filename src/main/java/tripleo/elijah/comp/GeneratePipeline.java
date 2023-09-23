/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.comp.notation.GM_GenerateModule;
import tripleo.elijah.comp.notation.GM_GenerateModuleRequest;
import tripleo.elijah.comp.notation.GN_GenerateNodesIntoSink;
import tripleo.elijah.comp.notation.GN_GenerateNodesIntoSinkEnv;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleInput;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultEnv;
import tripleo.elijah.stages.gen_generic.pipeline_impl.DefaultGenerateResultSink;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.work.WorkList;
import tripleo.elijah.work.WorkManager;
import tripleo.elijah.world.i.WorldModule;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 8/21/21 10:16 PM
 */
public class GeneratePipeline implements PipelineMember/*, AccessBus.AB_LgcListener*/ {
	private final ErrSink              errSink;
	private final CompilationEnclosure ce;
	private       AccessBus            __ab;
	//	private final DeducePipeline dpl;
	private       PipelineLogic        pipelineLogic;
	private       List<EvaNode>        lgc;

	public GeneratePipeline(final CompilationEnclosure aCe) {
		this.ce = aCe;

		errSink = aCe.getCompilation().getErrSink();

		aCe.getAccessBusPromise().then(wab -> {
			wab.subscribe_lgc(aLgc -> lgc = aLgc);

			__ab = wab;
		});
		aCe.waitPipelineLogic(aPl -> pipelineLogic = aPl);
	}

	protected void generate(final @NotNull List<EvaNode> lgc,
	                        final @NotNull ErrSink aErrSink,
	                        final @NotNull EIT_ModuleList mods,
	                        final @NotNull ElLog.Verbosity verbosity,
	                        final @NotNull GenerateResultEnv aFileGen) {
		final WorkManager    wm   = new WorkManager();
		final GenerateResult gr   = __ab.gr;
		final Compilation    comp = ce.getCompilation();

		for (final @NotNull OS_Module mod : mods.getMods()) {
			final List<EvaNode> nodes = lgc.stream()
			                               .filter(aGeneratedNode -> aGeneratedNode.module() == mod)
			                               .collect(Collectors.toList());

			final var ce = comp.getCompilationEnclosure();
			new EIT_ModuleInput(mod, comp).doGenerate(nodes, aErrSink, verbosity, pipelineLogic, wm, (gr2) -> gr.additional(gr2), ce, aFileGen);
		}

		__ab.resolveGenerateResult(gr);
	}

	@Override
	public void run() {
		Preconditions.checkNotNull(pipelineLogic);
		Preconditions.checkNotNull(lgc);

		assert lgc.size() > 0;

		final IPipelineAccess pipelineAccess = ce.getCompilation().getCompilationEnclosure().getPipelineAccess();

		// FIXME Honestly doesn't belong
		final GN_GenerateNodesIntoSinkEnv env     = new GN_GenerateNodesIntoSinkEnv(null, null, null, null, null, this.ce.getPipelineAccess(), null);
		final GN_GenerateNodesIntoSink    gnis    = new GN_GenerateNodesIntoSink(env);
		final WorldModule                 mod     = (WorldModule) null;
		final GM_GenerateModuleRequest    gmr     = new GM_GenerateModuleRequest(gnis, mod, env);
		final GM_GenerateModule           gm      = new GM_GenerateModule(gmr);
		final GenerateResultEnv           fileGen = new GenerateResultEnv(new DefaultGenerateResultSink(pipelineAccess), __ab.gr, new WorkManager(), new WorkList(), gm);

		/*pipelineLogic.*/
		generate(lgc, errSink, pipelineLogic.mods, pipelineLogic.getVerbosity(), fileGen);
	}
}

//
//
//
