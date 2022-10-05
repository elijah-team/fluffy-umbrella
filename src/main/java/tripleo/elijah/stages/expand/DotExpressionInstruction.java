/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.expand;

import tripleo.elijah.lang.IExpression;

public class DotExpressionInstruction implements FunctionPrelimInstruction {
    private final FunctionPrelimInstruction variable;
    final IExpression expr;

    public DotExpressionInstruction(final FunctionPrelimInstruction i, final IExpression de) {
        this.variable = i;
        this.expr     = de;
    }

    @Override
    public String toString() {
        return "DotExpressionInstruction{" +
                "variable=" + variable +
                ", dot_exp=" + expr +
                '}';
    }
    @Override
    public int instructionNumber() {
        return _inst;
    }
    private int _inst;
    @Override
    public void setInstructionNumber(final int i) {_inst = i;}
}

//
//
//
