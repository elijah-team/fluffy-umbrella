/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.contexts.ClassContext;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a "class"
 * <p>
 * items -> ClassItems
 * docstrings
 * variables
 */
public class ClassStatement extends _CommonNC /* ProgramClosure */
		implements ClassItem, ModuleItem, StatementItem, FunctionItem, OS_Element, OS_Element2, Documentable,
		OS_Container {
	static final List<TypeName> emptyTypeNameList = ImmutableList.<TypeName>of();
	private final OS_Element   parent;
	ClassInheritance _inh = new ClassInheritance();    // remove final for ClassBuilder
	private       ClassTypes   _type;
	private       TypeNameList genericPart;
	private       OS_Type      osType;

	public ClassStatement(final OS_Element parentElement, final Context parentContext) {
		parent = parentElement;    // setParent

		@NotNull final ElObjectType x = DecideElObjectType.getElObjectType(parentElement);

		switch (x) {
		case MODULE:
			final OS_Module module = (OS_Module) parentElement;

			//
			this.setPackageName(module.pullPackageName());
			_packageName.addElement(this);
			module.add(this);

			break;

		case FUNCTION:

			// do nothing
			break;

		default:

			// we kind of fail the switch test here because OS_Container is not an OS_Element,
			// so we have to test explicitly, messing up the pretty flow we had.
			// hey sh*t happens.
			if (parentElement instanceof OS_Container) {
				((OS_Container) parentElement).add(this);
			} else {
				throw new IllegalStateException(String.format("Cant add ClassStatement to %s", parentElement));
			}
		}

		setContext(new ClassContext(parentContext, this));
	}

	public ProgramClosure XXX() {
		return new ProgramClosure() {
		}
				;
	}

	@Override    // OS_Container
	public void add(final OS_Element anElement) {
		if (!(anElement instanceof ClassItem)) {
			throw new IllegalStateException(String.format("Cant add %s to ClassStatement", anElement));
		}

		items.add((ClassItem) anElement);
	}

	public ConstructorDef addCtor(final IdentExpression aConstructorName) {
		return new ConstructorDef(aConstructorName, this, getContext());
	}

	public DestructorDef addDtor() {
		return new DestructorDef(this, getContext());
	}

	// endregion
	// region annotations
	public Iterable<AnnotationPart> annotationIterable() {
		List<AnnotationPart> aps = new ArrayList<AnnotationPart>();

		if (annotations == null) {
			return aps;
		}

		for (AnnotationClause annotationClause : annotations) {
			aps.addAll(annotationClause.aps);
		}

		return aps;
	}

	public ClassInheritance classInheritance() {
		return _inh;
	}

	public DefFunctionDef defFuncDef() {
		return new DefFunctionDef(this, getContext());
	}

	public Collection<ClassItem> findFunction(final String name) {
		return Collections2.filter(
				items,
				new Predicate<ClassItem>() {
					@Override
					public boolean apply(@Nullable final ClassItem item) {
						if ((item instanceof FunctionDef) && !(item instanceof ConstructorDef)) {
							if (((FunctionDef) item).name().equals(name)) {
								return true;
							}
						}

						return false;
					}
				}
		);
	}

	// endregion
	// region called from parser
	public FunctionDef funcDef() {
		return new FunctionDef(this, getContext());
	}

	public InvariantStatement invariantStatement() {
		NotImplementedException.raise();

		return null;
	}

	public void postConstruct() {
		assert nameToken != null;

		int destructor_count = 0;

		for (ClassItem item : items) {
			if (item instanceof DestructorDef) {
				destructor_count++;
			}
		}

		assert (destructor_count == 0) || (destructor_count == 1);
	}

	public PropertyStatement prop() {
		PropertyStatement propertyStatement = new PropertyStatement(this, getContext());

		add(propertyStatement);

		return propertyStatement;
	}

	public StatementClosure statementClosure() {
		return new AbstractStatementClosure(this);
	}

	@Override
	public String toString() {
		final String package_name;

		if ((getPackageName() != null) && (getPackageName()._name != null)) {
			final Qualident package_name_q = getPackageName()._name;

			package_name = package_name_q.toString();
		} else {
			package_name = "`'";
		}

		return String.format("<Class %d %s %s>", _a.getCode(), package_name, getName());
	}

	public TypeAliasStatement typeAlias() {
		NotImplementedException.raise();

		return null;
	}

	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.addClass(this);    // TODO visitClass
	}

	@SuppressWarnings("UnnecessaryLocalVariable")
	public Collection<ConstructorDef> getConstructors() {
		final Collection<ClassItem>      x = Collections2.filter(items, (ClassItem input) -> input instanceof ConstructorDef);
		final Collection<ConstructorDef> y = Collections2.transform(x, input -> (ConstructorDef) input);
		return y;
	}

	@Override    // OS_Element
	public ClassContext getContext() {
		return (ClassContext) _a._context;
	}

	public void setContext(final ClassContext ctx) {
		_a.setContext(ctx);
	}

	// endregion
	public @NotNull
	List<TypeName> getGenericPart() {
		if (genericPart == null) {
			return emptyTypeNameList;
		} else {
			return genericPart.p;
		}
	}

	public void setGenericPart(TypeNameList genericPart) {
		this.genericPart = genericPart;
	}

	public void setInheritance(ClassInheritance inh) {
		_inh = inh;
	}

	// region inheritance
	public IdentExpression getNameNode() {
		return nameToken;
	}

	public OS_Type getOS_Type() {
		if (osType == null) {
			osType = new OS_Type(this);
		}

		return osType;
	}

	@Override
	public OS_Element getParent() {
		return parent;
	}

	public ClassTypes getType() {
		return _type;
	}

	public void setType(final ClassTypes aType) {
		_type = aType;
	}
}

//
//
//


//~ Formatted by Jindent --- http://www.jindent.com
