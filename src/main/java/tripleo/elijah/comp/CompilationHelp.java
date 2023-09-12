/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import com.google.common.base.*;
import org.jetbrains.annotations.*;
import tripleo.elijah.comp.internal.*;
import tripleo.vendor.mal.*;

interface RuntimeProcess {
	void run();

	void postProcess();

	void prepare() throws Exception;
}

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
	private final ICompilationAccess ca;
	private final ProcessRecord      pr;
	private       RuntimeProcess     process;

	public RuntimeProcesses(final @NotNull ICompilationAccess aca, final @NotNull ProcessRecord aPr) {
		ca = aca;
		pr = aPr;
	}

	public void add(final RuntimeProcess aProcess) {
		process = aProcess;
	}

	public int size() {
		return process == null ? 0 : 1;
	}

	public void run_better() throws Exception {
		// do nothing. job over
		if (ca.getStage() == Stages.E) return;

		// rt.prepare();
		System.err.println("***** RuntimeProcess [prepare] named " + process);
		process.prepare();

		// rt.run();
		System.err.println("***** RuntimeProcess [run    ] named " + process);
		process.run();

		// rt.postProcess(pr);
		System.err.println("***** RuntimeProcess [postProcess] named " + process);
		process.postProcess();

		System.err.println("***** RuntimeProcess^ [postProcess/writeLogs]");
		pr.writeLogs(ca);
	}
}

final class EmptyProcess implements RuntimeProcess {
	public EmptyProcess(final ICompilationAccess ignoredACompilationAccess, final ProcessRecord ignoredAPr) {
	}

	@Override
	public void run() {
	}

	@Override
	public void postProcess() {
	}

	@Override
	public void prepare() {
	}
}

class DStageProcess implements RuntimeProcess {
	private final ICompilationAccess ca;
	private final ProcessRecord      pr;

	@Contract(pure = true)
	public DStageProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
		ca = aCa;
		pr = aPr;
	}

	@Override
	public void run() {
		final int y = 2;
	}

	@Override
	public void postProcess() {
	}

	@Override
	public void prepare() {
		assert ca.getStage() == Stages.D;
	}
}

class OStageProcess implements RuntimeProcess {
	final         stepA_mal.MalEnv2 env;
	private final ProcessRecord     pr;
	private final ICompilationAccess ca;

	OStageProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
		ca = aCa;
		pr = aPr;

		env = new stepA_mal.MalEnv2(null); // TODO what does null mean?

		Preconditions.checkNotNull(pr.ab);
		env.set(new types.MalSymbol("add-pipeline"), new _AddPipeline__MAL(pr.ab));
	}

	@Override
	public void run() {
		final AccessBus ab = pr.ab;

		ab.subscribePipelineLogic((pl) -> {
			final Compilation comp = ca.getCompilation();
			final Pipeline    ps   = comp.getPipelines();

			try {
				ps.run();

				ab.writeLogs();
			} catch (final Exception ex) {
//				Logger.getLogger(OStageProcess.class.getName()).log(Level.SEVERE, "Error during Piplines#run from OStageProcess", ex);
				comp.getErrSink().exception(ex);
			}
		});
	}

	@Override
	public void postProcess() {
	}

	@Override
	public void prepare() throws Exception {
		Preconditions.checkNotNull(pr);
		Preconditions.checkNotNull(pr.ab.gr);

		final AccessBus ab = pr.ab;

//		env.re("(def! GeneratePipeline 'native)");
		env.re("(add-pipeline 'DeducePipeline)"); // FIXME note moved from ...

		env.re("(add-pipeline 'GeneratePipeline)");
		env.re("(add-pipeline 'WritePipeline)");
		env.re("(add-pipeline 'WriteMesonPipeline)");

		ab.subscribePipelineLogic(pl -> {
			final Compilation comp = ca.getCompilation();

			comp.mod.modules.stream().forEach(pl::addModule);
		});
	}

	private static class _AddPipeline__MAL extends types.MalFunction {
		private final AccessBus ab;

		public _AddPipeline__MAL(final AccessBus aAb) {
			ab = aAb;
		}

		public types.MalVal apply(final types.MalList args) {
			final types.MalVal a0 = args.nth(0);

			if (a0 instanceof final types.MalSymbol pipelineSymbol) {
				// 0. accessors
				final String pipelineName = pipelineSymbol.getName();

				// 1. observe side effect
				final ProcessRecord.PipelinePlugin pipelinePlugin = ab.getPipelinePlugin(pipelineName);
				if (pipelinePlugin == null)
					return types.False;

				// 2. produce effect
				ab.add(pipelinePlugin::instance);
				return types.True;
			} else {
				// TODO exception? errSink??
				return types.False;
			}
		}
	}
}

//
//
//
