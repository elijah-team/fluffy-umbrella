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
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created 6/27/21 6:42 AM
 */
public abstract class BaseFunctionDef implements Documentable, ClassItem, OS_Container, OS_Element2 {

	public enum Species {
		REG_FUN,
		DEF_FUN,
		CTOR, DTOR,
		PROP_SET, PROP_GET,
		FUNC_EXPR
	}
	public final Attached _a = new Attached();
	protected    Species  _species;
	protected Scope3          scope3;
	protected FormalArgList   mFal = new FormalArgList(); // remove final for FunctionDefBuilder
	List<AnnotationClause> annotations = null;
	private   IdentExpression funName;
	private   AccessNotation  access_note;

	// region arglist

	private   El_Category     category;

	@Override // OS_Container
	public void add(final OS_Element anElement) {
		if (anElement instanceof FunctionItem) {
//			mScope2.add((StatementItem) anElement);
			scope3.add(anElement);
		} else
			throw new IllegalStateException(String.format("Cant add %s to FunctionDef", anElement));
	}

	public void addAnnotation(final AnnotationClause a) {
		if (annotations == null)
			annotations = new ArrayList<AnnotationClause>();
		annotations.add(a);
	}

	// endregion

	@Override  // Documentable
	public void addDocString(final Token aText) {
		scope3.addDocString(aText);
	}

	public @NotNull Iterable<AnnotationPart> annotationIterable() {
		final List<AnnotationPart> aps = new ArrayList<AnnotationPart>();
		if (annotations == null) return aps;
		for (final AnnotationClause annotationClause : annotations) {
			for (final AnnotationPart annotationPart : annotationClause.aps) {
				aps.add(annotationPart);
			}
		}
		return aps;
	}

	// region items

	public FormalArgList fal() {
		return mFal;
	}

	@Override
	public AccessNotation getAccess() {
		return access_note;
	}

	public Collection<FormalArgListItem> getArgs() {
		return mFal.items();
	}

	@Override
	public El_Category getCategory() {
		return category;
	}

	// endregion

	// region name

	@Override // OS_Element
	public Context getContext() {
		return _a._context;
	}

	public @NotNull List<FunctionItem> getItems() {
		final List<FunctionItem> collection = new ArrayList<FunctionItem>();
		for (final OS_Element element : scope3.items()) {
			if (element instanceof FunctionItem)
				collection.add((FunctionItem) element);
		}
		return collection;
		//return mScope2.items;
	}

	public OS_Module getModule() {
		var m = getParent();
		while (m != null && !(m instanceof OS_Module)) {
			m = m.getParent();
		}
		return (OS_Module) m;
	}

	// endregion

	// region context

	public IdentExpression getNameNode() {
		return funName;
	}

	@Override // OS_Element
	public abstract OS_Element getParent();

	// endregion

	// region annotations

	public Species getSpecies() {
		return _species;
	}

	public boolean hasItem(final OS_Element element) {
		return scope3.items().contains(element);
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

	@Override
	@NotNull // OS_Element2
	public String name() {
		if (funName == null)
			return "";
		return funName.getText();
	}

	// endregion

	// region Documentable

	public abstract void postConstruct();

	// endregion

	public TypeName returnType() {
		throw new NotImplementedException();
	}

	public void scope(final Scope3 sco) {
		scope3 = sco;
	}

	// region ClassItem

	@Override
	public void setAccess(final AccessNotation aNotation) {
		access_note = aNotation;
	}

	@Override
	public void setCategory(final El_Category aCategory) {
		category = aCategory;
	}

	public void setContext(final FunctionContext ctx) {
		_a.setContext(ctx);
	}

	public void setFal(final FormalArgList fal) {
		mFal = fal;
	}

	public void setName(final @NotNull IdentExpression aText) {
//		_name = EN_Name.create(funName.getText());

		funName = aText;
	}

	public void setSpecies(final Species aSpecies) {
		_species = aSpecies;
	}

	public void walkAnnotations(final AnnotationWalker annotationWalker) {
		if (annotations == null) return;
		for (final AnnotationClause annotationClause : annotations) {
			for (final AnnotationPart annotationPart : annotationClause.aps) {
				annotationWalker.annotation(annotationPart);
			}
		}
	}

	// endregion

}

//
//
//
