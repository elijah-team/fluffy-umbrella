/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import java.util.ArrayList;
import java.util.List;

import antlr.Token;
import tripleo.elijah.lang.AnnotationClause;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.Documentable;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.NamespaceTypes;
import tripleo.elijah.lang.OS_Element;

/**
 * Created 12/23/20 2:38 AM
 */
public class NamespaceStatementBuilder extends ElBuilder implements Documentable {
	private final List<AnnotationClause> annotations = new ArrayList<AnnotationClause>();
	private final NamespaceScope         _scope      = new NamespaceScope();
	private final List<Token> _docstrings = new ArrayList<Token>();
	private       NamespaceTypes         _type;
	private       OS_Element             _parent;
	private       Context                _parent_context;
	private       IdentExpression        _name;
	private       Context                _context;

	@Override
	public void addDocString(final Token s1) {
		_docstrings.add(s1);
	}

	public void annotations(final AnnotationClause a) {
		annotations.add(a);
	}

	@Override
	public NamespaceStatement build() {
		final NamespaceStatement cs = new NamespaceStatement(_parent, _parent_context);
		cs.setType(_type);
		cs.setName(_name);
		for (final AnnotationClause annotation : annotations) {
			cs.addAnnotation(annotation);
		}
		for (final ElBuilder builder : _scope.items()) {
//			if (builder instanceof AccessNotation) {
//				cs.addAccess((AccessNotation) builder);
//			} else {
//				cs.add(builder);
//			}
			final OS_Element built;
			builder.setParent(cs);
			builder.setContext(cs.getContext());
			built = builder.build();
			if (!(cs.hasItem(built))) // already added by constructor
				cs.add(built);
		}
		cs.postConstruct();
		return cs;
	}

	public NamespaceScope scope() {
		return _scope;
	}

//	public void setParent(OS_Element o) {
//		_parent = o;
//	}

	@Override
	protected void setContext(final Context context) {
		_context = context;
	}

//	public ClassScope getScope() {
//		return _scope;
//	}

	public void setName(final IdentExpression identExpression) {
		_name = identExpression;
	}

	public void setParentContext(final Context o) {
		_parent_context = o;
	}


	public void setType(final NamespaceTypes namespaceTypes) {
		_type = namespaceTypes;
	}
}

//
//
//
