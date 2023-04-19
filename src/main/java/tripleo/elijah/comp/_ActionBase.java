package tripleo.elijah.comp;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.ICompilationBus;
import tripleo.elijah.comp.internal.CB_Output;

import java.util.List;
import java.util.function.Supplier;

abstract class _ActionBase implements ICompilationBus.CB_Action {
	private final   _FindCI_Steps findCISteps;
	//			final Supplier<CR_Action> actionSupplier;
	protected final CR_Action     action;
	protected final CB_Output     o = new CB_Output();

	_ActionBase(final @NotNull _FindCI_Steps aFindCISteps,
				final @NotNull Supplier<CR_Action> aActionSupplier,
				final @NotNull CompilationRunner aCompilationRunner) {
		findCISteps = aFindCISteps;
		action      = aActionSupplier.get();
		action.attach(aCompilationRunner);
	}

	@Override
	public String name() {
		return action.name();
	}

	@Override
	public void execute() {
		action.execute(findCISteps.st1, o);
	}

	@Override
	public List<ICompilationBus.OutputString> outputStrings() {
		return o.get();
	}

}
