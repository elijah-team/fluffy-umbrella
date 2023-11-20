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

/**
 * Created 12/22/20 10:55 PM
 */
public class ConstructorDefBuilder extends BaseFunctionDefBuilder {
	private final ConstructorDefScope _scope = new ConstructorDefScope();
    private Context _context;

	public ConstructorDefScope scope() {
		return _scope;
	}

	@Override
	public ConstructorDef build() {
		assert _parent instanceof ClassStatement;
		//
		final ConstructorDef cd = new ConstructorDef(_name, (ClassStatement) _parent, _context);
		cd.setName(_name);
		cd.setFal(mFal);
		for (final AnnotationClause a : annotations) {
			cd.addAnnotation(a);
		}
		final Scope3 scope3 = new Scope3(cd);
		cd.scope(scope3);
		for (final ElBuilder item : _scope.items()) {
			item.setParent(cd);
			item.setContext(_context);
			cd.add(item.build());
		}
//		assert _species == FunctionDef.Species.CTOR;
		cd.setSpecies(FunctionDef.Species.CTOR);
		cd.postConstruct();
		return cd;
	}

	@Override
	protected void setContext(final Context context) {
		_context = context;
	}
}

//
//
//
