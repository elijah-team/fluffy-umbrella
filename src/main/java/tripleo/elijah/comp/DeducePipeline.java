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
import tripleo.elijah.stages.gen_fn.GeneratedNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 8/21/21 10:10 PM
 */
public class DeducePipeline implements
		PipelineMember,
		AccessBus.AB_ModuleListListener,
		AccessBus.AB_PipelineLogicListener {
	private final AccessBus           __ab;
	private       List<GeneratedNode> lgc = new ArrayList<GeneratedNode>();
	private       PipelineLogic       pipelineLogic;
	private       List<OS_Module>     ms;

	public DeducePipeline(final @NotNull AccessBus ab) {
		__ab = ab;
		__ab.getCompilation().spi(this);
	}

	@Override
	public void run() {
		Preconditions.checkNotNull(ms);
		Preconditions.checkNotNull(lgc);

		// TODO move this into a latch and wait for pipelineLogic and modules

		final List<OS_Module> ms1 = ms; //__ab.getCompilation().modules;;

		for (final OS_Module module : ms1) {
			pipelineLogic.addModule(module);
		}

		__ab.resolveModuleList(ms1);
		pipelineLogic.everythingBeforeGenerate();
		__ab.resolveLgc(lgc);

		if (!lgc.isEmpty()) throw new AssertionError();
		lgc = pipelineLogic.dp.generatedClasses.copy(); // ~~
	}

	@Override
	public void mods_slot(final List<OS_Module> injected) {
		ms = injected;
	}

	@Override
	public void pl_slot(final PipelineLogic injected) {
		pipelineLogic = injected;
	}
}

//
//
//
