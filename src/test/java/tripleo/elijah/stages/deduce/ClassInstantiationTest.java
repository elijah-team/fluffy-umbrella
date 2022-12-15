/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.StdErrSink;

import static tripleo.elijah.util.Helpers.List_of;

/**
 * Created 3/5/21 4:32 AM
 */
public class ClassInstantiationTest {

	@Test
	public void classInstantiation() {
		@NotNull String f = "test/basic1/class_instantiation/";
		@NotNull Compilation c = new Compilation(new StdErrSink(), new IO());

		c.feedCmdLine(List_of(f));
	}

	@Test
	public void classInstantiation2() {
		@NotNull String f = "test/basic1/class_instantiation2/";
		@NotNull Compilation c = new Compilation(new StdErrSink(), new IO());

		c.feedCmdLine(List_of(f));
	}

	@Test
	public void classInstantiation3() {
		@NotNull String f = "test/basic1/class_instantiation3/";
		@NotNull Compilation c = new Compilation(new StdErrSink(), new IO());

		c.feedCmdLine(List_of(f));
	}
}

//
//
//
