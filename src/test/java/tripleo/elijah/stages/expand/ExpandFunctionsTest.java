package tripleo.elijah.stages.expand;

import org.junit.Assert;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.comp.IO;
import tripleo.elijah.comp.StdErrSink;
import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.lang.*;
import tripleo.elijah.util.Helpers;

import java.util.Collection;

public class ExpandFunctionsTest {

//    @Test
    public void expand() {
        final Compilation c = new Compilation(new StdErrSink(), new IO());
//        final String file_name = "test/basic/listfolders2.elijah";
        final String file_name = "test/basic/listfolders3/listfolders3.elijah";
        c.feedCmdLine(Helpers.List_of(file_name, "-sO"));
        final OS_Module mod = c.fileNameToModule(file_name);
        Assert.assertTrue(mod != null);
        final ClassStatement kl = mod.getClassByName("Main");
        final Collection<ClassItem> fdl = kl.findFunction("main");
        final FunctionDef fd = (FunctionDef) fdl.iterator().next();
        final FunctionContext fc = (FunctionContext) fd.getContext();
        final FunctionPrelimInstruction fi1 = fc.functionPrelimInstructions.get(0);
        assert fi1 instanceof IntroducedVariable;
        {
            final IntroducedVariable introducedVariable = (IntroducedVariable) fi1;
            Assert.assertTrue(introducedVariable.kind == IntroducedVariable.Type.PROCEDURE_CALL);
            Assert.assertTrue(((IdentExpression) introducedVariable.variable).getText().equals("MainLogic"));
            Assert.assertTrue(introducedVariable.args.expressions().size() == 0);
        }
        {
            final FunctionPrelimInstruction fi2 = fc.functionPrelimInstructions.get(1);
            assert fi2 instanceof DotExpressionInstruction;
            final IExpression expr = ((DotExpressionInstruction) fi2).expr;
            Assert.assertTrue(((IdentExpression)expr).getText().equals("main"));
        }
        {
            final FunctionPrelimInstruction fi3 = fc.functionPrelimInstructions.get(2);
//          assert fi3 instanceof IntroducedVariable;
//          Assert.assertTrue(((IntroducedVariable) fi3).kind == IntroducedVariable.Type.PROCEDURE_CALL);
        }
        {
            final FunctionPrelimInstruction fi4 = fc.functionPrelimInstructions.get(3);
//          assert fi4 instanceof IntroducedVariable;
//          Assert.assertTrue(((IntroducedVariable) fi4).kind == IntroducedVariable.Type.PROCEDURE_CALL);
        }
        {
            final FunctionPrelimInstruction fi5 = fc.functionPrelimInstructions.get(4);
//          assert fi5 instanceof IntroducedVariable;
//          Assert.assertTrue(((IntroducedVariable) fi5).kind == IntroducedVariable.Type.PROCEDURE_CALL);
        }
        {
            final FunctionPrelimInstruction fi6 = fc.functionPrelimInstructions.get(5);
            assert fi6 instanceof AssignPrelimInstruction;
            Assert.assertTrue(((AssignPrelimInstruction) fi6).instructionNumber() == 6);
        }
        final int y=2;
    }
}