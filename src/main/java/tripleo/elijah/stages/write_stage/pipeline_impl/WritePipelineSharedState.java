package tripleo.elijah.stages.write_stage.pipeline_impl;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.generate.ElSystem;
import tripleo.util.buffer.Buffer;

import java.io.File;

/**
 * Really a record, but state is not all set at once
 */
public final class WritePipelineSharedState {
	public  ElSystem                               sys;
	public  Multimap<CompilerInstructions, String> lsp_outputs;
	public  Compilation                            c;
	public  File                                   file_prefix;
	public  Multimap<String, Buffer>               mmb;
	private GenerateResult                         gr;

	@Contract(pure = true)
	public GenerateResult getGr() {
		return gr;
	}

	@Contract(mutates = "this")
	public void setGr(final @NotNull GenerateResult aGr) {
		gr = aGr;
	}
}
