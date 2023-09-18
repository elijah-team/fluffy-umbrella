package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.CompilationChange;
import tripleo.elijah.comp.ICompilationBus;
import tripleo.elijah.comp.ILazyCompilerInstructions;

import java.util.ArrayList;
import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

public class CompilationBus implements ICompilationBus {
	private final Compilation      c;
	private final List<CB_Process> processes = new ArrayList<>();

	public CompilationBus(final Compilation aC) {
		c = aC;
	}

	@Override
	public void option(final @NotNull CompilationChange aChange) {
		aChange.apply(c);
	}

	@Override
	public void inst(final @NotNull ILazyCompilerInstructions aLazyCompilerInstructions) {
		// TODO 09/15 how many times are we going to do this?
//		System.out.println("** [ci] " + aLazyCompilerInstructions.get());
	}

	public void add(final CB_Action action) {
		processes.add(new CB_Process() {
			@Override
			public List<CB_Action> steps() {
				var a = new CB_Action() {
					@Override
					public String name() {
						return "Single Action Process";
					}

					@Override
					public void execute() {
						action.execute();
					}

					@Override
					public OutputString[] outputStrings() {
						return new OutputString[0];
					}
				};
				return List_of(a);
			}
		});
	}

	@Override
	public void add(final CB_Process aProcess) {
		processes.add(aProcess);
	}

	@Override
	public void run_all() {
		for (final CB_Process process : processes) {
			for (final CB_Action action : process.steps()) {
				action.execute();
			}
		}
	}
}
