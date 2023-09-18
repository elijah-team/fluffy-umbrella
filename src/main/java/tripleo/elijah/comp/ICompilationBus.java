package tripleo.elijah.comp;

import tripleo.elijah.comp.internal.CompilationBus;

import java.util.List;

public interface ICompilationBus {
	interface CB_Action {
		void execute();

		String name();

		OutputString[] outputStrings();

	}

	interface CB_Process {
		default void execute(CompilationBus aCompilationBus) {
			for (final CB_Action action : steps()) {
				action.execute();
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
