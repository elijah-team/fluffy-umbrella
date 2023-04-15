/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdeferred2.impl.DeferredObject;
import tripleo.elijah.comp.i.RuntimeProcess;
import tripleo.elijah.util.Stupidity;

import static tripleo.elijah.util.Helpers.List_of;

final class EmptyProcess implements RuntimeProcess {
	public EmptyProcess(final ICompilationAccess aCompilationAccess, final ProcessRecord aPr) { }
	@Override public void run(final Compilation aCompilation) { }
	@Override public void postProcess() { }
	@Override public void prepare() { }
}

class DStageProcess implements RuntimeProcess {
	private final ICompilationAccess ca;
	private final ProcessRecord pr;

	@Contract(pure = true)
	public DStageProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
		ca = aCa;
		pr = aPr;
	}

	@Override
	public void run(final Compilation aCompilation) {
		int y=2;
	}

	@Override
	public void postProcess() {
	}

	@Override
	public void prepare() {
		//assert pr.stage == Stages.D; // FIXME
	}
}

class OStageProcess implements RuntimeProcess {
	private final ProcessRecord pr;
	private final ICompilationAccess ca;

	OStageProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
		ca = aCa;
		pr = aPr;
	}

	@Override
	public void run(final Compilation aCompilation) {
		Pipeline ps = aCompilation.pipelines;
		
		try {
			ps.run();
		} catch (Exception ex) {
			Logger.getLogger(OStageProcess.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void postProcess() {
	}

	@Override
	public void prepare() {
		Preconditions.checkNotNull(pr);
		Preconditions.checkNotNull(pr.dpl);

		Preconditions.checkNotNull(pr.pipelineLogic);
		Preconditions.checkNotNull(pr.pipelineLogic.gr);

		final DeferredObject<PipelineLogic, Void, Void> ppl = (DeferredObject<PipelineLogic, Void, Void>) pr.pa.getPipelineLogicPromise();
		ppl.resolve(pr.pipelineLogic);
		
		final Compilation        comp = ca.getCompilation();
		
		final DeducePipeline     dpl  = pr.dpl; //new DeducePipeline      (ca);
		final GeneratePipeline   gpl  = new GeneratePipeline	(pr.pa);
		final WritePipeline      wpl  = new WritePipeline		(pr.pa);
		final WriteMesonPipeline wmpl = new WriteMesonPipeline	(comp, pr, ppl, wpl);

		List_of(dpl, gpl, wpl, wmpl)
				.forEach(ca::addPipeline);
		
		pr.setGenerateResult(pr.pipelineLogic.gr);

		// NOTE Java needs help!
		//Helpers.<Consumer<Supplier<GenerateResult>>>List_of(wpl.consumer(), wmpl.consumer())
		List_of(wpl.consumer(), wmpl.consumer())
				.forEach(pr::consumeGenerateResult);
	}
}

//
//
//
