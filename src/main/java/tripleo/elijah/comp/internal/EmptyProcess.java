package tripleo.elijah.comp.internal;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ICompilationAccess;
import tripleo.elijah.comp.internal.ProcessRecord;
import tripleo.elijah.comp.i.RuntimeProcess;

public final class EmptyProcess implements RuntimeProcess {
	public EmptyProcess(final ICompilationAccess aCompilationAccess, final ProcessRecord aPr) {
	}

	@Override
	public void run(final Compilation aComp) {
	}

	@Override
	public void postProcess() {
	}

	@Override
	public void prepare() {
	}
}
