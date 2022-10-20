/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */

package tripleo.elijah.diagnostic;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.List;

/**
 * Created 12/26/20 5:31 AM
 */
public interface Diagnostic {
	String code();
	Severity severity();
	@NotNull
	Locatable primary();
	@NotNull
	List<Locatable> secondary();

	void report(PrintStream stream);

	public enum Severity {
		INFO, LINT, WARN, ERROR
	}
}

//
//
//
