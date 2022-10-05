/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.contexts;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.*;
import tripleo.elijah.stages.expand.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tripleo
 *
 * Created 	Mar 26, 2020 at 6:13:58 AM
 */
public class FunctionContext extends Context {

	private final BaseFunctionDef carrier;
	private final Context _parent;
	public List<FunctionPrelimInstruction> functionPrelimInstructions = new ArrayList<FunctionPrelimInstruction>();
	private int functionPrelimInstructionsNumber = 1;

	public FunctionContext(final Context aParent, final BaseFunctionDef fd) {
		_parent = aParent;
		carrier = fd;
	}

	@Override public LookupResultList lookup(final String name, final int level, final LookupResultList Result, final List<Context> alreadySearched, final boolean one) {
		alreadySearched.add(carrier.getContext());
		for (final FunctionItem item: carrier.getItems()) {
			if (!(item instanceof ClassStatement) &&
				!(item instanceof NamespaceStatement) &&
				!(item instanceof FunctionDef) &&
				!(item instanceof VariableSequence)
			) continue;
			if (item instanceof OS_Element2) {
				if (((OS_Element2) item).name().equals(name)) {
					Result.add(name, level, (OS_Element) item, this);
				}
			} else if (item instanceof VariableSequence) {
//				System.out.println("[FunctionContext#lookup] VariableSequence "+item);
				for (final VariableStatement vs : ((VariableSequence) item).items()) {
					if (vs.getName().equals(name))
						Result.add(name, level, vs, this);
				}
			}
		}
		for (final FormalArgListItem arg : carrier.getArgs()) {
			if (arg.name().equals(name)) {
				Result.add(name, level, arg, this);
			}
		}
		if (carrier.getParent() != null) {
			final Context context = getParent()/*carrier.getParent().getContext()*/;
			if (!alreadySearched.contains(context) || !one)
				return context.lookup(name, level + 1, Result, alreadySearched, false);
		}
		return Result;
	}

	@Override
	public Context getParent() {
		return _parent;
	}

	public IntroducedVariable introduceVariable(final IExpression variable) {
//		System.out.println("[#introduceVariable] "+variable);
		final IntroducedVariable introducedVariable = new IntroducedVariable(variable);
		variableTable.add(introducedVariable);
		addPrelimInstruction(introducedVariable);
		return introducedVariable;
	}

	public IntroducedVariable introduceVariable(final VariableStatement variable) {
//		System.out.println("[#introduceVariable] "+variable);
		final IntroducedVariable introducedVariable = new IntroducedVariable(variable);
		variableTable.add(introducedVariable);
		addPrelimInstruction(introducedVariable);
		return introducedVariable;
	}

	List<IntroducedVariable> variableTable = new ArrayList<IntroducedVariable>();

	public DotExpressionInstruction dotExpression(final FunctionPrelimInstruction i, final IExpression de) {
		final DotExpressionInstruction dei = new DotExpressionInstruction(i, de);
		addPrelimInstruction(dei);
		return dei;
	}

	public FunctionCallPrelimInstruction makeProcCall(final FunctionPrelimInstruction fi, final ExpressionList args) {
		final IntroducedExpressionList els = simplifyExpressionList(args);
		final FunctionCallPrelimInstruction fci = new FunctionCallPrelimInstruction(fi, els);
		addPrelimInstruction(fci);
		return fci;
	}

	private IntroducedExpressionList simplifyExpressionList(final ExpressionList args) {
		final IntroducedExpressionList args1 = new IntroducedExpressionList();
		if (args == null) return args1;
		for (final IExpression arg : args) {
			final int y=2;
			if (arg.getKind() == ExpressionKind.PROCEDURE_CALL) {
				FunctionPrelimInstruction i = null; // TODO
				if (arg.getLeft().getKind() == ExpressionKind.IDENT) {
					i=introduceVariable(arg.getLeft());
					final ExpressionList args2 = ((ProcedureCallExpression) arg).getArgs();
					final IntroducedExpressionList expressionList = simplifyExpressionList(args2);
					((IntroducedVariable)i).makeIntoFunctionCall(expressionList);
					args1.add(i);
				}
				//makeProcCall(i, ((ProcedureCallExpression)arg).getArgs());
			} else if (arg.getKind() == ExpressionKind.FUNC_EXPR) {
				final int yy=2;
				final FunctionPrelimInstruction i = introduceVariable(arg);
				args1.add(i);
			}
		}
		return args1;
	}

//	public FunctionPrelimInstruction assign(FunctionPrelimInstruction fi, FunctionPrelimInstruction fi2) {
//		AssignPrelimInstruction api = new AssignPrelimInstruction(fi, fi2);
//		addPrelimInstruction(api);
//		return api;
//	}

	public FunctionPrelimInstruction assign(final FunctionPrelimInstruction fi, final IExpression fi2) {
		final AssignPrelimInstruction api = new AssignPrelimInstruction(fi, fi2);
		addPrelimInstruction(api);
		return api;
	}

	private void addPrelimInstruction(final @NotNull FunctionPrelimInstruction fpi) {
		functionPrelimInstructions.add(fpi);
		fpi.setInstructionNumber(functionPrelimInstructionsNumber++);
	}

	public FunctionPrelimInstruction introduceFunction(final IExpression expression) {
//		System.out.println("[#introduceFunction] "+expression);
		final IntroducedFunction introducedFunction = new IntroducedFunction(expression);
//		variableTable.add(introducedFunction);
		addPrelimInstruction(introducedFunction);
		return introducedFunction;
	}
}

//
//
//
