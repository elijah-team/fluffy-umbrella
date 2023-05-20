package tripleo.elijah.comp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.ICompilationBus;
import tripleo.elijah.comp.internal.CR_FindCIs;
import tripleo.elijah.comp.internal.CR_State;

import java.util.List;

import static tripleo.elijah.util.Helpers.List_of;

class _FindCI_Steps implements ICompilationBus.CB_Process {

	private final CompilationRunner   compilationRunner;
	final         CR_State            st1;
	private final List<CompilerInput> inputs;

	@Contract(pure = true)
	public _FindCI_Steps(final @NotNull CompilationRunner aCompilationRunner,
						 final @NotNull List<CompilerInput> aInputs) {
		compilationRunner = aCompilationRunner;
		inputs            = aInputs;
		//
		st1               = compilationRunner.crState;
	}

	@Override
	@NotNull
	public List<ICompilationBus.CB_Action> steps() {
		final ICompilationBus.CB_Action a = new _ActionBase(this, () -> {
			return new CR_FindCIs(inputs, compilationRunner.compilation, compilationRunner.progressSink);
		}, compilationRunner) {
			@Override
			public void execute() {
				st1.cur = this;
				action.execute(st1, o);
				st1.cur = null;
			}
		};

		final ICompilationBus.CB_Action b = new _ActionBase(this,
															() -> new CR_AlmostComplete(compilationRunner),
															compilationRunner) {
		};

		return List_of(a, b);
	}
}
