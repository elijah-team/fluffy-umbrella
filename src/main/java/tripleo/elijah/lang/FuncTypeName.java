package tripleo.elijah.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.util.NotImplementedException;

import java.io.File;

/**
 * Created 8/16/20 2:16 AM
 */
public class FuncTypeName implements TypeName {
	private final Context _ctx;
	private TypeModifiers _modifiers;
	private @Nullable TypeNameList _arglist = null/*new TypeNameList()*/;
	private @Nullable TypeName _returnValue = null /*new RegularTypeName()*/; // TODO warning

	public FuncTypeName(final Context cur) {
		_ctx = cur;
	}

	public void argList(final TypeNameList tnl) {
		_arglist = tnl;
	}

	public void argList(final @NotNull FormalArgList op) {
		@NotNull TypeNameList tnl = new TypeNameList();
		for (@NotNull FormalArgListItem fali : op.falis) {
			final TypeName tn = fali.typeName();
			if (tn != null)
				tnl.add(tn);
			else {
//				tnl.add(TypeName.Undeclared(fali)); // TODO implement me
			}
		}
		argList(tnl);
	}

	//	@Override
	public void type(final TypeModifiers typeModifiers) {
		_modifiers = typeModifiers;
	}

	@Override
	public @NotNull Type kindOfType() {
		return Type.FUNCTION;
	}

	public void returnValue(final TypeName rtn) {
		_returnValue = rtn;
	}

	@Override
	public boolean isNull() {
		return _arglist == null && _returnValue == null;
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
	public int getLine() {
		return -1;
	}

	@Override
	public int getColumn() {
		return -1;
	}

	@Override
	public int getLineEnd() {
		return -1;
	}

	@Override
	public int getColumnEnd() {
		return -1;
	}

	@Override
	public @Nullable File getFile() {
		return null;
	}
}

//
//
//