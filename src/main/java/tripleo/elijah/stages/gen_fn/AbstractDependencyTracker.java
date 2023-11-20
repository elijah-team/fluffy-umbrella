/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.deduce.FunctionInvocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 6/21/21 11:36 PM
 */
public abstract class AbstractDependencyTracker implements DependencyTracker {
    private final List<FunctionInvocation> dependentFunctions = new ArrayList<FunctionInvocation>();
    private final List<GenType> dependentTypes = new ArrayList<GenType>();

    @Override
    public List<GenType> dependentTypes() {
        return dependentTypes;
    }

    @Override
    public List<FunctionInvocation> dependentFunctions() {
        return dependentFunctions;
    }

	public void addDependentType(@NotNull final GenType aType) {
		dependentTypes.add(aType);
	}

	public void addDependentFunction(@NotNull final FunctionInvocation aFunction) {
		dependentFunctions.add(aFunction);
	}
}

//
//
//
