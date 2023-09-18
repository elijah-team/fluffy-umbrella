package tripleo.elijah.ut;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationChange;
import tripleo.elijah.comp.ICompilationBus;
import tripleo.elijah.comp.ILazyCompilerInstructions;
import tripleo.elijah.util.NotImplementedException;

public class UT_CompilationBus implements ICompilationBus {
	private final Compilation   c;
	private final UT_Controller utc;
	private final List<CB_Process> p = new ArrayList<>();
	List<CB_Action> actions = new ArrayList<>();
	private       CB_Process       last;


	public UT_CompilationBus(final Compilation aC, final UT_Controller aUTController) {
		c   = (aC);
		utc = aUTController;
		//utc.cb = this;
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

	@Override
	public void inst(final @NotNull ILazyCompilerInstructions aLazyCompilerInstructions) {
		// TODO 09/15 how many times are we going to do this?
//		System.out.println("** [ci] " + aLazyCompilerInstructions.get().getFilename());
	}

	@Override
	public void option(final @NotNull CompilationChange aChange) {
		aChange.apply(c);
	}

	@Override
	public void run_all() {
		throw new NotImplementedException();
	}
}
