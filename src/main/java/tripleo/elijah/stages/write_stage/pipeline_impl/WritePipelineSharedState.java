package tripleo.elijah.stages.write_stage.pipeline_impl;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.functionality.f203.F203;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.generate.ElSystem;
import tripleo.util.buffer.Buffer;

import java.io.File;

/**
 * Really a record, but state is not all set at once
 */
public final class WritePipelineSharedState {
	public  IPipelineAccess                        pa;
	public  Compilation                            c;
	public  ElSystem                               sys;
	public final File base_dir;
	private GenerateResult                         gr;
	public  Multimap<CompilerInstructions, String> lsp_outputs;
	public  File                                   file_prefix;
	public  Multimap<String, Buffer>               mmb;

	public WritePipelineSharedState(final @NotNull IPipelineAccess pa0) {
		pa = pa0;
		c  = pa0.getCompilation();
		//
		base_dir = new F203(c.getErrSink(), c).chooseDirectory();
	}

	@Contract(pure = true)
	public GenerateResult getGr() {
		return gr;
	}

	@Contract(mutates = "this")
	public void setGr(final @NotNull GenerateResult aGr) {
		gr = aGr;
	}
}
