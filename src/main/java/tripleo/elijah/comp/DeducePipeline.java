/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.GeneratedNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 8/21/21 10:10 PM
 */
public class DeducePipeline implements PipelineMember {
	private final Compilation c;
	List<GeneratedNode> lgc = new ArrayList<GeneratedNode>();

	public DeducePipeline(Compilation aCompilation) {
		c = aCompilation;

		for (final OS_Module module : c.modules) {
			if (false) {
/*
				new DeduceTypes(module).deduce();
				for (final OS_Element2 item : module.items()) {
					if (item instanceof ClassStatement || item instanceof NamespaceStatement) {
						System.err.println("8001 "+item);
					}
				}
				new TranslateModule(module).translate();
*/
//				new ExpandFunctions(module).expand();
//
//  			final JavaCodeGen visit = new JavaCodeGen();
//	       		module.visitGen(visit);
			} else {
				c.pipelineLogic.addModule(module);
			}
		}
	}

	@Override
	public void run() {
		c.pipelineLogic.everythingBeforeGenerate(lgc);
		lgc = c.pipelineLogic.dp.generatedClasses.copy();
	}
}

//
//
//
