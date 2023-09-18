package tripleo.elijah.comp;

import tripleo.elijah.comp.internal.CompilationBus;

import java.util.List;

public interface ICompilationBus {
	void option(CompilationChange aChange);

	void inst(ILazyCompilerInstructions aLazyCompilerInstructions);

	void add(CB_Action aCBAction);

	void add(CB_Process aProcess);

	void run_all();

	interface CB_Action {
		String name();

		void execute();

		OutputString[] outputStrings();

	}

	interface OutputString {
		String getText();
	}

	interface CB_Process {
		List<CB_Action> steps();

		default String name() {return getClass().getName();}

		default void execute(CompilationBus aCompilationBus) {
			for (final CB_Action action : steps()) {
				action.execute();
			}
		}
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
}
