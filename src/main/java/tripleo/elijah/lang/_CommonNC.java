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

import java.util.ArrayList;
import java.util.List;

/**
 * Created 3/29/21 5:11 PM
 */
abstract class _CommonNC {
	public final    Attached        _a    = new Attached();
	protected final List<ClassItem> items = new ArrayList<ClassItem>();
	private final   List<String>    mDocs = new ArrayList<String>();
	private final List<AccessNotation> accesses = new ArrayList<AccessNotation>();
	protected       IdentExpression nameToken;
	protected       OS_Package      _packageName;
	List<AnnotationClause> annotations = null;
	// region ClassItem
	private AccessNotation access_note;
	private El_Category    category;

	public OS_Package getPackageName() {
		return _packageName;
	}

	public void setPackageName(final OS_Package aPackageName) {
		_packageName = aPackageName;
	}

	public void addDocString(final Token aText) {
		mDocs.add(aText.getText());
	}

	// OS_Container
	public List<OS_Element2> items() {
		final ArrayList<OS_Element2> a = new ArrayList<OS_Element2>();
		for (final ClassItem functionItem : getItems()) {
			final boolean b = functionItem instanceof OS_Element2;
			if (b) a.add((OS_Element2) functionItem);
		}
		return a;
	}

	public List<ClassItem> getItems() {
		return items;
	}

	// OS_Element2
	public String name() {
		return getName();
	}

	public String getName() {
		if (nameToken == null) return "";
		return nameToken.getText();
	}

	public void setName(final IdentExpression i1) {
		nameToken = i1;
	}

	public void addAccess(final AccessNotation acs) {
		accesses.add(acs);
	}

	public void walkAnnotations(final AnnotationWalker annotationWalker) {
		if (annotations == null) return;
		for (final AnnotationClause annotationClause : annotations) {
			for (final AnnotationPart annotationPart : annotationClause.aps) {
				annotationWalker.annotation(annotationPart);
			}
		}
	}

	public boolean hasItem(final OS_Element element) {
		if (!(element instanceof ClassItem)) return false;
		return items.contains(element);
	}

	public void addAnnotations(final List<AnnotationClause> as) {
		if (as == null) return;
		for (final AnnotationClause annotationClause : as) {
			addAnnotation(annotationClause);
		}
	}

	public void addAnnotation(final AnnotationClause a) {
		if (annotations == null)
			annotations = new ArrayList<AnnotationClause>();
		annotations.add(a);
	}

	public El_Category getCategory() {
		return category;
	}

	public void setCategory(final El_Category aCategory) {
		category = aCategory;
	}

	public AccessNotation getAccess() {
		return access_note;
	}

	public void setAccess(final AccessNotation aNotation) {
		access_note = aNotation;
	}

	// endregion
}

//
//
//
