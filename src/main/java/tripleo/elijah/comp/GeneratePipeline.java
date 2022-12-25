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
import tripleo.elijah.stages.gen_fn.GeneratedNode;

import java.util.List;

/**
 * Created 8/21/21 10:16 PM
 */
public class GeneratePipeline implements PipelineMember, AccessBus.AB_LgcListener {
	private final AccessBus           __ab;
	//	private final Compilation c;
//	private final DeducePipeline dpl;
	private       PipelineLogic       pipelineLogic;
	private       List<GeneratedNode> lgc;

	public GeneratePipeline(@NotNull final AccessBus ab) {
//		c = ab.getCompilation();

		ab.subscribePipelineLogic(pll -> pipelineLogic = pll);
		ab.subscribe_lgc(this);

		__ab = ab;
	}

	@Override
	public void run() {
		Preconditions.checkNotNull(pipelineLogic);
		Preconditions.checkNotNull(lgc);

		pipelineLogic.generate(lgc, __ab.getCompilation().getErrSink());
	}

	@Override
	public void lgc_slot(final List<GeneratedNode> aX) {
		lgc = aX;
	}
}

//
//
//
