/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp.internal;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.comp.IO;
import tripleo.elijah.testing.comp.IFunctionMapHook;
import tripleo.elijah.util.NotImplementedException;

import java.util.List;

public class CompilationImpl extends Compilation {

	public CompilationImpl(ErrSink aEee, IO aIo) {
		super(aEee, aIo);
	}

	public void testMapHooks(List<IFunctionMapHook> aMapHooks) {
		throw new NotImplementedException();
	}

}

//
//
//
