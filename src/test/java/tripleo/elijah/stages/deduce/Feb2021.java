/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.junit.Test;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.comp.internal.CompilationImpl;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 9/9/21 4:16 AM
 */
public class Feb2021 {

	@Test
	public void testProperty() throws Exception {
		final Compilation c = new CompilationImpl(new StdErrSink(), new IO());

		c.feedCmdLine(List_of("test/feb2021/property/"));
	}

	@Test
	public void testFunction() throws Exception {
		final Compilation c = new CompilationImpl(new StdErrSink(), new IO());

		c.feedCmdLine(List_of("test/feb2021/function/"));
	}

	@Test
	public void testHier() throws Exception {
		final Compilation c = new CompilationImpl(new StdErrSink(), new IO());

		c.feedCmdLine(List_of("test/feb2021/hier/"));
	}

}

//
//
//
