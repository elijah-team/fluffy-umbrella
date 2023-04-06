/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Contract;

/**
 * Created 8/21/21 10:16 PM
 */
public class GeneratePipeline implements PipelineMember, Consumer<Supplier<GenerateResult>> {
	//private final Compilation               c;
	private final DefaultGenerateResultSink grs;

	@Contract(pure = true)
	public GeneratePipeline(@NotNull IPipelineAccess pa) {
		//c     = pa.getCompilation();
		grs   = new DefaultGenerateResultSink(this, pa);

		latch2 = new DoubleLatch<List<EvaNode>>(nodes -> {
			pa.pipelineLogic().generate(nodes, grs);

			final List<GenerateResultItem> x = grs.resultList();

			WritePipeline.SPrintStream xps = new WritePipeline.SPrintStream();

			WritePipeline.debug_buffers_logic(x, xps);

			System.err.println("789789 "+xps.getString());

			int                            y = 2;
		});

		final DeducePipeline deducePipeline = pa.getDeducePipeline();

		deducePipeline.lgcp(latch2::notify);
	}

	@Override
	public void run() {
		latch2.notify(true);
	}

	@Override
	public void accept(Supplier<GenerateResult> t) {
		NotImplementedException.raise();
	}

	private final DoubleLatch<List<EvaNode>> latch2;
}

//
//
//
