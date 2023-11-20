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
import tripleo.elijah.contexts.PropertyStatementContext;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.Helpers;

/**
 * Created 8/6/20 4:00 PM
 */
public class PropertyStatement implements OS_Element, OS_Element2, ClassItem {

	private final Context context;
	private final OS_Element parent;

	public FunctionDef set_fn;
	public FunctionDef get_fn;
	private IdentExpression prop_name;
	private TypeName typeName;
	private boolean _set_is_abstract;
	private boolean _get_is_abstract;

	public PropertyStatement(final OS_Element parent, final Context cur) {
		this.parent = parent;
		this.context = new PropertyStatementContext(cur, this);
	}

	@NotNull
	private FunctionDef createSetFunction() {
		final FunctionDef functionDef = new FunctionDef(this, getContext());
		functionDef.setName(Helpers.string_to_ident(String.format("<prop_set %s>", prop_name)));
		functionDef.setSpecies(FunctionDef.Species.PROP_SET);
		final @NotNull FormalArgList fal  = new FormalArgList();
		final FormalArgListItem      fali = fal.next();
		fali.setName(Helpers.string_to_ident("Value"));
		fali.setTypeName(this.typeName);
		final RegularTypeName unitType = new RegularTypeName();
		unitType.setName(Helpers.string_to_qualident("Unit"));
		functionDef.setReturnType(unitType/*BuiltInTypes.Unit*/);
		functionDef.setFal(fal);
		return functionDef;
	}

	@NotNull
	private FunctionDef createGetFunction() {
		final FunctionDef functionDef = new FunctionDef(this, getContext());
		functionDef.setName(Helpers.string_to_ident(String.format("<prop_get %s>", prop_name)));
		functionDef.setSpecies(FunctionDef.Species.PROP_GET);
		functionDef.setReturnType(typeName);
		return functionDef;
	}

	@Override // OS_Element
	public void visitGen(final ElElementVisitor visit) {
		visit.visitPropertyStatement(this);
	}

	@Override // OS_Element
	public OS_Element getParent() {
		return parent;
	}

	@Override // OS_Element
	public Context getContext() {
		return context;
	}

	public void setName(final IdentExpression prop_name) {
		this.prop_name = prop_name;
	}

//	public TypeName typeName() {
//		return tn;
//	}

/*
	public Scope get_scope() {
		throw new NotImplementedException();
//		return get_fn.scope();
	}

	public Scope set_scope() {
		throw new NotImplementedException();
//		return set_fn.scope();
	}
*/

	public FunctionDef get_fn() {
		return get_fn;
	}

	public FunctionDef set_fn() {
		return set_fn;
	}

	@Override
	public String name() {
		return prop_name.getText();
	}

	public void setTypeName(final TypeName typeName) {
//		tripleo.elijah.util.Stupidity.println_err2("** setting TypeName in PropertyStatement to "+typeName);
		this.typeName = typeName;
		this.set_fn = createSetFunction();
		this.get_fn = createGetFunction();
	}

	public TypeName getTypeName() {
		return typeName;
	}


	public void addGet() {
		_get_is_abstract = true;
	}

	public void addSet() {
		_set_is_abstract = true;
	}

	public void get_scope(final Scope3 sco) {
		get_fn.scope(sco);
	}

	public void set_scope(final Scope3 sco) {
		set_fn.scope(sco);
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

}

//
//
//
