package tripleo.elijah.ut;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationChange;
import tripleo.elijah.comp.ICompilationBus;
import tripleo.elijah.comp.ILazyCompilerInstructions;

import java.util.ArrayList;
import java.util.List;

public class UT_CompilationBus implements ICompilationBus {
	private final Compilation   c;
	private final UT_Controller utc;
	List<CB_Action> actions = new ArrayList<>();
	private       CB_Process       last;
	private final List<CB_Process> p = new ArrayList<>();


	public UT_CompilationBus(final Compilation aC, final UT_Controller aUTController) {
		c   = (aC);
		utc = aUTController;
		//utc.cb = this;
	}

	@Override
	public void option(final @NotNull CompilationChange aChange) {
		aChange.apply(c);
	}

	@Override
	public void inst(final @NotNull ILazyCompilerInstructions aLazyCompilerInstructions) {
		System.out.println("** [ci] " + aLazyCompilerInstructions.get().getFilename());
	}

	public void add(final CB_Action action) {
//		action.execute();
		actions.add(action);
	}

	@Override
	public void add(final CB_Process aProcess) {
		last = aProcess;
		p.add(last);
	}

	public CB_Process getLast() {
		return last;
	}
}
