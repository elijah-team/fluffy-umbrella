/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
/**
 * 
 */
package tripleo.elijah.contexts;

import tripleo.elijah.lang.*;

import java.util.List;

/**
 * @author Tripleo
 *
 * Created 	Mar 29, 2020 at 8:59:42 PM
 */
public class NamespaceContext extends Context {

	private final Context _parent;
	public NamespaceStatement carrier;

//	public NamespaceContext(NamespaceStatement namespaceStatement) {
//		carrier = namespaceStatement;
//	}

	public NamespaceContext(final Context aParent, final NamespaceStatement ns) {
		_parent = aParent;
		carrier = ns;
	}

	@Override public LookupResultList lookup(final String name, final int level, final LookupResultList Result, final List<Context> alreadySearched, final boolean one) {
		alreadySearched.add(carrier.getContext());
		for (final ClassItem item: carrier.getItems()) {
			if (!(item instanceof ClassStatement) &&
				!(item instanceof NamespaceStatement) &&
				!(item instanceof VariableSequence) &&
				!(item instanceof AliasStatement) &&
				!(item instanceof FunctionDef) &&
				!(item instanceof PropertyStatement)
			) continue;
			if (item instanceof OS_Element2) {
				if (((OS_Element2) item).name().equals(name)) {
					Result.add(name, level, item, this);
				}
			}
			if (item instanceof VariableSequence) {
//				System.out.println("[NamespaceContext#lookup] VariableSequence "+item);
				for (final VariableStatement vs : ((VariableSequence) item).items()) {
					if (vs.getName().equals(name))
						Result.add(name, level, vs, this);
				}
			}
		}
		if (getParent() != null) {
			final Context context = getParent();
			if (!alreadySearched.contains(context) || !one)
				return context.lookup(name, level + 1, Result, alreadySearched, false);
		}
		return Result;

	}

	@Override public Context getParent() {
		return _parent;
	}
}
