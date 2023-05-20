package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.ci.CompilerInstructionsImpl;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.i.CD_CompilationRunnerStart;
import tripleo.elijah.comp.i.CompilationClosure;
import tripleo.elijah.comp.i.IPipelineAccess;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

public class CD_CompilationRunnerStart_1 implements CD_CompilationRunnerStart {
	@Override
	public void start(final @NotNull CompilationRunner cr,
					  final @NotNull CompilerInstructions aCompilerInstructions,
					  final boolean do_out,
					  final @NotNull IPipelineAccess pa) {
		try {
			final CB_Output out = new CB_Output();

			final CR_FindCIs              f1 = new CR_FindCIs(pa.getCompilerInput());
			final CR_ProcessInitialAction f2 = new CR_ProcessInitialAction((CompilerInstructionsImpl) aCompilerInstructions, do_out);
			final CR_AlmostComplete       f3 = new CR_AlmostComplete(cr);
			final CR_RunBetterAction      f4 = new CR_RunBetterAction();

			final @NotNull List<CR_Action> l = List_of(f1, f2, f3, f4);

			for (final CR_Action each : l) {
				each.execute(cr.crState, out);
			}
		} catch (Exception aE) {
			final CompilationClosure ccl = pa.getCompilationClosure();
			ccl.errSink().exception(aE);
		}
	}
}
