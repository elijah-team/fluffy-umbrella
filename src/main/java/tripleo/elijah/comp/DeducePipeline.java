/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.IPipelineAccess;
import tripleo.elijah.stages.deduce.pipeline_impl.DeducePipelineImpl;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.util.NotImplementedException;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 8/21/21 10:10 PM
 */
public class DeducePipeline implements PipelineMember, Consumer<Supplier<GenerateResult>> {
	private final DeducePipelineImpl impl;

	public DeducePipeline(final @NotNull IPipelineAccess pa) {
		logProgress("***** Hit DeducePipeline constructor");
		impl = new DeducePipelineImpl(pa);
	}

	protected void logProgress(final String g) {
		tripleo.elijah.util.Stupidity.println_err_2(g);
	}

	@Override
	public void run() {
		logProgress("***** Hit DeducePipeline #run");
		impl.run();
	}

	@Override
	public void accept(final Supplier<GenerateResult> t) {
		NotImplementedException.raise();
	}
}

//
//
//
