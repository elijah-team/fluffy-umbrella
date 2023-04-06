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
import org.jetbrains.annotations.Contract;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.DoubleLatch;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.elijah.stages.gen_generic.pipeline_impl.DefaultGenerateResultSink;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 8/21/21 10:16 PM
 */
public class GeneratePipeline implements PipelineMember, Consumer<Supplier<GenerateResult>> {
	private final Compilation c;

	@Contract(pure = true)
	public GeneratePipeline(IPipelineAccess pa) {
		c     = pa.getCompilation();
		latch = new GPL(pa);

		final DeducePipeline deducePipeline = pa.getDeducePipeline();

		deducePipeline.lgcp(new DoneCallback<List<EvaNode>>() {
			@Override
			public void onDone(final List<EvaNode> a_lgc) {
				latch.set(a_lgc);
			}
		});

		final GPL latch3 = new GPL(pa);

		latch2 = new DoubleLatch<List<EvaNode>>(nodes -> {
			latch.run();
		});
	}

	final private GPL latch;

	@Override
	public void run() {
		//latch.run(); //see latch2.run() above
	}

	@Override
	public void accept(Supplier<GenerateResult> t) {
		NotImplementedException.raise();
	}

	private class GPL implements Runnable {
		private List<EvaNode>             result;
		private DefaultGenerateResultSink grs;

		private GPL(final IPipelineAccess pa) {
			grs = new DefaultGenerateResultSink(GeneratePipeline.this, pa);
		}

		@Contract(mutates = "this")
		public void set(final List<EvaNode> aResult) {
			result = aResult;
		}

		@Override
		public void run() {
			c.pipelineLogic.generate(result, grs);

			final List<GenerateResultItem> x = grs.resultList();
			int                            y = 2;
		}
	}

	private final DoubleLatch<List<EvaNode>> latch2;
}

//
//
//
