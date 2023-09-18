package tripleo.elijah.comp;

import tripleo.elijah.comp.i.CB_Monitor;
import tripleo.elijah.comp.i.CB_OutputString;
import tripleo.elijah.comp.internal.CB_Output;
import tripleo.elijah.comp.internal.CompilationBus;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;

public interface ICompilationBus {
	interface CB_Action {
		void execute(CB_Monitor aMonitor);

		String name();

		List<CB_OutputString> outputStrings();

//		OutputString[] outputStrings();
	}

	interface CB_Process {
		default void execute(CompilationBus aCompilationBus) {
			final CB_Monitor monitor = new CB_Monitor() {
				@Override
				public void reportFailure(final tripleo.elijah.comp.i.CB_Action aCBAction, final CB_Output aCB_output) {
					throw new NotImplementedException();
				}

				@Override
				public void reportSuccess(final tripleo.elijah.comp.i.CB_Action aCBAction, final CB_Output aCB_output) {
					throw new NotImplementedException();
				}
			};

			for (final CB_Action action : steps()) {
				action.execute(monitor);
			}
		}

		default String name() {return getClass().getName();}

		List<CB_Action> steps();
	}

	class COutputString implements OutputString {

		private final String _text;

		public COutputString(final String aText) {
			_text = aText;
		}

		@Override
		public String getText() {
			return _text;
		}
	}

	interface OutputString {
		String getText();
	}

	void add(CB_Action aCBAction);

	void add(CB_Process aProcess);

	void inst(ILazyCompilerInstructions aLazyCompilerInstructions);

	void option(CompilationChange aChange);

	void run_all();
}
