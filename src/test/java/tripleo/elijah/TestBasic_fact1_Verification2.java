package tripleo.elijah;

import org.junit.*;
import tripleo.elijah.comp.*;
import tripleo.elijah.comp.internal.*;
import tripleo.elijah.util.*;

import static org.junit.Assert.*;

@SuppressWarnings("NewClassNamingConvention")
public class TestBasic_fact1_Verification2 {
	private Compilation c;

	@Before
	public void setUp() {
		final String s = "test/basic/fact1/main2";
		c = new CompilationImpl(new StdErrSink(), new IO());
		c.reports().turnAllOutputOff();
		c.feedCmdLine(Helpers.List_of(s, "-sO"));
	}

	@Test
	public void testInputs_fact1() {
		assertTrue(c.reports().containsInput("test/basic/fact1/fact1.elijah"));
	}

	@Test
	public void testInputs_main2_elijah() {
		assertTrue(c.reports().containsInput("test/basic/fact1/main2/main2.elijah"));
	}

	@Test
	public void testInputs_main2_ez() {
//        assertTrue(c.reports().containsInput("test/basic/fact1/main2/main2.ez"))
	}

	@Test
	public void testOutputs_main2_Main_h() {
		assertTrue(c.reports().containsCodeOutput("/main2/Main.h"));
	}

	@Test
	public void testOutputs_code2_main2_Main_c() {
		assertTrue(c.reports().containsCodeOutput("/main2/Main.c"));
	}

    /*
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/sww/modules-sw-writer
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/inputs.txt
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/buffers.txt
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/Makefile
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/logs/(DEDUCE_PHASE)
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/logs/lib_elijjah~~lib-c~~Prelude.elijjah
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/logs/test~~basic~~fact1~~fact1.elijah
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/logs/test~~basic~~fact1~~main2~~main2.elijah
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/logs/test~~basic~~fact1~~main2~~main2.elijah
401b Writing path: COMP/2408d3e32dc3f2d0d6254141917fa7629c71352a506a0edd17a007d1e3baa781/<date>/logs/test~~basic~~fact1~~main2~~main2.elijah

*/
    /*
import wprust.demo.fact

class Main < Arguments {
main() {
var b1 = 3
val a1 = argument(1)

if a1.isInt() {
b1 = a1.to_int() // intValue()
}

val f1 = factorial(b1)
println(f1)
}
}
*/
	/**
	 * **************************************************************************  */
    /*
package wprust.demo.fact

class Main11 {
main() {
const kl = 3
val f1 = factorial(kl)
println(f1)
}
}

//#pragma return_result
namespace / *__MODULE__* / {
		factorial_r(i: u64) -> u64 {
			case i {
				0 { Result = 0 }
				n { Result = n * factorial(n-1) } // _r/_i // also tailcall (not a tailcall, but thanks for the reminder)
			}
		}
		factorial_i(i: u64) -> u64 {
			var acc = 1
			iterate from 2 to i with num {
				acc *= num
			}
			Result = acc
		}

		alias factorial = factorial_r
	}

	*/
    /*
	test/basic/fact1/

	test/basic/fact1/main2


*/
}
