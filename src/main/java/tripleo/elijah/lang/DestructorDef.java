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

/**
 * @author Tripleo
 *
 * Created 	Apr 16, 2020 at 7:35:50 AM
 */
public class DestructorDef extends BaseFunctionDef {

	private final ClassStatement parent;

	public DestructorDef(final ClassStatement aClassStatement, final Context context) {
		parent = aClassStatement;
		if (aClassStatement instanceof OS_Container) {
			((OS_Container) parent).add(this);
		} else {
			throw new IllegalStateException("adding DestructorDef to " + aClassStatement.getClass().getName());
		}
		_a.setContext(new FunctionContext(context, this));
		setSpecies(Species.DTOR);
	}

	@Override
	public void visitGen(final ElElementVisitor visit) {
		visit.visitDestructor(this);
	}

	@Override
	public OS_Element getParent() {
		return null;
	}

	@Override
	public void postConstruct() {

	}
}
