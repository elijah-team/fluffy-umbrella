package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationChange;
import tripleo.elijah.comp.ICompilationBus;
import tripleo.elijah.comp.ILazyCompilerInstructions;

public class CompilationBus implements ICompilationBus {
	private final Compilation c;

	public CompilationBus(final Compilation aC) {
		c = aC;
	}

	@Override
	public void option(final @NotNull CompilationChange aChange) {
		aChange.apply(c);
	}

	@Override
	public void inst(final @NotNull ILazyCompilerInstructions aLazyCompilerInstructions) {
		System.out.println("** [ci] " + aLazyCompilerInstructions.get());
	}

	@Override
	public void add(final @NotNull CB_Action action) {
		action.execute();
	}

	@Override
	public void add(final @NotNull CB_Process aProcess) {
		aProcess.steps().stream()
		        .forEach(step -> {
			        step.execute();
		        });
	}

}
