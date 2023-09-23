package tripleo.elijah.comp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.util.NotImplementedException;

public enum Stages {
	E("E") {
		@Override
		public RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr, final CompilationEnclosure aCe) {
			return new EmptyProcess(aCa, aPr);
		}

		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			NotImplementedException.raise();
		}
	},
	D("D") {
		@Override
		public @NotNull RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr, final CompilationEnclosure aCe) {
			return new DStageProcess(aCa, aPr);
		}

		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			aCompilationAccess.writeLogs();
		}
	},
	S("S") {
		@Override
		public RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr, final CompilationEnclosure aCe) {
			throw new NotImplementedException();
		}

		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			aCompilationAccess.writeLogs();
		}
	},  // ??
	O("O") {
		@Override
		public RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr, final CompilationEnclosure aCe) {
			return new OStageProcess(aCa, aPr, aCe);
		}

		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			aCompilationAccess.writeLogs();
		}
	}  // Output
	;

	private final String s;

	@Contract(pure = true)
	Stages(final String aO) {
		s = aO;
	}

	public abstract RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr, final CompilationEnclosure aCe);

	public abstract void writeLogs(final ICompilationAccess aCompilationAccess);
}
