package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.AccessBus;
import tripleo.elijah.comp.DeducePipeline;
import tripleo.elijah.comp.GeneratePipeline;
import tripleo.elijah.comp.ICompilationAccess;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.PipelineMember;
import tripleo.elijah.comp.WriteMesonPipeline;
import tripleo.elijah.comp.WritePipeline;
import tripleo.elijah.comp.i.CompilationEnclosure;

public class ProcessRecord {
	public final AccessBus ab;

	public ProcessRecord(final @NotNull ICompilationAccess ca0) {
		ab = new AccessBus(ca0.getCompilation(), ca0.getCompilation().getCompilationEnclosure().getPipelineAccess());

		ab.addPipelinePlugin(new GeneratePipelinePlugin());
		ab.addPipelinePlugin(new DeducePipelinePlugin());
		ab.addPipelinePlugin(new WritePipelinePlugin());
		ab.addPipelinePlugin(new WriteMesonPipelinePlugin());

		ab.addPipelineLogic(PipelineLogic::new);
//		ab.add(DeducePipeline::new);
	}

	public void writeLogs(final @NotNull ICompilationAccess ca) {
		//ab.writeLogs();
		ca.getStage().writeLogs(ca);
	}

	public interface PipelinePlugin {
		String name();

		PipelineMember instance(final CompilationEnclosure ce);
	}

	class GeneratePipelinePlugin implements PipelinePlugin {

		@Override
		public String name() {
			return "GeneratePipeline";
		}

		@Override
		public PipelineMember instance(final CompilationEnclosure ce) {
			return new GeneratePipeline(ce);
		}
	}

	class DeducePipelinePlugin implements PipelinePlugin {

		@Override
		public String name() {
			return "DeducePipeline";
		}

		@Override
		public PipelineMember instance(final CompilationEnclosure ce) {
			return new DeducePipeline(ce);
		}
	}

	class WritePipelinePlugin implements PipelinePlugin {
		@Override
		public String name() {
			return "WritePipeline";
		}

		@Override
		public PipelineMember instance(final CompilationEnclosure ce) {
			return new WritePipeline(ce);
		}
	}

	class WriteMesonPipelinePlugin implements PipelinePlugin {
		@Override
		public String name() {
			return "WriteMesonPipeline";
		}

		@Override
		public PipelineMember instance(final CompilationEnclosure ce) {
			return new WriteMesonPipeline(ce);
		}
	}

}
