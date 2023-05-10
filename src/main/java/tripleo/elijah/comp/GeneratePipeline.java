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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleInput;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.nextgen.model.SM_Module;
import tripleo.elijah.nextgen.model.SM_Module__babyPrint;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.DoubleLatch;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.elijah.stages.gen_generic.pipeline_impl.DefaultGenerateResultSink;
import tripleo.elijah.stages.gen_generic.pipeline_impl.ProcessedNode;
import tripleo.elijah.stages.gen_generic.pipeline_impl.ProcessedNode1;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.stages.write_stage.pipeline_impl.DebugBuffersLogic;
import tripleo.elijah.stages.write_stage.pipeline_impl.SPrintStream;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created 8/21/21 10:16 PM
 */
public class GeneratePipeline implements PipelineMember, Consumer<Supplier<GenerateResult>>, AccessBus.AB_LgcListener {
	private final DefaultGenerateResultSink  grs;
	private final IPipelineAccess            pa;
	private final DoubleLatch<List<EvaNode>> latch2;
	private final ErrSink                    errSink;
	private final CompilationEnclosure       ce;
	private       PipelineLogic              pipelineLogic;
	private       List<EvaNode>              lgc;


	//public GeneratePipeline(final @NotNull AccessBus aAccessBus) {
	//	this(aAccessBus.getPipelineAccess());
	//}
	@Contract(pure = true)
	public GeneratePipeline(@NotNull IPipelineAccess pa0) {
		pa = pa0;

		//

		ce = pa0.getCompilationEnclosure();

		//

		final AccessBus ab = pa.getAccessBus();
		ab.subscribePipelineLogic(aPl -> pipelineLogic = aPl);
		ab.subscribe_lgc(aLgc -> lgc = aLgc);
		errSink = ab.getCompilation().getErrSink();

		//

		latch2 = new DoubleLatch<List<EvaNode>>(this::lgc_slot);

		//

		grs = new DefaultGenerateResultSink(this, pa);
		pa.registerNodeList(latch2::notify);
	}



	@Override
	public void lgc_slot(final List<EvaNode> aLgc) {

		final List<ProcessedNode> nodes = processLgc(aLgc);

		pa.pipelineLogic().generate(nodes, grs);

		final List<GenerateResultItem> x = grs.resultList();

		SPrintStream xps = new SPrintStream();

		for (final GenerateResultItem ab : x) {
			DebugBuffersLogic.__debug_buffers_logic_each(xps, ab);
		}

		//System.err.println("789789 "+xps.getString()); //04/15

		int y = 2;
	}

	private @NotNull List<ProcessedNode> processLgc(final @NotNull List<EvaNode> aLgc) {
		final List<ProcessedNode> l = new ArrayList<>();

		for (EvaNode evaNode : aLgc) {
			l.add(new ProcessedNode1(evaNode));
		}

		return l;
	}

	@Override
	public void run() {
		latch2.notify(true);

		if (false && pipelineLogic != null) {
			Preconditions.checkNotNull(pipelineLogic);
			Preconditions.checkNotNull(lgc);

			/*pipelineLogic().*/
			generate(lgc, errSink, pipelineLogic.mods, pipelineLogic.getVerbosity());
		}
	}

	protected void generate(final @NotNull List<EvaNode> _______lgc,
							final @NotNull ErrSink aErrSink,
							final @NotNull EIT_ModuleList mods,
							final @NotNull ElLog.Verbosity verbosity) {
		final WorkManager    wm   = new WorkManager();

		final GenerateResult gr   = pa.getAccessBus().gr;
		final Compilation    comp = pa.getCompilation();


		assert _______lgc.equals(lgc);

		for (final @NotNull OS_Module mod : mods.getMods()) {
			final List<EvaNode> nodes = lgc.stream()
					.filter(aGeneratedNode -> aGeneratedNode.module() == mod)
					.collect(Collectors.toList());

			final EIT_ModuleInput moduleInput = new EIT_ModuleInput(mod, comp);

			final SM_Module sm = moduleInput.computeSourceModel();
			SM_Module__babyPrint.babyPrint(sm);
//			simpleUsageExample();

			//GenerateResultSink grs = new DefaultGenerateResultSink(this, __ab.getPipelineAccess());

			moduleInput.doGenerate(nodes, aErrSink, verbosity, wm,
								   (gr2) -> {
									   gr.additional(gr2);
									   grs.additional(gr2);
								   }, ce);

			System.out.println("999999 " + ((DefaultGenerateResultSink) grs).resultList());
		}

		pa.getAccessBus().resolveGenerateResult(gr);
	}

	@Override
	public void accept(Supplier<GenerateResult> t) {
		NotImplementedException.raise();
	}

//	public void simpleUsageExample() {
//		Parseable pbr = Parsers.newParseable("{:x 1, :y 2}");
//		Parser    p = Parsers.newParser(defaultConfiguration());
//		Map<?, ?> m = (Map<?, ?>) p.nextValue(pbr);
////		assertEquals(m.get(newKeyword("x")), 1L);
////		assertEquals(m.get(newKeyword("y")), 2L);
////		assertEquals(Parser.END_OF_INPUT, p.nextValue(pbr));
//	}

}

//
//
//
