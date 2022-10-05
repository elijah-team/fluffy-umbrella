/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.contexts;

import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.DefFunctionDef;
import tripleo.elijah.lang.LookupResultList;

import java.util.List;

/**
 * @author Tripleo
 *
 * Created 	Mar 26, 2020 at 9:24:44 PM
 */
public class DefFunctionContext extends Context {

	private final DefFunctionDef carrier;

	public DefFunctionContext(final DefFunctionDef functionDef) {
		carrier = functionDef;
	}

	/**
	 * By definition should have nothing to lookup
	 * 
	 * @param name
	 * @param level
	 * @param alreadySearched
	 * @return
	 */
	@Override public LookupResultList lookup(final String name, final int level, final LookupResultList Result, final List<Context> alreadySearched, final boolean one) {
//		final LookupResultList Result = new LookupResultList();
		alreadySearched.add(carrier.getContext());
		return getParent().lookup(name, level, Result, alreadySearched, one);
	}

	@Override
	public Context getParent() {
		return null;
	}

}
