/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp.pipelines;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import tripleo.elijah.comp.CompilationShit;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.PipelineMember;
import tripleo.elijah.comp.internal.CCA;
import tripleo.elijah.stages.gen_fn.GeneratedNode;

import java.util.List;

/**
 * Created 8/21/21 10:16 PM
 */
public class GeneratePipeline implements PipelineMember {
	private final PipelineLogic pipelineLogic;
	private Promise<List<GeneratedNode>, Void, Void> lgcp;

	public GeneratePipeline(CCA cca1) {
		lgcp = cca1.lgcp();

		pipelineLogic = cca1.getPipelineLogic();

		cca1.getPipelineAcceptor().accept(this);
	}

	@Override
	public void run() {
		lgcp.then(new DoneCallback<List<GeneratedNode>>() {
			@Override
			public void onDone(List<GeneratedNode> result) {
				pipelineLogic.generate(result);
			}
		});
	}

	@Override
	public void attachCB(CompilationShit cs) {
		int y = 2;
	}
}

//
//
//
