package tripleo.elijah.stages.expand;

import tripleo.elijah.lang.IExpression;

import java.util.List;

public class IntroducedFunction implements FunctionPrelimInstruction {
    private final IExpression base;
    private String funName;
    private List<IExpression> args;

    public IntroducedFunction(final IExpression expression) {
        this.base = expression;
    }

    @Override
    public int instructionNumber() {
        return _inst;
    }
    private int _inst;
    @Override
    public void setInstructionNumber(final int i) {_inst = i;}

    public void setName(final String s) {
        funName = s;
    }

    public void setArgs(final List<IExpression> list) {
        this.args = list;
    }
}
