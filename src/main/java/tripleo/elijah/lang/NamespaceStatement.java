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
 *
 * Created Apr 2, 2019 at 11:08:12 AM
 */
public class NamespaceStatement extends _CommonNC implements Documentable, ModuleItem, ClassItem, StatementItem, FunctionItem, OS_Container, OS_Element2 {

	private final OS_Element parent;
	private NamespaceTypes _kind;

	public NamespaceStatement(final OS_Element aElement, final Context context) {
		parent = aElement; // setParent
		if (aElement instanceof  OS_Module) {
			final OS_Module module = (OS_Module) aElement;
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

	public void setContext(final NamespaceContext ctx) {
		_a.setContext(ctx);
	}

	public StatementClosure statementClosure() {
		return new AbstractStatementClosure(new AbstractScope2(this) {
			@Override
			public void statementWrapper(final IExpression aExpr) {
				throw new NotImplementedException();
			}

			@Override
			public StatementClosure statementClosure() {
				throw new NotImplementedException();
//				return null;
			}

			@Override
			public void add(final StatementItem aItem) {
				NamespaceStatement.this.add((OS_Element) aItem);
			}

		});
	}

	public TypeAliasStatement typeAlias() {
		throw new NotImplementedException();
	}

	public InvariantStatement invariantStatement() {
		throw new NotImplementedException();
	}

	public FunctionDef funcDef() {
		return new FunctionDef(this, getContext());
	}

	public ProgramClosure XXX() {
		return new ProgramClosure() {
		};
	}

	@Override // OS_Element
	public void visitGen(final @NotNull ElElementVisitor visit) {
		visit.visitNamespaceStatement(this);
	}

	@Override // OS_Element
	public OS_Element getParent() {
		return parent;
	}

	@Override // OS_Element
	public Context getContext() {
		return _a.getContext();
	}

	public void setType(final NamespaceTypes aType) {
		_kind = aType;
	}

	public NamespaceTypes getKind() { return _kind; }

	@Override
	public String toString() {
		return String.format("<Namespace %d %s `%s'>", _a.getCode(), getPackageName()._name, getName());
	}

	@Override // OS_Container
	public void add(final OS_Element anElement) {
		if (anElement instanceof ClassItem)
			items.add((ClassItem) anElement);
		else
			System.err.printf("[NamespaceStatement#add] not a ClassItem: %s%n", anElement);
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

	public OS_Type getOS_Type() {
//		return new OS_Type(OS_Type.Type.);
		return null; // TODO
	}

	// region ClassItem

	// endregion

}

//
//
//
