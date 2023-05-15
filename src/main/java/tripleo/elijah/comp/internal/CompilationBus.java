package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.i.CompilationChange;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.i.ICompilationBus;
import tripleo.elijah.comp.i.ILazyCompilerInstructions;

public class CompilationBus implements ICompilationBus {
	public final  CompilerDriver cd;
	private final Compilation    c;

	public CompilationBus(final CompilationEnclosure ace) {
		c  = ace.getCompilationAccess().getCompilation();
		cd = new CompilerDriver(this);

		ace.setCompilerDriver(cd);
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
		aProcess.steps().stream().forEach(CB_Action::execute);
	}
}
