/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.generate;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.stages.gen_c.CDependencyRef;
import tripleo.elijah.stages.gen_c.OutputFileC;
import tripleo.elijah.stages.gen_fn.EvaClass;
import tripleo.elijah.stages.gen_fn.EvaConstructor;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_generic.Dependency;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.elijah.util.Stupidity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created 1/8/21 11:02 PM
 */
public class ElSystem {
	private final Supplier<OutputStrategy> outputStrategyCreator;
	private final Map<EvaFunction, String> gfm_map = new HashMap<EvaFunction, String>();
	private final boolean                  verbose;

	public ElSystem(final boolean aB, final Compilation ignoredAC, final Supplier<OutputStrategy> aCreateOutputStratgy) {
		verbose = aB;
		outputStrategyCreator = aCreateOutputStratgy;
	}

	public void generateOutputs(@NotNull GenerateResult gr) {
		final OutputStrategy outputStrategy1 = outputStrategyCreator.get();

		// TODO hard coded
		final OutputStrategyC outputStrategyC = new OutputStrategyC(outputStrategy1);

		for (final GenerateResultItem ab : gr.results()) {
			final String filename = getFilenameForNode(ab.node, ab.ty, outputStrategyC);

			assert filename != null;

			ab.output = filename;

			final Dependency dependency1 = ab.getDependency();

			if (ab.ty == GenerateResult.TY.HEADER)
				dependency1.setRef(new CDependencyRef(filename));

			for (final Dependency dependency : dependency1.getNotedDeps()) {
				if (dependency.referent != null) {
					final String filename1 = getFilenameForNode((EvaNode) dependency.referent, GenerateResult.TY.HEADER, outputStrategyC);
					dependency.setRef(new CDependencyRef(filename1));
				} else {
					int y = 2;
					assert false;
				}
			}

			gr.completeItem(ab); // CIH::addItem
		}

		if (verbose) {
			for (GenerateResultItem ab : gr.results()) {
				if (ab.node instanceof EvaFunction) continue;
				//tripleo.elijah.util.Stupidity.println_out_2
				System.out.println
						("** " + ab.node + " " + ab.output/*((CDependencyRef)ab.getDependency().getRef()).getHeaderFile()*/);
			}
		}

		Map<String, OutputFileC> outputFiles = new HashMap<>();

		for (GenerateResultItem ab : gr.results()) {
			OutputFileC outputFileC = new OutputFileC(ab.output);
			outputFiles.put(ab.output, outputFileC);
		}

		for (GenerateResultItem ab : gr.results()) {
			final OutputFileC outputFileC = outputFiles.get(ab.output);
			outputFileC.putDependencies(ab.getDependency().getNotedDeps/*dependencies*/());
		}

		for (GenerateResultItem ab : gr.results()) {
			final OutputFileC outputFileC = outputFiles.get(ab.output);
			outputFileC.putBuffer(ab.buffer);
		}

		for (GenerateResultItem ab : gr.results()) {
			final OutputFileC outputFileC = outputFiles.get(ab.output);
			ab.outputFile = outputFileC;
		}

		gr.signalDone(outputFiles);
	}

	String getFilenameForNode(final @NotNull EvaNode node,
							  final GenerateResult.TY ty,
							  final OutputStrategyC outputStrategyC) {
		final String s;

		if (node instanceof EvaNamespace evaNamespace) {
			s = outputStrategyC.nameForNamespace(evaNamespace, ty);

			logProgress(41, evaNamespace, s);

			for (final EvaFunction gf : evaNamespace.functionMap.values()) {
				final String ss = getFilenameForNode(gf, ty, outputStrategyC);
				gfm_map.put(gf, ss);
			}
		} else if (node instanceof EvaClass evaClass) {
			s = outputStrategyC.nameForClass(evaClass, ty);

			logProgress(48, evaClass, s);

			for (final EvaFunction gf : evaClass.functionMap.values()) {
				final String ss = getFilenameForNode(gf, ty, outputStrategyC);
				gfm_map.put(gf, ss);
			}
		} else if (node instanceof EvaFunction evaFunction) {
			s = outputStrategyC.nameForFunction(evaFunction, ty);

			logProgress(30, evaFunction, s);
		} else if (node instanceof EvaConstructor evaConstructor) {
			s = outputStrategyC.nameForConstructor(evaConstructor, ty);

			logProgress(55, evaConstructor, s);
			//throw new IllegalStateException("Unexpected value: " + node);
		} else {
			logProgress(140, null, null);

			throw new IllegalStateException("Can't be here.");
		}

		return s;
	}

	@Contract(pure = true)
	private void logProgress(final int code, final @NotNull EvaNode evaNode, final String s) {
		// code:
		//   41:  EvaNamespace
		//   48:  EvaClass
		//   30:  EvaFunction
		//   55:  EvaConstructor
		//   140: not above
		Stupidity.println_out_2( Integer.valueOf(code).toString() + " " + evaNode.toString() + " " + s);
	}
}

//
//
//
