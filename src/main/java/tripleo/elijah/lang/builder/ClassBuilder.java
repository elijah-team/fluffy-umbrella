/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import tripleo.elijah.lang.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/22/20 7:59 PM
 */
public class ClassBuilder {
	private List<AnnotationClause> annotations = new ArrayList<AnnotationClause>();
	private ClassTypes _type;
	private OS_Element _parent;
	private Context _parent_context;
	private IdentExpression _name;
	private final ClassScope _scope = new ClassScope();
	private final ClassInheritance _inh = new ClassInheritance();
	private TypeNameList genericPart;

	public void setType(ClassTypes classTypes) {
		_type = classTypes;
	}

	public ClassStatement build() {
		ClassStatement cs = new ClassStatement(_parent, _parent_context);
		cs.setType(_type);
		assert _name != null;
		cs.setName(_name);
		for (AnnotationClause annotation : annotations) {
			cs.addAnnotation(annotation);
		}
		if (genericPart != null)
			cs.setGenericPart(genericPart);
		for (ElBuilder builder : _scope.items()) {
//			if (builder instanceof AccessNotation) {
//				cs.addAccess((AccessNotation) builder);
//			} else {
//				cs.add(builder);
//			}
			OS_Element built;
			builder.setParent(cs);
			builder.setContext(cs.getContext());
			built = builder.build();
			if (!(cs.hasItem(built))) // already added by constructor
				cs.add(built);
		}
//		_inh.setParent(cs);
		cs.setInheritance(_inh);
		return cs;
	}

	public void annotation_clause(AnnotationClause a) {
		if (a == null) return;
		annotations.add(a);
	}

	public void setName(IdentExpression identExpression) {
		_name = identExpression;
	}

	public void setParent(OS_Element o) {
		_parent = o;
	}

	public void setParentContext(Context o) {
		_parent_context = o;
	}

	public ClassScope getScope() {
		return _scope;
	}

	public ClassInheritance classInheritance() {
		return _inh;
	}

	public void annotations(List<AnnotationClause> as) {
		for (AnnotationClause annotationClause : as) {
			annotation_clause(annotationClause);
		}
	}

	public void setGenericPart(TypeNameList tnl) {
		genericPart = tnl;
	}
}

//
//
//
