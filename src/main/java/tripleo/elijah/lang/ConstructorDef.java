/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
package tripleo.elijah.lang;

import tripleo.elijah.contexts.FunctionContext;
import tripleo.elijah.lang2.ElElementVisitor;
import tripleo.elijah.util.Helpers;

/**
 * @author Tripleo
 *
 * Created 	Apr 16, 2020 at 7:34:07 AM
 */
public class ConstructorDef extends BaseFunctionDef {
	public static final IdentExpression emptyConstructorName = Helpers.string_to_ident("<>");

	// TODO override name() ??
	public static final ConstructorDef defaultVirtualCtor = new ConstructorDef(null, null, null);

	private final OS_Element parent;

	public ConstructorDef(final IdentExpression aConstructorName, final _CommonNC aParent, final Context context) {
		parent = (OS_Element) aParent;
		if (parent != null) {
			if (aParent instanceof OS_Container) {
				((OS_Container) parent).add(this);
			} else {
				throw new IllegalStateException("adding FunctionDef to " + aParent.getClass().getName());
			}
			_a.setContext(new FunctionContext(context, this));
		}

		if (aConstructorName != null)
			setName(aConstructorName);
		else
			setName(emptyConstructorName); // hack for Context#lookup
		setSpecies(Species.CTOR);
	}

	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitConstructorDef(this);
	}

	@Override // OS_Element
	public OS_Element getParent() {
		return parent;
	}

	@Override
	public void postConstruct() {

	}

	@Override
	public String toString() {
		return String.format("<Constructor %s %s %s>", parent, name(), getArgs());
	}


}

//
//
//
