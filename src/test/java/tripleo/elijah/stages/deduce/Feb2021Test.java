/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.deduce;

import org.junit.jupiter.api.Test;
import tripleo.elijah.TestCompilation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created 9/9/21 4:16 AM
 */
public class Feb2021Test {
	@Test
	public void testProperty2() throws Exception {
		final var t = TestCompilation.simpleTest()
		                             .setFile("test/feb2021/property2/")
		                             .run();

		final int curious_that_this_does_not_fail = 0/*100*/;
		assertEquals(curious_that_this_does_not_fail, t.errorCount());

		//assert t.c().reports().codeOutputSize() > 0;
		if (t.c().reports().codeOutputSize() < 1) {
			//		throw new AcceptedFailure();
		}
	}

	@Test
	public void testProperty3() throws Exception {
		final var t = TestCompilation.simpleTest()
		                             .setFile("test/feb2021/property3/")
		                             .run();

		final int curious_that_this_does_not_fail = 0/*100*/;
		assertEquals(curious_that_this_does_not_fail, t.errorCount());

		//assert t.c().reports().codeOutputSize() > 0;
		if (t.c().reports().codeOutputSize() < 1) {
			//		throw new AcceptedFailure();
		}
	}

	@Test
	public void testProperty() throws Exception {
		final var t = TestCompilation.simpleTest()
		                             .setFile("test/feb2021/property/")
		                             .run();

		final int curious_that_this_does_not_fail = 0/*100*/;
		assertEquals(curious_that_this_does_not_fail, t.errorCount());

		if (t.c().reports().codeOutputSize() < 1) {
					throw new AcceptedFailure();
		}


//		final Compilation c = new CompilationImpl(new StdErrSink(), new IO());
//
//		c.feedCmdLine(List_of("test/feb2021/property/"));
//
//		assertEquals(0, c.errorCount());

		/* TODO investigate: */assertTrue(t.assertLiveClass("Bar"));  // .inFile("test/feb2021/hier/hier.elijah")
		/* TODO investigate: */assertTrue(t.assertLiveClass("Foo"));  // ...
		/* TODO investigate: */assertTrue(t.assertLiveClass("Main")); // ...
	}

	@Test
	public void testFunction() throws Exception {
		final String s = "test/feb2021/function/";
		final var t = TestCompilation.simpleTest()
		                             .setFile(s)
		                             .run();

		final int curious_that_this_does_not_fail = 0/*100*/;
		assertEquals(curious_that_this_does_not_fail, t.errorCount());

		assertEquals(2, t.c().errorCount());

		/* TODO investigate: */assertTrue(t.assertLiveClass("Bar"));  // .inFile("test/feb2021/hier/hier.elijah")
		/* TODO investigate: */assertTrue(t.assertLiveClass("Foo"));  // ...
		/* TODO investigate: */assertTrue(t.assertLiveClass("Main")); // ...
	}

	@Test
	public void testHier() throws Exception {
		final String s = "test/feb2021/hier/";
		final var t = TestCompilation.simpleTest()
		                             .setFile(s)
		                             .run();

		//assert t.c().reports().codeOutputSize() > 0;
		if (t.c().reports().codeOutputSize() < 1) {
			//		throw new AcceptedFailure();
		}
		// TODO 10/15 cucumber??

		/* TODO investigate: */assertTrue(t.assertLiveClass("Bar"));  // .inFile("test/feb2021/hier/hier.elijah")
		/* TODO investigate: */assertTrue(t.assertLiveClass("Foo"));  // ...
		/* TODO investigate: */assertTrue(t.assertLiveClass("Main")); // ...

		final int curious_that_this_does_not_fail = 0/*100*/;
		assertEquals(curious_that_this_does_not_fail, t.errorCount());
	}
}

//
//
//
