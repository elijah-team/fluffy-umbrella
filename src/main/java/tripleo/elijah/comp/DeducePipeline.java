/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.GeneratedNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 8/21/21 10:10 PM
 */
public class DeducePipeline implements PipelineMember, AccessBus.AB_ModuleListListener {
	private final AccessBus __ab;
	//	private final Compilation c;
	List<GeneratedNode> lgc = new ArrayList<GeneratedNode>();

	//
	//
	//
	private PipelineLogic pipelineLogic;
	private List<OS_Module> ms;
	//
	//
	//

	public DeducePipeline(final @NotNull AccessBus ab) {
//		c = ab.getCompilation();

		__ab = ab;

		ab.subscribePipelineLogic(new DoneCallback<PipelineLogic>() {
			@Override
			public void onDone(final PipelineLogic result) {
				pipelineLogic = result;
			}
		});

//		ab.subscribe_moduleList(this);
	}

	@Override
	public void run() {
		// TODO move this into a latch and wait for pipelineLogic and modules

		List<OS_Module> ms1 = __ab.getCompilation().modules;

		for (final OS_Module module : ms1) {
			pipelineLogic.addModule(module);
		}

//		System.err.println(ms.size());

		__ab.resolveModuleList(ms1);

//		assert lgc.size() == 0;
		pipelineLogic.everythingBeforeGenerate();

//		assert lgc.size() == ms.size();
		__ab.resolveLgc(lgc);

		lgc = pipelineLogic.dp.generatedClasses.copy();
	}

	@Override
	public void mods_slot(List<OS_Module> mods) {
		ms = mods;
	}
}

//
//
//
