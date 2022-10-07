/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.impl.StdErrSink;
import tripleo.elijah.factory.comp.CompilationFactory;

public class Main {

	public static void main(final String[] args) throws Exception {
		final Compilation cc = CompilationFactory.mkCompilation(new StdErrSink(), new IO());

		cc.feedCmdLine(args);
	}
}

//
//
//
