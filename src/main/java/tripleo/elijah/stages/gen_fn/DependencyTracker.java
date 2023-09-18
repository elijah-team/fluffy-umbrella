/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */

package tripleo.elijah.stages.gen_fn;

import java.util.List;

import tripleo.elijah.stages.deduce.FunctionInvocation;

/**
 * Created 6/22/21 3:40 AM
 */
public interface DependencyTracker {
	List<FunctionInvocation> dependentFunctions();

	// for use in DeduceTypes2
	List<GenType> dependentTypes();
}

//
//
//
