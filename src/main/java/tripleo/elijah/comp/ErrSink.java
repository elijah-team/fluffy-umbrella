/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah.comp;

import tripleo.elijah.diagnostic.Diagnostic;

public interface ErrSink {
	
	void exception(Exception exception);

    /*@ ensures errorCount() == \old errorCount + 1*/
    void reportError(String s);

    void reportWarning(String s);

    int errorCount();

    void info(String format);

	void reportDiagnostic(Diagnostic diagnostic);

	enum Errors {
        ERROR, WARNING, INFO
    }
}

//
//
//
