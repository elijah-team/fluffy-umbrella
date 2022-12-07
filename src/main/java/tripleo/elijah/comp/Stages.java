package tripleo.elijah.comp;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.util.NotImplementedException;

enum Stages {
	E("E") {
		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			NotImplementedException.raise();
		}

		@Override
		public RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
			return new EmptyProcess(aCa, aPr);
		}

		@Override
		public void runBackBetter(RuntimeProcesses aRt, ProcessRecord aPr) {
			// do nothing. job over.
		}
	},
	D("D") {
		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			aCompilationAccess.writeLogs();
		}

		@Override
		public RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
			return new DStageProcess(aCa, aPr);
		}

		@Override
		public void runBackBetter(RuntimeProcesses rt, ProcessRecord pr) {
			// FIXME make sure we only deduce here, and not generate or write output
			rt.prepare();
			rt.run();
			rt.postProcess(pr);
		}
	},
	S("S") {
		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			aCompilationAccess.writeLogs();
		}

		@Override
		public RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
			throw new NotImplementedException();
		}

		@Override
		public void runBackBetter(RuntimeProcesses rt, ProcessRecord pr) {
			// FIXME wtf is this??
			rt.prepare();
			rt.run();
			rt.postProcess(pr);
		}
	}, // ??
	O("O") {
		@Override
		public void writeLogs(final ICompilationAccess aCompilationAccess) {
			aCompilationAccess.writeLogs();
		}

		@Override
		public RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr) {
			return new OStageProcess(aCa, aPr);
		}

		@Override
		public void runBackBetter(RuntimeProcesses rt, ProcessRecord pr) {
			// everything. hold the mayo. extra mayo.
			rt.prepare();
			rt.run();
			rt.postProcess(pr);
		}
	}  // Output
	;

	private final String s;

	@Contract(pure = true)
	Stages(final String aO) {
		s = aO;
	}

	public abstract void writeLogs(final ICompilationAccess aCompilationAccess);

	public abstract RuntimeProcess getProcess(final ICompilationAccess aCa, final ProcessRecord aPr);

	public abstract void runBackBetter(RuntimeProcesses aRt, ProcessRecord aPr);
}
