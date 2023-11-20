/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
/**
 * Created Mar 25, 2019 at 3:00:39 PM
 *
 */
package tripleo.elijah.comp;

import tripleo.elijah.diagnostic.Diagnostic;

/**
 * @author tripleo(sb)
 *
 */
public class StdErrSink implements ErrSink {

	private int _errorCount;

	@Override
	public void exception(final Exception e) {
		_errorCount++;
		tripleo.elijah.util.Stupidity.println_err2("exception: " + e);
		e.printStackTrace(System.err);
	}

	@Override
	public void reportError(final String s) {
		_errorCount++;
		if (Compilation.CompilationAlways.VOODOO) System.err.printf("ERROR: %s%n", s);
	}

	@Override
	public void reportWarning(final String s) {
		if (Compilation.CompilationAlways.VOODOO) System.err.printf("WARNING: %s%n", s);
	}

	@Override
	public int errorCount() {
		return _errorCount;
	}

	@Override
	public void info(final String message) {
		if (Compilation.CompilationAlways.VOODOO) System.err.printf("INFO: %s%n", message);
	}

	@Override
	public void reportDiagnostic(final Diagnostic diagnostic) {
		if (diagnostic.severity() == Diagnostic.Severity.ERROR)
			_errorCount++;
		if (Compilation.CompilationAlways.VOODOO)
			diagnostic.report(System.err);
	}
}

//
//
//
