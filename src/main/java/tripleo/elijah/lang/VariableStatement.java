/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.diagnostic.Locatable;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.stages.deduce.DeduceTypeWatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Referenced classes of package pak:
//			TypeRef, IExpression

public class VariableStatement implements OS_Element, @NotNull Locatable {

	private final VariableSequence  _parent;
	public        DeduceTypeWatcher dtw;

	private TypeName typeName = new VariableTypeName();
	private IExpression initialValue = IExpression.UNASSIGNED;
	private IdentExpression name;
	private TypeModifiers typeModifiers;

	public VariableStatement(final VariableSequence aSequence) {
		_parent = aSequence;
	}

	public String getName() {
		return name.getText();
	}

	public IdentExpression getNameToken() {
		return name;
	}

	public void setName(final IdentExpression s) {
		name = s;
	}

	public void initial(@NotNull final IExpression aExpr) {
		initialValue=aExpr;
	}

	public void set(final TypeModifiers y) {
		typeModifiers = y;
	}

	public TypeModifiers getTypeModifiers() {
		return typeModifiers;
	}

	@NotNull public TypeName typeName() {
		return typeName;
	}

	public void setTypeName(@NotNull final TypeName tn) {
		typeName = tn;
	}

	@NotNull public IExpression initialValue() {
		return initialValue;
	}

	@Nullable List<AnnotationClause> annotations = null;

	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitVariableStatement(this);
	}

	@Override
	public Context getContext() {
		return getParent().getContext();
	}

	// region annotations

	@Override
	public OS_Element getParent() {
		return _parent;
	}

	public void addAnnotation(final AnnotationClause a) {
		if (annotations == null)
			annotations = new ArrayList<AnnotationClause>();
		annotations.add(a);
	}

	public void walkAnnotations(final AnnotationWalker annotationWalker) {
		if (_parent.annotations != null) {
			for (final AnnotationClause annotationClause : _parent.annotations) {
				for (final AnnotationPart annotationPart : annotationClause.aps) {
					annotationWalker.annotation(annotationPart);
				}
			}
		}
		if (annotations == null) return;
		for (final AnnotationClause annotationClause : annotations) {
			for (final AnnotationPart annotationPart : annotationClause.aps) {
				annotationWalker.annotation(annotationPart);
			}
		}
	}

	// endregion

	// region Locatable

	@Override
	public int getLine() {
		// TODO what about annotations
		return name.getLine();
	}

	@Override
	public int getColumn() {
		// TODO what about annotations
		return name.getColumn();
	}

	@Override
	public int getLineEnd() {
		// TODO what about initialValue
		return name.getLineEnd();
	}

	@Override
	public int getColumnEnd() {
		// TODO what about initialValue
		return name.getColumnEnd();
	}

	@Override
	public File getFile() {
		return name.getFile();
	}

	// endregion
}

//
//
//
