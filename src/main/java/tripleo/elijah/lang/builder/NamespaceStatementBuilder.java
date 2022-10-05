/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import antlr.Token;
import tripleo.elijah.lang.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/23/20 2:38 AM
 */
public class NamespaceStatementBuilder extends ElBuilder implements Documentable {
	private List<AnnotationClause> annotations = new ArrayList<AnnotationClause>();
	private NamespaceTypes _type;
	private OS_Element _parent;
	private Context _parent_context;
	private IdentExpression _name;
	private NamespaceScope _scope = new NamespaceScope();
	private Context _context;

	public void setType(NamespaceTypes namespaceTypes) {
		_type = namespaceTypes;
	}

	@Override
	public NamespaceStatement build() {
		NamespaceStatement cs = new NamespaceStatement(_parent, _parent_context);
		cs.setType(_type);
		cs.setName(_name);
		for (AnnotationClause annotation : annotations) {
			cs.addAnnotation(annotation);
		}
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
		cs.postConstruct();
		return cs;
	}

	@Override
	protected void setContext(Context context) {
		_context = context;
	}

	public void annotations(AnnotationClause a) {
		annotations.add(a);
	}

	public void setName(IdentExpression identExpression) {
		_name = identExpression;
	}

//	public void setParent(OS_Element o) {
//		_parent = o;
//	}

	public void setParentContext(Context o) {
		_parent_context = o;
	}

//	public ClassScope getScope() {
//		return _scope;
//	}

	private final List<Token> _docstrings = new ArrayList<Token>();

	@Override
	public void addDocString(Token s1) {
		_docstrings.add(s1);
	}


	public NamespaceScope scope() {
		return _scope;
	}
}

//
//
//
