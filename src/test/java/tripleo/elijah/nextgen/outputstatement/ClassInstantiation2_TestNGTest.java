/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package tripleo.elijah.nextgen.outputstatement;

//import static org.testng.Assert.*;
//import org.testng.annotations.Test;

import org.junit.Test;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.impl.StdErrSink;
import tripleo.elijah.factory.comp.CompilationFactory;
import tripleo.elijah.nextgen.outputtree.EOT_OutputTree;

import static org.junit.Assert.assertNotNull;
import static tripleo.elijah.util.Helpers.List_of;

//@Test
public class ClassInstantiation2_TestNGTest {

	public ClassInstantiation2_TestNGTest() {
	}

	// TODO add test methods here.
	// The methods must be annotated with annotation @Test. For example:
	//
	// @Test
	// public void hello() {}

	@Test
	public void classInstantiation2() throws Exception {
		final String f = "test/basic1/class_instantiation2/";
		final Compilation c = CompilationFactory.mkCompilation(new StdErrSink(), new IO());

		c.feedCmdLine(List_of(f));

		final EOT_OutputTree ot = c.getOutputTree();

		assertNotNull(ot);
	}

/*
    @org.testng.annotations.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.testng.annotations.AfterClass
    public static void tearDownClass() throws Exception {
    }

    @org.testng.annotations.BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @org.testng.annotations.AfterMethod
    public void tearDownMethod() throws Exception {
    }
*/
}
