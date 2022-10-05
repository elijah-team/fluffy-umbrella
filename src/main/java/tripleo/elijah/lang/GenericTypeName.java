/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
/**
 *
 */
package tripleo.elijah.lang;

import tripleo.elijah.util.NotImplementedException;

import java.io.File;

/**
 * Created 8/16/20 7:42 AM
 */
public class GenericTypeName implements TypeName {
	private final Context _ctx;
	private Qualident _typeName;
	private TypeModifiers modifiers;
	private TypeName constraint;

	public GenericTypeName(final Context cur) {
		_ctx=cur;
	}

	public void typeName(final Qualident xy) {
		_typeName = xy;
	}

	public void set(final TypeModifiers modifiers_) {
		modifiers = modifiers_;
	}

	@Override
	public boolean isNull() {
		return _typeName == null;
	}

	@Override
	public void setContext(final Context context) {
		throw new NotImplementedException();
	}

	@Override
	public Context getContext() {
		return _ctx;
	}

	@Override
	public Type kindOfType() {
		return Type.GENERIC;
	}

	public void setConstraint(TypeName aConstraint) {
		constraint = aConstraint;
	}

	// region Locatable

	@Override
	public int getLine() {
		return _typeName.parts().get(0).getLine();
	}

	@Override
	public int getColumn() {
		return _typeName.parts().get(0).getColumn();
	}

	@Override
	public int getLineEnd() {
		return _typeName.parts().get(_typeName.parts().size()-1).getLineEnd();
	}

	@Override
	public int getColumnEnd() {
		return _typeName.parts().get(_typeName.parts().size()-1).getColumnEnd();
	}

	@Override
	public File getFile() {
		return _typeName.parts().get(0).getFile();
	}

	// endregion
}

//
//
//
