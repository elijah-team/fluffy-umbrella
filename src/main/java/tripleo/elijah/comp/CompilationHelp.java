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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdeferred2.impl.DeferredObject;
import tripleo.elijah.comp.i.RuntimeProcess;

import static tripleo.elijah.util.Helpers.List_of;

/*
interface RuntimeProcess {
	void run(final Compilation aCompilation);

	void postProcess();

	void prepare();
}
*/

class StageToRuntime {
	@Contract("_, _, _ -> new")
	@NotNull
	public static RuntimeProcesses get(final @NotNull Stages stage,
									   final @NotNull ICompilationAccess ca,
									   final @NotNull ProcessRecord aPr) {
		final RuntimeProcesses r = new RuntimeProcesses(ca, aPr);

		r.add(stage.getProcess(ca, aPr));

		return r;
	}
}

class RuntimeProcesses {
	private final List<RuntimeProcess> processes = new ArrayList<>();
	private final ICompilationAccess   ca;
	private final ProcessRecord pr;

	public RuntimeProcesses(final @NotNull ICompilationAccess aca, final @NotNull ProcessRecord aPr) {
		ca = aca;
		pr = aPr;
	}

	public void run() {
		final Compilation comp = ca.getCompilation();

		for (RuntimeProcess runtimeProcess : processes) {
			System.err.println("***** RuntimeProcess [run    ] named " + runtimeProcess);
			runtimeProcess.run(comp);
		}
	}

	public void prepare() throws Exception {
		for (RuntimeProcess runtimeProcess : processes) {
			System.err.println("***** RuntimeProcess [prepare] named " + runtimeProcess);
			runtimeProcess.prepare();
		}
	}

	public void add(final RuntimeProcess aProcess) {
		processes.add(aProcess);
	}

	public void postProcess(ProcessRecord pr) {
		for (RuntimeProcess runtimeProcess : processes) {
			System.err.println("***** RuntimeProcess [postProcess] named " + runtimeProcess);
			runtimeProcess.postProcess();
		}

		System.err.println("***** RuntimeProcess^ [postProcess/writeLogs]");
		pr.writeLogs(ca);
	}

	public int size() {
		return processes.size();
	}

	public void run_better() {
		// do nothing. job over
		if (ca.getCompilation().stage == Stages.E) return;

		final RuntimeProcesses rt = this;

		try {
			rt.prepare();
		} catch (Exception aE) {
			throw new RuntimeException(aE);
		}
		rt.run();
		rt.postProcess(pr);
	}

	private void addPipeline(final PipelineMember aPipelineMember) {
	}

	private static class FakePipelines {
		int size() { return 4; }

		public void run() {	}
	}

	public void run_loser() {
		if (false) {
			final PipelineLogic pipelineLogic;
			final Compilation   comp      = null;
			final Stages        stage     = null;
			final FakePipelines pipelines = new FakePipelines();

			pipelineLogic = pr.pipelineLogic;

			final DeducePipeline dpl = pr.dpl;
			addPipeline(dpl);

			if (stage == Stages.O) {
				pr.setGenerateResult(null);

				final GeneratePipeline gpl = new GeneratePipeline(comp, dpl);
				addPipeline(gpl);
				final WritePipeline wpl = new WritePipeline(comp, pr, null);
				pr.consumeGenerateResult(wpl);
				addPipeline(wpl);
				final WriteMesonPipeline wmpl = new WriteMesonPipeline(comp, pr, null, wpl);
				pr.consumeGenerateResult(wmpl);
				addPipeline(wmpl);
			} else
				assert stage == Stages.D;

			assert pipelines.size() == 4;
			pipelines.run();

			ca.writeLogs();
		}
	}
}

final class EmptyProcess implements RuntimeProcess {
	public EmptyProcess(final ICompilationAccess aCompilationAccess, final ProcessRecord aPr) { }
	@Override public void run(final Compilation aCompilation) { }
	@Override public void postProcess() { }
	@Override public void prepare() { }
}

class DStageProcess implements RuntimeProcess {
	private final ICompilationAccess ca;
	private final ProcessRecord pr;

	@Contract(pure = true)
	public DStageProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
		ca = aCa;
		pr = aPr;
	}

	@Override
	public void run(final Compilation aCompilation) {
		int y=2;
	}

	@Override
	public void postProcess() {
	}

	@Override
	public void prepare() {
		//assert pr.stage == Stages.D; // FIXME
	}
}

class OStageProcess implements RuntimeProcess {
	private final ProcessRecord pr;
	private final ICompilationAccess ca;

	OStageProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
		ca = aCa;
		pr = aPr;
	}

	@Override
	public void run(final Compilation aCompilation) {
		Pipeline ps = aCompilation.pipelines;
		
		try {
			ps.run();
		} catch (Exception ex) {
			Logger.getLogger(OStageProcess.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void postProcess() {
	}

	@Override
	public void prepare() {
		Preconditions.checkNotNull(pr);
		Preconditions.checkNotNull(pr.dpl);

		Preconditions.checkNotNull(pr.pipelineLogic);
		Preconditions.checkNotNull(pr.pipelineLogic.gr);

		final DeferredObject<PipelineLogic, Void, Void> ppl = new DeferredObject<>();
		ppl.resolve(pr.pipelineLogic);
		
		final Compilation        comp = ca.getCompilation();
		
		final DeducePipeline     dpl  = pr.dpl; //new DeducePipeline      (ca);
		final GeneratePipeline   gpl  = new GeneratePipeline	(comp, dpl);
		final WritePipeline      wpl  = new WritePipeline		(comp, pr, ppl);
		final WriteMesonPipeline wmpl = new WriteMesonPipeline	(comp, pr, ppl, wpl);

		List_of(dpl, gpl, wpl, wmpl)
				.forEach(ca::addPipeline);
		
		pr.setGenerateResult(pr.pipelineLogic.gr);

		// NOTE Java needs help!
		//Helpers.<Consumer<Supplier<GenerateResult>>>List_of(wpl.consumer(), wmpl.consumer())
		List_of(wpl.consumer(), wmpl.consumer())
				.forEach(pr::consumeGenerateResult);
	}
}

//
//
//
