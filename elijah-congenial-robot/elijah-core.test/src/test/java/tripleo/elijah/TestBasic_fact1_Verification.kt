package tripleo.elijah

import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import tripleo.elijah_durable_congenial.comp.Finally
import tripleo.elijah_durable_congenial.comp.i.Compilation
import tripleo.elijah_durable_congenial.comp.signal.DeducePipeline_finishedSignal
import tripleo.elijah_durable_congenial.factory.comp.CompilationFactory
import tripleo.elijah_durable_congenial.util.Helpers
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("PrivatePropertyName")
class TestBasicFact1VerificationTest {
    private var REPORTS : Finally? = null
    private lateinit var c: Compilation

    @Before
    fun setUp() {
        val s = "test/basic/fact1/main2"
        c = CompilationFactory.mkCompilationSilent()
        c.feedCmdLine(Helpers.List_of(s, "-sO"))
        this.REPORTS = c.reports()

        assertEquals(true, c.getSignalResult(DeducePipeline_finishedSignal.INSTANCE))
    }

    @Ignore
    @Test
    fun testInputs_fact1() {
        assertTrue(REPORTS!!.containsInput("test/basic/fact1/fact1.elijah"))
    }

    @Ignore
    @Test
    fun testInputs_main2_elijah() {
        assertTrue(REPORTS!!.containsInput("test/basic/fact1/main2/main2.elijah"))
    }

    @Ignore
    @Test
    fun testInputs_main2_ez() {
//        assertTrue(REPORTS.containsInput("test/basic/fact1/main2/main2.ez"))
    }

    @Ignore
    @Test
    fun testOutputs_main2_Main_h() {
        assertTrue(REPORTS!!.containsCodeOutput("/main2/Main.h"))
    }

    @Ignore
    @Test
    fun testOutputs_code2_main2_Main_c() {
        assertTrue(REPORTS!!.containsCodeOutput("/main2/Main.c"))
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
