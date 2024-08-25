package tripleo.elijah

import org.junit.Test
import kotlin.test.assertTrue

//import gumtree.spoon.AstComparator
//import gumtree.spoon.builder.SpoonGumTreeBuilder
//import gumtree.spoon.diff.Diff
//import gumtree.spoon.diff.DiffImpl
//import org.junit.Before
//import org.junit.Test
//import spoon.SpoonModelBuilder
//import spoon.compiler.SpoonResource
//import spoon.reflect.CtModel
//import spoon.reflect.declaration.CtType
//import spoon.reflect.factory.Factory
//import spoon.support.compiler.VirtualFile
//import spoon.support.compiler.jdt.JDTBasedSpoonCompiler
//import spoon.testing.utils.ModelUtils.createFactory
//import tripleo.elijah_durable_congenial.comp.Finally
//import tripleo.elijah_durable_congenial.comp.i.Compilation
//import tripleo.elijah_durable_congenial.comp.signal.DeducePipeline_finishedSignal
//import tripleo.elijah_durable_congenial.factory.comp.CompilationFactory
//import tripleo.elijah_durable_congenial.util.Helpers
//import kotlin.test.assertEquals

@Suppress("PrivatePropertyName")
class TestBasic_fact1_Gumtree {
    //    private var REPORTS : Finally? = null
//    private lateinit var c: Compilation
//
//    @Before
//    fun setUp() {
//        val s = "test/basic/fact1/main2"
//        c = CompilationFactory.mkCompilationSilent()
//        c.feedCmdLine(Helpers.List_of(s, "-sO"))
//        this.REPORTS = c.reports()
//
//        assertEquals(true, c.getSignalResult(DeducePipeline_finishedSignal.INSTANCE))
//    }
//
    @Test
    fun dummy() {
        assertTrue(true)
    }

//    @Test
//    fun testInputs_fact1() {
//        val ac = AstComparator()
//        val scanner = SpoonGumTreeBuilder()
//
//        val compare: Diff = DiffImpl(
//            scanner.treeContext,
//            scanner.getTree(m(ac, "a")),
//            scanner.getTree(m(ac, "b"))
//        )
//
//        val ros = compare.rootOperations
//        for (ro in ros) {
//            System.err.println("9999-0053 "+ro)
//        }
//
//        assertEquals(listOf(),ros)
//    }
//
//    private fun m(
//        ac: AstComparator,
//        filename: String
//    ): CtType<*>? {
//        val content = String(TestBasic_fact1_Gumtree::class.java.getResourceAsStream(filename)!!.readAllBytes())
//        val resource = VirtualFile(content, filename)
//        return getCtType(resource)
//    }
//
//    fun getCtType(resource: SpoonResource?): CtType<*>? {
//        val factory: Factory = createFactory()
//        factory.model.setBuildModelIsFinished<CtModel>(false)
//        val compiler: SpoonModelBuilder = JDTBasedSpoonCompiler(factory)
//        compiler.factory.environment.setLevel("OFF")
//        compiler.addInputSource(resource)
//        compiler.build()
//        if (factory.Type().all.size == 0) {
//            return null
//        }
//
//        // let's first take the first type.
//        val type = factory.Type().all[0]
//        // Now, let's ask to the factory the type (which it will set up the
//        // corresponding
//        // package)
//        return factory.Type().get<Any>(type.qualifiedName)
//    }
}
