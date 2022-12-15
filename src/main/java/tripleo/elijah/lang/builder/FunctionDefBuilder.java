/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang.builder;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 12/22/20 8:57 PM
 */
public class FunctionDefBuilder extends BaseFunctionDefBuilder {

	private @NotNull FunctionDefScope _scope = new FunctionDefScope();
	private @NotNull List<FunctionModifiers> _mods = new ArrayList<FunctionModifiers>();
	private TypeName _returnType;
	private Context _context;

	public FunctionDefScope scope() {
		return _scope;
	}

	public void set(FunctionModifiers aFunctionModifiers) {
		_mods.add(aFunctionModifiers);
	}

	public void setReturnType(TypeName tn) {
		_returnType = tn;
	}

	@Override
	public @NotNull FunctionDef build() {
		@NotNull FunctionDef functionDef = new FunctionDef(_parent, _context);
		functionDef.setName(_name);
		functionDef.setFal(mFal == null ? new FormalArgList() : mFal);
		functionDef.setReturnType(_returnType);
		for (FunctionModifiers mod : _mods) {
			functionDef.set(mod);
		}
		for (AnnotationClause a : annotations) {
			functionDef.addAnnotation(a);
		}
		if (_scope.isAbstract()) {
			functionDef.setAbstract(true);
		}
		@NotNull Scope3 scope3 = new Scope3(functionDef);
		functionDef.scope(scope3);
		for (@NotNull ElBuilder b : _scope.items()) {
			b.setParent(functionDef);
			b.setContext(functionDef.getContext());
			OS_Element built = b.build();
			if (!(functionDef.hasItem(built))) // already added by constructor
				functionDef.add(built);
		}
		functionDef.setSpecies(_species);
		functionDef.postConstruct();
		return functionDef;
	}

	@Override
	protected void setContext(Context context) {
		_context = context;
	}
}

//
//
//
