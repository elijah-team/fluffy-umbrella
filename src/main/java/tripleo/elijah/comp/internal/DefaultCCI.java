package tripleo.elijah.comp.internal;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.i.CCI;
import tripleo.elijah.comp.i.ILazyCompilerInstructions;
import tripleo.elijah.comp.i.IProgressSink;
import tripleo.elijah.comp.i.ProgressSinkComponent;
import tripleo.elijah.util.Maybe;

public class DefaultCCI implements CCI {
	//private final @NotNull Compilation compilation;
	private final Compilation.CIS _cis;
	private final IProgressSink   _ps;

	@Contract(pure = true)
	public DefaultCCI(final @NotNull Compilation aCompilation, final Compilation.CIS aCis, final IProgressSink aProgressSink) {
		//compilation = aCompilation;
		_cis = aCis;
		_ps  = aProgressSink;
	}

	@Override
	public void accept(final @NotNull Maybe<ILazyCompilerInstructions> mcci, final IProgressSink aPs) {
		if (mcci.isException()) return;

		final ILazyCompilerInstructions cci = mcci.o;
		final CompilerInstructions      ci  = cci.get();

		assert _ps != aPs;

		_ps.note(131, ProgressSinkComponent.CCI, -1, new Object[]{ci.getName()});
		aPs.note(131, ProgressSinkComponent.CCI, -1, new Object[]{ci.getName()});

		IProgressSink t = null;
		try {
			t       = _cis.ps;
			_cis.ps = aPs;
			_cis.onNext(ci); //CIO::l.add(aCompilerInstructions);
		} finally {
			_cis.ps = t;
		}
		//compilation.pushItem(ci);
	}
}
