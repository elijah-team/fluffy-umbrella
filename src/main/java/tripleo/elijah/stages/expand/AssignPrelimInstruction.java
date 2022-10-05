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

public class AssignPrelimInstruction implements FunctionPrelimInstruction {
    private final FunctionPrelimInstruction var;
    private final IExpression /*FunctionPrelimInstruction*/ expr;

    public AssignPrelimInstruction(final FunctionPrelimInstruction fi, /*FunctionPrelimInstruction*/final IExpression fi2) {
        this.var = fi;
        this.expr = fi2;
    }

    @Override public void setInstructionNumber(final int i) {_inst = i;}
    @Override public int instructionNumber() {
        return _inst;
    }
    private int _inst;
}

//
//
//
