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
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;
import tripleo.elijah.stages.gen_fn.GeneratedNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created 8/21/21 10:10 PM
 */
public class DeducePipeline implements PipelineMember {
	// private final Compilation c;
	private final AccessBus __ab;
	List<GeneratedNode> lgc = new ArrayList<GeneratedNode>();
	private PipelineLogic pipelineLogic;

	public DeducePipeline(Compilation aCompilation, final AccessBus ab) {
// 		c = aCompilation;
//
// 		for (final OS_Module module : c.modules) {
// 			if (false) {
// /*
// 				new DeduceTypes(module).deduce();
// 				for (final OS_Element2 item : module.items()) {
// 					if (item instanceof ClassStatement || item instanceof NamespaceStatement) {
// 						System.err.println("8001 "+item);
// 					}
// 				}
// 				new TranslateModule(module).translate();
// */
// //				new ExpandFunctions(module).expand();
// //
// //  			final JavaCodeGen visit = new JavaCodeGen();
// //	       		module.visitGen(visit);
// 			} else {
// 				c.pipelineLogic.addModule(module);
// 			}
// 		}
//      c = ab.getCompilation();

		__ab = ab;

		ab.subscribePipelineLogic(result -> pipelineLogic = result);

//      ab.subscribe_moduleList(this);
	}

	@Override
	public void run() {
/*
		c.pipelineLogic.everythingBeforeGenerate(lgc);
		lgc = c.pipelineLogic.dp.generatedClasses.copy();
*/

		// TODO move this into a latch and wait for pipelineLogic and modules

		List<OS_Module> ms1 = __ab.getCompilation().getModules();

//      assert ms1 == ms && ms != null;

		for (final OS_Module module : ms1) {
			pipelineLogic.addModule(module);
		}

//              System.err.println(ms.size());

		final EIT_ModuleList eml = new EIT_ModuleList(ms1);

		__ab.resolveModuleList(eml);

//              assert lgc.size() == 0;
		pipelineLogic.everythingBeforeGenerate(Collections.emptyList());

//              assert lgc.size() == ms.size();
		__ab.resolveLgc(lgc);

		lgc = pipelineLogic.dp.generatedClasses.copy();

	}
}

//
//
//
