package tripleo.elijah.comp;

import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.internal.CB_Output;
import tripleo.elijah.comp.internal.CR_State;
import tripleo.elijah.nextgen.query.Mode;

////	class CR_FindStdlib implements CR_Action {
////
////		private String prelude_name;
////
////		CR_FindStdlib(final String aPreludeName) {
////			prelude_name = aPreludeName;
////		}
////
////		@Override
////		public void attach(final CompilationRunner cr) {
////
////		}
////
////		@Override
////		public void execute(final CR_State st) {
////			@NotNull final Operation<CompilerInstructions> op = findStdLib(prelude_name, compilation);
////			assert op.mode() == Mode.SUCCESS; // TODO .NOTHING??
////		}
////	}
//
class CR_FindStdlibAction implements CR_Action {

	private final CompilationRunner compilationRunner;

	public CR_FindStdlibAction(final CompilationRunner aCompilationRunner) {
		compilationRunner = aCompilationRunner;
	}

	@Override
	public void attach(final CompilationRunner cr) {

	}

	@Override
	public void execute(final CR_State st, final CB_Output aO) {
		final Operation<CompilerInstructions> x = compilationRunner.findStdLib(Compilation.CompilationAlways.defaultPrelude(), compilationRunner.compilation);
		if (x.mode() == Mode.FAILURE) {
			compilationRunner.compilation.getErrSink().exception(x.failure());
			return;
		}
		aO.logProgress(130, "GEN_LANG: " + x.success().genLang());
	}

	@Override
	public String name() {
		return "find stdlib";
	}
}
