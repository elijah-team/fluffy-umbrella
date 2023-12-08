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
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleInput;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.work.WorkManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 8/21/21 10:16 PM
 */
public class GeneratePipeline implements PipelineMember/*, AccessBus.AB_LgcListener*/ {
	private final ErrSink             errSink;
	private final AccessBus           __ab;
	//	private final DeducePipeline dpl;
	private       PipelineLogic       pipelineLogic;
	private       List<GeneratedNode> lgc;

	public GeneratePipeline(@NotNull final AccessBus ab) {
		errSink = ab.getCompilation().getErrSink();

		ab.subscribePipelineLogic(aPl -> pipelineLogic = aPl);
		ab.subscribe_lgc(aLgc -> lgc = aLgc);

		__ab = ab;
	}

	@Override
	public void run() {
		Preconditions.checkNotNull(pipelineLogic);
		Preconditions.checkNotNull(lgc);

		assert lgc.size() > 0;

		/*pipelineLogic.*/
		generate(lgc, errSink, pipelineLogic.mods, pipelineLogic.getVerbosity());
	}

	protected void generate(final @NotNull List<GeneratedNode> lgc,
	                        final @NotNull ErrSink aErrSink,
	                        final @NotNull EIT_ModuleList mods,
	                        final @NotNull ElLog.Verbosity verbosity) {
		final WorkManager    wm   = new WorkManager();
		final GenerateResult gr   = __ab.gr;
		final Compilation    comp = __ab.getCompilation();

		for (final @NotNull OS_Module mod : mods.getMods()) {
			final List<GeneratedNode> nodes = lgc.stream()
			                                     .filter(aGeneratedNode -> aGeneratedNode.module() == mod)
			                                     .collect(Collectors.toList());

			new EIT_ModuleInput(mod, comp).doGenerate(nodes, aErrSink, verbosity, pipelineLogic, wm, (gr2) -> gr.additional(gr2));
		}

		__ab.resolveGenerateResult(gr);
	}
}

//
//
//
