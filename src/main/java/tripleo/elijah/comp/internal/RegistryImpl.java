/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationShit;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.PipelineMember;
import tripleo.elijah.comp.pipelines.DeducePipeline;
import tripleo.elijah.comp.pipelines.GeneratePipeline;
import tripleo.elijah.comp.pipelines.WritePipeline;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.logging.ElLog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegistryImpl implements Registry {
	// TODO "scope" of pipelines ??
	private final Pipeline pipelines = new Pipeline();
	private final List<Attachable> las = new ArrayList<>();
	private final Compilation c;
	private final CompilationShit cs;
	private final String stage;
	private final boolean silent;
	private PipelineLogic pipelineLogic;
	private CCA cca;

	public RegistryImpl(final @NotNull CompilationShit acs, String aStage, boolean aSilent) {
		c = acs.getComp();
		cs = acs;


		stage = aStage;
		silent = aSilent;
	}

	public void addPipeline(final PipelineMember plm) {
		pipelines.add(plm);
	}

	@Override
	public void loadPlan() {
		final Attachable itm;

//		pipelineLogic = new PipelineLogic(cs);

		final CCA cca1 = new CCA(this, cs);
		pipelineLogic = cca1.getPipelineLogic();
		itm = pipelineLogic;
		las.add(itm);

		assert ((CompilationImpl) c).modules.size() > 0;

		final DeducePipeline dpl = new DeducePipeline(cca1);
		if (stage.equals("O")) {
			final GeneratePipeline gpl = new GeneratePipeline(cca1);
			final WritePipeline wpl = new WritePipeline(cca1);
		} else if (stage.equals("E")) {
			int y = 2;
		} else
			assert stage.equals("D");

		cca1.resolveModules(((CompilationImpl) c).modules);
//		cca1.resolveLGN(null);

		cca = cca1;
	}

	@Override
	public void runPlan(List<ElLog> elLogs) throws Exception { // TODO result-type or errSink
		for (Attachable la : las) {
			la.attachCB(cs);
		}
		for (Attachable la : pipelines.pls) {
			la.attachCB(cs);
		}

		pipelines.run();

		{
			DeducePipeline deducePipeline = (DeducePipeline) pipelines.pls
					.stream()
					.filter(x -> x instanceof DeducePipeline)
					.collect(Collectors.toList())
					.get(0);
			List<GeneratedNode> generatedNodes = deducePipeline._______lgc();
			cca.resolveLGN(generatedNodes);
		}

//		((DeferredObject<List<GeneratedNode>,Void,Void>) cca.lgcp())
//				.resolve(generatedNodes);

		cs.writeLogs(silent);
	}

	@Override
	public void GodsPlan() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	public void runPlan() throws Exception {
		runPlan(cs.getLogs());
	}

	/**
	 * Created 8/21/21 10:09 PM
	 */
	class Pipeline {
		private final List<PipelineMember> pls = new ArrayList<>();

		public void add(PipelineMember aPipelineMember) {
			pls.add(aPipelineMember);
		}

		public void run() throws Exception {
			for (PipelineMember pl : pls) {
				pl.run();
			}
		}
	}
}

//
//
//
