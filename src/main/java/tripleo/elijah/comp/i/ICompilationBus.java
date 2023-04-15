package tripleo.elijah.comp.i;

import java.util.List;

public interface ICompilationBus {
	void option(CompilationChange aChange);

	void inst(ILazyCompilerInstructions aLazyCompilerInstructions);

	void add(CB_Action aCBAction);

	void add(CB_Process aProcess);

	interface CB_Action {
		String name();

		void execute();

		List<OutputString> outputStrings();

	}

	interface OutputString {
		String getText();
	}

	interface CB_Process {
//		void execute();

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
}
