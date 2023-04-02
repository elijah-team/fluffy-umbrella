/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.elijah.util.NotImplementedException;

/**
 * Created 8/21/21 10:16 PM
 */
public class GeneratePipeline implements PipelineMember, Consumer<Supplier<GenerateResult>> {
	private final Compilation    c;

	@Contract(pure = true)
	public GeneratePipeline(Compilation aCompilation, @NotNull DeducePipeline aDeducePipeline) {
		c = aCompilation;

		aDeducePipeline.lgcp(new DoneCallback<List<EvaNode>>() {
			@Override
			public void onDone(final List<EvaNode> a_lgc) {
				latch.set(a_lgc);
				//lp.then((x) -> latch.run());
			}
		});
	}

	//DeferredObject<Object, Void, Void> lp = new DeferredObject<>();

	final private GPL latch = new GPL();

	@Override
	public void run() {
		//lp.resolve(new Object());
		latch.run();
	}

	@Override
	public void accept(Supplier<GenerateResult> t) {
		NotImplementedException.raise();
	}

	private class GPL implements Runnable {
		private List<EvaNode> result;
		private DefaultGenerateResultSink grs = new DefaultGenerateResultSink();

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

	public interface GenerateResultSink {
		void add(EvaNode node);

		void additional(GenerateResult aGenerateResult);
	}

	public class DefaultGenerateResultSink implements GenerateResultSink {


		private final List<GenerateResultItem> gris = new ArrayList<>();


		@Override
		public void add(EvaNode node){
			int y=2;
		}

		@Override
		public void additional(final GenerateResult aGenerateResult) {
			for (GenerateResultItem result : aGenerateResult.results()) {
				gris.add(result);
				add(result.node);
			}
		}

		public List<GenerateResultItem> resultList() {
			return ImmutableList.copyOf(gris);
		}

	}
}

//
//
//
