/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp.pipelines;

import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.CompilationShit;
import tripleo.elijah.comp.PipelineLogic;
import tripleo.elijah.comp.PipelineMember;
import tripleo.elijah.comp.internal.CCA;
import tripleo.elijah.comp.internal.ModuleListener;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.stages.gen_fn.GeneratedNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created 8/21/21 10:10 PM
 */
public class DeducePipeline implements PipelineMember, ModuleListener {
	private final PipelineLogic pipelineLogic;
	private final DeferredObject<List<GeneratedNode>, Void, Void> lgcp;
	private List<GeneratedNode> lgc = new ArrayList<GeneratedNode>();
	private final Consumer<OS_Module> ms;

	@Override
	public void onModules(final List<OS_Module> modules) {
		for (final OS_Module module : modules) {
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
//				cca.addModule(module);
				ms.accept(module);
			}
		}
	}

	public DeducePipeline(@NotNull CCA cca) {
		cca.addModuleListener(this);

		cca.getPipelineAcceptor().accept(this);

		this.pipelineLogic = cca.getPL();

		ms = cca::addModule;

		lgcp = (DeferredObject<List<GeneratedNode>, Void, Void>) cca.lgcp();
	}

	@Override
	public void run() {
		System.err.println("****** dp [RUN   ]");

		pipelineLogic.everythingBeforeGenerate(lgc);
		lgc = pipelineLogic.dp.generatedClasses.copy(); // TODO special case 0-length

//		lgcp.resolve(lgc);
	}

	@Override
	public void attachCB(CompilationShit cs) {
		int y = 2;
	}

	public List<GeneratedNode> _______lgc() {
		return lgc;
	}
}

//
//
//
