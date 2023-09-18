/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.contexts.ModuleContext;

import java.util.ArrayList;
import java.util.List;

// TODO is this right, or should be interface??
public abstract class Context {

//	private OS_Container attached;

	public static class SearchList {
		@NotNull List<Context> alreadySearched = new ArrayList<>();

		public void add(Context c) {
			alreadySearched.add(c);
		}

		public boolean contains(Context context) {
			return alreadySearched.contains(context);
		}

		public @NotNull ImmutableList<Context> getList() {
			return ImmutableList.copyOf(alreadySearched);
		}
	}

//	public Context(OS_Container attached) {
//		this.attached = attached;
//	}

	public Context() {
	}

	public @NotNull Compilation compilation() {
		final OS_Module module = module();
		return module.parent;
	}

	public abstract @Nullable Context getParent();

	public LookupResultList lookup(@NotNull final String name) {
		final LookupResultList Result = new LookupResultList();
		return lookup(name, 0, Result, new ArrayList<Context>(), false);
	}

	public abstract LookupResultList lookup(String name, int level, LookupResultList Result, List<Context> alreadySearched, boolean one);

//	public abstract @Nullable LookupResultList lookup(String name, int level, LookupResultList Result, SearchList alreadySearched, boolean one);

	public @Nullable LookupResultList lookup(final String name, final int level, final LookupResultList Result, final SearchList alreadySearched, final boolean one) {
		return lookup(name, level, Result, new ArrayList<>(), one);
	}

	public @NotNull OS_Module module() {
		Context ctx = this;//getParent();
		while (!(ctx instanceof ModuleContext)) {
			ctx = ctx.getParent();
		}
		return ((ModuleContext) ctx).getCarrier();
	}
}

//
//
//
