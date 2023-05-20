/*  -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp.internal;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.*;
import tripleo.elijah.comp.notation.GN_Notable;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.logging.ElLog;

import java.util.List;

public class CR_State {
	//private final CompilationRunner         compilationRunner;
	public        ICompilationBus.CB_Action cur;
	public        ProcessRecord             pr;
	public        RuntimeProcesses          rt;
	ICompilationAccess ca;

	@Contract(pure = true)
	public CR_State(final CompilationRunner ignoredACompilationRunner, ICompilationAccess aCa) {
		//compilationRunner       = aCompilationRunner;
		ca                      = aCa;
		ca.getCompilation().set_pa(new ProcessRecord_PipelineAccess());
		pr                      = new ProcessRecordImpl(ca);
	}

	public ICompilationAccess ca() {
		return ca;
	}


	public interface PipelinePlugin {
		String name();

		PipelineMember instance(final @NotNull AccessBus ab0);
	}

	private static class ProcessRecordImpl implements ProcessRecord {
		private final PipelineLogic                              pipelineLogic;
		//private final DeducePipeline                             dpl;
		private final ICompilationAccess                         ca;
		private       AccessBus                                  ab;
		private final IPipelineAccess                            pa;

		public ProcessRecordImpl(final @NotNull ICompilationAccess ca0) {
			ca                      = ca0;

			//ca.getCompilation().getCompilationEnclosure().getAccessBusPromise()
			//		.then(iab->ab=iab);
			ca.getCompilation().getCompilationEnclosure().getAccessBusPromise().then((final @NotNull AccessBus iab) -> {
				ab  = iab;
			});

			pa            = ca.getCompilation().get_pa();

			pipelineLogic = new PipelineLogic(pa);
			//dpl           = new DeducePipeline(pa);

			//ca.getCompilation().getCompilationEnclosure().getAccessBusPromise().then((final @NotNull AccessBus iab) -> {
			//	ab  = iab;
			//	env = ab.env();
			//});
		}

		@Override
		public void writeLogs() {
			ca.getCompilation().cfg.stage.writeLogs(ca);
		}

		@Contract(pure = true)
		@Override
		public PipelineLogic pipelineLogic() {
			return pipelineLogic;
		}

		@Contract(pure = true)
		@Override
		public IPipelineAccess pa() {
			return pa;
		}

		@Contract(pure = true)
		@Override
		public ICompilationAccess ca() {
			return ca;
		}

		@Contract(pure = true)
		@Override
		public AccessBus ab() {
			return ab;
		}
	}

	public static class GeneratePipelinePlugin implements PipelinePlugin {
		@Override
		public String name() {
			return "GeneratePipeline";
		}

		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new GeneratePipeline(ab0.getPipelineAccess());
		}
	}

	public static class DeducePipelinePlugin implements PipelinePlugin {
		@Override
		public String name() {
			return "DeducePipeline";
		}

		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new DeducePipeline(ab0.getPipelineAccess());
		}
	}

	public static class WritePipelinePlugin implements PipelinePlugin {
		@Override
		public String name() {
			return "WritePipeline";
		}

		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new WritePipeline(ab0.getPipelineAccess());
		}
	}

	public static class WriteMesonPipelinePlugin implements PipelinePlugin {
		@Override
		public String name() {
			return "WriteMesonPipeline";
		}

		@Override
		public PipelineMember instance(final @NotNull AccessBus ab0) {
			return new WriteMesonPipeline(ab0);
		}
	}

	class ProcessRecord_PipelineAccess implements IPipelineAccess {
		private final DeferredObject<List<EvaNode>, Void, Void> nlp = new DeferredObject<>();
		private final DeferredObject<PipelineLogic, Void, Void> ppl = new DeferredObject<>();
		private       List<CompilerInput>                       inp;
		private       WritePipeline                             _wpl;
		private       AccessBus                                 _ab;

		@Override
		public Compilation getCompilation() {
			return ca.getCompilation();
		}

		@Override
		public DeducePipeline getDeducePipeline() {
		//	return getProcessRecord().dpl();
			throw new Error("237 dpl");
		}

		@Override
		public PipelineLogic pipelineLogic() {
			return getProcessRecord().pipelineLogic();
		}

		@Override
		public ProcessRecord getProcessRecord() {
			return pr;
		}

		@Override
		public DeferredObject<PipelineLogic, Void, Void> getPipelineLogicPromise() {
			return ppl;
		}

		@Override
		public void setNodeList(final List<EvaNode> aEvaNodeList) {
			nlp/*;)*/.resolve(aEvaNodeList);
		}

		@Override
		public void registerNodeList(final DoneCallback<List<EvaNode>> done) {
			nlp.then(done);
		}

		@Override
		public void _setAccessBus(final AccessBus ab) {
			_ab = ab;
		}

		@Override
		public AccessBus getAccessBus() {
			return _ab;
		}

		@Override
		public WritePipeline getWitePipeline() {
			return _wpl;
		}

		@Override
		public void setWritePipeline(final WritePipeline aWritePipeline) {
			_wpl = aWritePipeline;
		}

		@Override
		public void notate(final int provenance, final @NotNull GN_Notable aNotable) {
			aNotable.run();
		}

		@Override
		public List<CompilerInput> getCompilerInput() {
			return inp;
		}

		@Override
		public void setCompilerInput(final List<CompilerInput> aInputs) {
			inp = aInputs;
		}

		@Override
		public CompilationClosure getCompilationClosure() {
			return getCompilation().getCompilationClosure();
		}

		@Override
		public CompilationEnclosure getCompilationEnclosure() {
			return getCompilation().getCompilationEnclosure();
		}

		@Override
		public void addLog(final ElLog aLOG) {
			getCompilationEnclosure().getPipelineLogic().addLog(aLOG);
		}
	}
}

//
// vim:set shiftwidth=4 softtabstop=0 noexpandtab:
//
