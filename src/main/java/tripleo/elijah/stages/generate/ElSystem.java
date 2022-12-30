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
import tripleo.elijah.stages.gen_fn.GeneratedClass;
import tripleo.elijah.stages.gen_fn.GeneratedConstructor;
import tripleo.elijah.stages.gen_fn.GeneratedFunction;
import tripleo.elijah.stages.gen_fn.GeneratedNamespace;
import tripleo.elijah.stages.gen_fn.GeneratedNode;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 1/8/21 11:02 PM
 */
public class ElSystem {
	private OutputStrategy outputStrategy;
	private Compilation compilation;
	private final Map<GeneratedFunction, String> gfm_map = new HashMap<GeneratedFunction, String>();
	public boolean verbose = true;

	public void generateOutputs(@NotNull final GenerateResult gr) {
		final @NotNull OutputStrategyC outputStrategyC = new OutputStrategyC(this.outputStrategy);

		for (final GenerateResultItem ab : gr.results()) {
			final String s = generateOutputs_Internal(ab.node, ab.ty, outputStrategyC);
			assert s != null;
			ab.output = s;
		}

		if (verbose) {
			for (final GenerateResultItem ab : gr.results()) {
				if (ab.node instanceof GeneratedFunction) continue;
				System.out.println("** "+ab.node+" "+ab.output);
			}
		}
	}

	String generateOutputs_Internal(final GeneratedNode node, final GenerateResult.TY ty, final OutputStrategyC outputStrategyC) {
		final String s;
		String ss;
		if (node instanceof GeneratedNamespace) {
			final GeneratedNamespace generatedNamespace = (GeneratedNamespace) node;
			s = outputStrategyC.nameForNamespace(generatedNamespace, ty);
//			System.out.println("41 "+generatedNamespace+" "+s);
			for (final GeneratedFunction gf : generatedNamespace.functionMap.values()) {
				ss = generateOutputs_Internal(gf, ty, outputStrategyC);
				gfm_map.put(gf, ss);
			}
		} else if (node instanceof GeneratedClass) {
			final GeneratedClass generatedClass = (GeneratedClass) node;
			s = outputStrategyC.nameForClass(generatedClass, ty);
//			System.out.println("48 "+generatedClass+" "+s);
			for (final GeneratedFunction gf : generatedClass.functionMap.values()) {
				ss = generateOutputs_Internal(gf, ty, outputStrategyC);
				gfm_map.put(gf, ss);
			}
		} else if (node instanceof GeneratedFunction) {
			final GeneratedFunction generatedFunction = (GeneratedFunction) node;
			s = outputStrategyC.nameForFunction(generatedFunction, ty);
//			System.out.println("55 "+generatedFunction+" "+s);
		} else if (node instanceof GeneratedConstructor) {
			final GeneratedConstructor generatedConstructor = (GeneratedConstructor) node;
			s = outputStrategyC.nameForConstructor(generatedConstructor, ty);
//			System.out.println("55 "+generatedConstructor+" "+s);
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
