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
 * Created 12/22/20 8:57 PM
 */
public class FunctionDefBuilder extends BaseFunctionDefBuilder {

    private final FunctionDefScope _scope = new FunctionDefScope();
    private final List<FunctionModifiers> _mods = new ArrayList<FunctionModifiers>();
    private TypeName _returnType;
    private Context _context;

    public FunctionDefScope scope() {
        return _scope;
    }

    public void set(final FunctionModifiers aFunctionModifiers) {
        _mods.add(aFunctionModifiers);
    }

	public void setReturnType(final TypeName tn) {
		_returnType = tn;
	}

	@Override
	public FunctionDef build() {
		final FunctionDef functionDef = new FunctionDef(_parent, _context);
		functionDef.setName(_name);
		functionDef.setFal(mFal == null ? new FormalArgList() : mFal);
		functionDef.setReturnType(_returnType);
		for (final FunctionModifiers mod : _mods) {
			functionDef.set(mod);
		}
		for (final AnnotationClause a : annotations) {
			functionDef.addAnnotation(a);
		}
		if (_scope.isAbstract()) {
			functionDef.setAbstract(true);
		}
		final Scope3 scope3 = new Scope3(functionDef);
		functionDef.scope(scope3);
		for (final ElBuilder b : _scope.items()) {
			b.setParent(functionDef);
			b.setContext(functionDef.getContext());
			final OS_Element built = b.build();
			if (!(functionDef.hasItem(built))) // already added by constructor
				functionDef.add(built);
		}
		functionDef.setSpecies(_species);
		functionDef.postConstruct();
		return functionDef;
	}

	@Override
	protected void setContext(final Context context) {
		_context = context;
	}
}

//
//
//
