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
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.contexts.FunctionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created 6/27/21 6:42 AM
 */
public abstract class BaseFunctionDef implements Documentable, ClassItem, OS_Container, OS_Element2 {

	public Attached _a = new Attached();
	protected Species _species;
	List<AnnotationClause> annotations = null;
	protected Scope3 scope3;
	protected FormalArgList mFal = new FormalArgList(); // remove final for FunctionDefBuilder
	private IdentExpression funName;
	private AccessNotation access_note;
	private El_Category category;

	// region arglist

	public FormalArgList fal() {
		return mFal;
	}

	public void setFal(FormalArgList fal) {
		mFal = fal;
	}

	public Collection<FormalArgListItem> getArgs() {
		return mFal.items();
	}

	// endregion

	public void scope(Scope3 sco) {
		scope3 = sco;
	}

	@Override // OS_Element
	public abstract OS_Element getParent();

	// region items

	public boolean hasItem(OS_Element element) {
		return scope3.items().contains(element);
	}

	public @NotNull List<FunctionItem> getItems() {
		List<FunctionItem> collection = new ArrayList<FunctionItem>();
		for (OS_Element element : scope3.items()) {
			if (element instanceof FunctionItem)
				collection.add((FunctionItem) element);
		}
		return collection;
		//return mScope2.items;
	}

	@Override // OS_Container
	public List<OS_Element2> items() {
		final ArrayList<OS_Element2> a = new ArrayList<OS_Element2>();
		for (final OS_Element functionItem : scope3.items()) {
			if (functionItem instanceof OS_Element2)
				a.add((OS_Element2) functionItem);
		}
		return a;
	}

	@Override // OS_Container
	public void add(final OS_Element anElement) {
		if (anElement instanceof FunctionItem) {
//			mScope2.add((StatementItem) anElement);
			scope3.add(anElement);
		} else
			throw new IllegalStateException(String.format("Cant add %s to FunctionDef", anElement));
	}

	// endregion

	// region name

	public IdentExpression getNameNode() {
		return funName;
	}

	@Override @NotNull // OS_Element2
	public String name() {
		if (funName == null)
			return "";
		return funName.getText();
	}

	public void setName(final @NotNull IdentExpression aText) {
		funName = aText;
	}

	// endregion

	// region context

	public void setContext(final FunctionContext ctx) {
		_a.setContext(ctx);
	}

	@Override // OS_Element
	public Context getContext() {
		return _a._context;
	}

	// endregion

	// region annotations

	public abstract void postConstruct();

	public void addAnnotation(final AnnotationClause a) {
		if (annotations == null)
			annotations = new ArrayList<AnnotationClause>();
		annotations.add(a);
	}

	public void walkAnnotations(AnnotationWalker annotationWalker) {
		if (annotations == null) return;
		for (AnnotationClause annotationClause : annotations) {
			for (AnnotationPart annotationPart : annotationClause.aps) {
				annotationWalker.annotation(annotationPart);
			}
		}
	}

	public Iterable<AnnotationPart> annotationIterable() {
		List<AnnotationPart> aps = new ArrayList<AnnotationPart>();
		if (annotations == null) return aps;
		for (AnnotationClause annotationClause : annotations) {
			for (AnnotationPart annotationPart : annotationClause.aps) {
				aps.add(annotationPart);
			}
		}
		return aps;
	}

	// endregion

	// region Documentable

	@Override  // Documentable
	public void addDocString(final Token aText) {
		scope3.addDocString(aText);
	}

	// endregion

	public Species getSpecies() {
		return _species;
	}

	public void setSpecies(final Species aSpecies) {
		_species = aSpecies;
	}

	// region ClassItem

	@Override
	public El_Category getCategory() {
		return category;
	}

	@Override
	public void setCategory(El_Category aCategory) {
		category = aCategory;
	}

	@Override
	public AccessNotation getAccess() {
		return access_note;
	}

	@Override
	public void setAccess(AccessNotation aNotation) {
		access_note = aNotation;
	}

	public enum Species {
		REG_FUN,
		DEF_FUN,
		CTOR, DTOR,
		PROP_SET, PROP_GET,
		FUNC_EXPR
	}

	// endregion

}

//
//
//
