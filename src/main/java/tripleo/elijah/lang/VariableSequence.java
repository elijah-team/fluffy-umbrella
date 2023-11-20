/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah.lang;

import tripleo.elijah.gen.ICodeGen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VariableSequence implements StatementItem, FunctionItem, ClassItem {

	private Context _ctx;
	private OS_Element parent;
	List<VariableStatement> stmts;

	@Deprecated public VariableSequence() {
		stmts = new ArrayList<VariableStatement>();
	}

	public VariableSequence(final Context aContext) {
		stmts = new ArrayList<VariableStatement>();
		_ctx = aContext;
	}

	private TypeModifiers def;

	public void defaultModifiers(final TypeModifiers aModifiers) {def=aModifiers;}

	public VariableStatement next() {
		final VariableStatement st = new VariableStatement(this);
		st.set(def);
		stmts.add(st);
		return st;
	}

	public Collection<VariableStatement> items() {
		return stmts;
	}

	@Override
	public void visitGen(final ICodeGen visit) {
		visit.visitVariableSequence(this);
	}

	@Override
	public OS_Element getParent() {
		return this.parent;
	}

	public void setParent(final OS_Element parent) {
		this.parent = parent;
	}

	@Override
	public Context getContext() {
		return _ctx;
	}

	public void setContext(final Context ctx) {
		_ctx = ctx;
	}

	@Override public String toString() {
		final List<String> r = new ArrayList<String>();
		for (final VariableStatement stmt : stmts) {
			r.add(stmt.getName());
		}
		return r.toString();
//		return (stmts.stream().map(n -> n.getName()).collect(Collectors.toList())).toString();
	}

	List<AnnotationClause> annotations = null;

	public void addAnnotation(final AnnotationClause a) {
		if (annotations == null)
			annotations = new ArrayList<AnnotationClause>();
		annotations.add(a);
	}

	// region ClassItem

	private AccessNotation access_note;
	private El_Category category;

	@Override
	public void setCategory(final El_Category aCategory) {
		category = aCategory;
	}

	@Override
	public void setAccess(final AccessNotation aNotation) {
		access_note = aNotation;
	}

	@Override
	public El_Category getCategory() {
		return category;
	}

	@Override
	public AccessNotation getAccess() {
		return access_note;
	}

	// endregion

	public void setTypeName(final TypeName aTypeName) {
		for (final VariableStatement vs : stmts) {
			vs.setTypeName(aTypeName);
		}
	}

}

//
//
//
