/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.contexts;

import tripleo.elijah.lang.*;
import tripleo.elijah.stages.expand.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 8/21/20 11:53 PM
 */
public class FuncExprContext extends FunctionContext {

	private final FuncExpr carrier;
	private final Context _parent;
	public List<FunctionPrelimInstruction> functionPrelimInstructions = new ArrayList<FunctionPrelimInstruction>();
	private int functionPrelimInstructionsNumber = 1;

	public FuncExprContext(final Context cur, final FuncExpr pc) {
		super(cur, pc);
		_parent = cur;
		carrier = pc;
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

}

//
//
//
