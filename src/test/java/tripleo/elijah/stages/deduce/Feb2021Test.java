/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import static org.junit.Assert.assertEquals;
import static tripleo.elijah.util.Helpers.List_of;

import org.junit.Test;

import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.comp.internal.CompilationImpl;

/**
 * Created 9/9/21 4:16 AM
 */
public class Feb2021Test {

	@Test
	public void testFunction() {
		final Compilation c = new CompilationImpl(new StdErrSink(), new IO());

		c.feedCmdLine(List_of("test/feb2021/function/"));

		assertEquals(2, c.errorCount());
	}

	@Test
	public void testHier() {
		final Compilation c = new CompilationImpl(new StdErrSink(), new IO());

		c.feedCmdLine(List_of("test/feb2021/hier/"));

		assertEquals(0, c.errorCount());
	}

	@Test
	public void testProperty() {
		final Compilation c = new CompilationImpl(new StdErrSink(), new IO());

		c.feedCmdLine(List_of("test/feb2021/property/"));

		assertEquals(0, c.errorCount());
	}
}

//
//
//
