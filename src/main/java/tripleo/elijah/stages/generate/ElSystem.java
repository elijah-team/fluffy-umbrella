/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.generate;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaConstructor;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 1/8/21 11:02 PM
 */
public class ElSystem {
	private final Map<EvaFunction, String> gfm_map = new HashMap<EvaFunction, String>();
	public        boolean                        verbose = true;
	private       OutputStrategy                 outputStrategy;
	private       Compilation                    compilation;

	public void generateOutputs(@NotNull final GenerateResult gr) {
		final @NotNull OutputStrategyC outputStrategyC = new OutputStrategyC(this.outputStrategy);

		for (final GenerateResultItem ab : gr.results()) {
			final String s = generateOutputs_Internal(ab.node, ab.ty, outputStrategyC);
			assert s != null;
			ab.output = s;
		}

		if (verbose) {
			for (final GenerateResultItem ab : gr.results()) {
				if (ab.node instanceof EvaFunction) continue;
				tripleo.elijah.util.Stupidity.println2("** " + ab.node + " " + ab.output);
			}
		}
	}

	String generateOutputs_Internal(final EvaNode node, final GenerateResult.TY ty, final OutputStrategyC outputStrategyC) {
		final String s;
		String       ss;
		if (node instanceof final EvaNamespace generatedNamespace) {
			s = outputStrategyC.nameForNamespace(generatedNamespace, ty);
//			tripleo.elijah.util.Stupidity.println2("41 "+generatedNamespace+" "+s);
			for (final EvaFunction gf : generatedNamespace.functionMap.values()) {
				ss = generateOutputs_Internal(gf, ty, outputStrategyC);
				gfm_map.put(gf, ss);
			}
		} else if (node instanceof final EvaClass generatedClass) {
			s = outputStrategyC.nameForClass(generatedClass, ty);
//			tripleo.elijah.util.Stupidity.println2("48 "+generatedClass+" "+s);
			for (final EvaFunction gf : generatedClass.functionMap.values()) {
				ss = generateOutputs_Internal(gf, ty, outputStrategyC);
				gfm_map.put(gf, ss);
			}
		} else if (node instanceof final EvaFunction generatedFunction) {
			s = outputStrategyC.nameForFunction(generatedFunction, ty);
//			tripleo.elijah.util.Stupidity.println2("55 "+generatedFunction+" "+s);
		} else if (node instanceof final EvaConstructor aEvaConstructor) {
			s = outputStrategyC.nameForConstructor(aEvaConstructor, ty);
//			tripleo.elijah.util.Stupidity.println2("55 "+generatedConstructor+" "+s);
		} else
			throw new IllegalStateException("Can't be here.");
		return s;
	}

	public void setOutputStrategy(final OutputStrategy outputStrategy) {
		this.outputStrategy = outputStrategy;
	}

	public void setCompilation(final Compilation compilation) {
		this.compilation = compilation;
	}
}

//
//
//
