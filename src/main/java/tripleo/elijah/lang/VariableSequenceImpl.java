/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah.lang;

import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang2.ElElementVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VariableSequenceImpl implements VariableSequence {

	final     List<VariableStatement> stmts;
	@Nullable List<AnnotationClause>  annotations = null;
	private   Context                 _ctx;

	private OS_Element     parent;
	private AccessNotation access_note;

	private TypeModifiers   def;
	private El_CategoryImpl category;

	public VariableStatement next() {
		final VariableStatement st = new VariableStatement(this);
		st.set(def);
		stmts.add(st);
		return st;
	}

	public Collection<VariableStatement> items() {
		return stmts;
	}

	@Deprecated
	public VariableSequenceImpl() {
		stmts = new ArrayList<VariableStatement>();
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

	@Override
	public String toString() {
		final List<String> r = new ArrayList<String>();
		for (final VariableStatement stmt : stmts) {
			r.add(stmt.getName());
		}
		return r.toString();
//		return (stmts.stream().map(n -> n.getName()).collect(Collectors.toList())).toString();
	}

	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitVariableSequence(this);
	}

	public void addAnnotation(final AnnotationClause a) {
		if (annotations == null)
			annotations = new ArrayList<AnnotationClause>();
		annotations.add(a);
	}

	// region ClassItem

	public VariableSequenceImpl(final Context aContext) {
		stmts = new ArrayList<VariableStatement>();
		_ctx  = aContext;
	}

	public void defaultModifiers(final TypeModifiers aModifiers) {
		def = aModifiers;
	}

	@Override
	public void setCategory(final El_CategoryImpl aCategory) {
		category = aCategory;
	}

	@Override
	public void setAccess(final AccessNotation aNotation) {
		access_note = aNotation;
	}

	@Override
	public El_CategoryImpl getCategory() {
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

	@Override
	public void setCategory(final El_Category aCategory) {
		// TODO Auto-generated method stub

	}

}

//
//
//
