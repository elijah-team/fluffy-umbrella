/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import antlr.Token;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.contexts.SyntacticBlockContext;
import tripleo.elijah.lang2.ElElementVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created 8/30/20 1:49 PM
 */
public class SyntacticBlock implements OS_Element, OS_Container, FunctionItem, StatementItem {

	private final List<FunctionItem>    _items = new ArrayList<FunctionItem>();
	private final OS_Element            _parent;
	private       SyntacticBlockContext ctx;
	private       Scope3                scope3;

	public SyntacticBlock(final OS_Element aParent) {
		_parent = aParent;
	}

	@Override
	public void visitGen(final @NotNull ElElementVisitor visit) {
		visit.visitSyntacticBlock(this);
	}

	@Override
	public OS_Element getParent() {
		return _parent;
	}

	@Override
	public Context getContext() {
		return ctx;
	}

	public List<FunctionItem> getItems() {
		final List<FunctionItem> collection = new ArrayList<FunctionItem>();
		for (final OS_Element element : scope3.items()) {
			if (element instanceof FunctionItem)
				collection.add((FunctionItem) element);
		}
		return collection;
		//return _items;
	}

	public void setContext(final SyntacticBlockContext ctx) {
		this.ctx = ctx;
	}

	public void postConstruct() {
	}

	@Override
	public List<OS_Element2> items() {
		final Collection<OS_Element> items = Collections2.filter(scope3.items(), new Predicate<OS_Element>() {
				@Override
				public boolean apply(@Nullable final OS_Element input) {
					return input instanceof OS_Element2;
				}
		});
		final Collection<OS_Element2> c = Collections2.transform(items, new Function<OS_Element, OS_Element2>() {
			@Nullable
			@Override
			public OS_Element2 apply(@Nullable final OS_Element input) {
				return (OS_Element2) input;
			}
		});
		return new ArrayList<OS_Element2>(c);
	}

	@Override
	public void add(final OS_Element anElement) {
		if (!(anElement instanceof FunctionItem))
			return; // TODO throw?
		scope3.add(anElement);
	}

	@Override
	public void addDocString(final Token s1) {
		scope3.addDocString(s1);
	}

	public void scope(final Scope3 sco) {
		scope3 = sco;
	}

}

//
//
//
