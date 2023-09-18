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
import tripleo.elijah.contexts.NamespaceContext;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.NotImplementedException;

/**
 * @author Tripleo(sb)
 * <p>
 * Created Apr 2, 2019 at 11:08:12 AM
 */
public class NamespaceStatement extends _CommonNC implements Documentable, ModuleItem, ClassItem, StatementItem, FunctionItem, OS_Container, OS_Element2 {

	private final OS_Element     parent;
	private       NamespaceTypes _kind;

	public NamespaceStatement(final OS_Element aElement, final Context context) {
		parent = aElement; // setParent
		if (aElement instanceof final OS_Module module) {
			//
			this.setPackageName(module.pullPackageName());
			_packageName.addElement(this);
			module.add(this);
		} else if (aElement instanceof OS_Container) {
			((OS_Container) aElement).add(this);
		} else {
			throw new IllegalStateException(String.format("Cant add NamespaceStatement to %s", aElement));
		}
		setContext(new NamespaceContext(context, this));
	}

	@Override // OS_Container
	public void add(final OS_Element anElement) {
		if (anElement instanceof ClassItem)
			items.add((ClassItem) anElement);
		else
			System.err.printf("[NamespaceStatement#add] not a ClassItem: %s%n", anElement);
	}

	public FunctionDef funcDef() {
		return new FunctionDef(this, getContext());
	}

	@Override // OS_Element
	public Context getContext() {
		return _a.getContext();
	}

	public NamespaceTypes getKind() {
		return _kind;
	}

	public OS_Type getOS_Type() {
//		return new OS_Type(OS_Type.Type.);
		return null; // TODO
	}

	@Override // OS_Element
	public OS_Element getParent() {
		return parent;
	}

	public InvariantStatement invariantStatement() {
		throw new NotImplementedException();
	}

	public void postConstruct() {
		if (nameToken == null || nameToken.getText().equals("")) {
			setType(NamespaceTypes.MODULE);
		} else if (nameToken.getText().equals("_")) {
			setType(NamespaceTypes.PRIVATE);
		} else if (nameToken.getText().equals("__package__")) {
			setType(NamespaceTypes.PACKAGE);
		} else {
			setType(NamespaceTypes.NAMED);
		}
	}

	public void setContext(final NamespaceContext ctx) {
		_a.setContext(ctx);
	}

	public void setType(final NamespaceTypes aType) {
		_kind = aType;
	}

	public StatementClosure statementClosure() {
		return new AbstractStatementClosure(new AbstractScope2(this) {
			@Override
			public void add(final StatementItem aItem) {
				NamespaceStatement.this.add((OS_Element) aItem);
			}

			@Override
			public StatementClosure statementClosure() {
				throw new NotImplementedException();
//				return null;
			}

			@Override
			public void statementWrapper(final IExpression aExpr) {
				throw new NotImplementedException();
			}

		});
	}

	@Override
	public String toString() {
		return String.format("<Namespace %d %s `%s'>", _a.getCode(), getPackageName()._name, getName());
	}

	public TypeAliasStatement typeAlias() {
		throw new NotImplementedException();
	}

	@Override // OS_Element
	public void visitGen(final @NotNull ElElementVisitor visit) {
		visit.visitNamespaceStatement(this);
	}

	public ProgramClosure XXX() {
		return new ProgramClosure() {
		};
	}

	// region ClassItem

	// endregion

}

//
//
//
