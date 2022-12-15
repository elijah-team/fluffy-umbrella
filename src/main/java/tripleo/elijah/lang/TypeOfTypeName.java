package tripleo.elijah.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.stages.deduce.DeduceLookupUtils;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.ResolveError;
import tripleo.elijah.util.NotImplementedException;

import java.io.File;

/**
 * Created 8/16/20 7:42 AM
 */
public class TypeOfTypeName implements TypeName {
	private final Context _ctx;
	private Qualident _typeOf;
	private TypeModifiers modifiers;

	public TypeOfTypeName(final Context cur) {
		_ctx=cur;
	}

	public void typeOf(final Qualident xy) {
		_typeOf=xy;
	}

	public void set(final TypeModifiers modifiers_) {
		modifiers = modifiers_;
	}

	@Override
	public @NotNull Type kindOfType() {
		return Type.TYPE_OF;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public void setContext(final Context context) {
		throw new NotImplementedException();
	}

	@Override
	public Context getContext() {
		return _ctx;
	}

	public @Nullable TypeName resolve(@NotNull Context ctx, @NotNull DeduceTypes2 deduceTypes2) throws ResolveError {
//		System.out.println(_typeOf.toString());
		LookupResultList lrl = DeduceLookupUtils.lookupExpression(_typeOf, ctx, deduceTypes2);
		@Nullable OS_Element best = lrl.chooseBest(null);
		if (best instanceof VariableStatement)
			return ((VariableStatement) best).typeName();
		return null;
	}

	// region Locatable

	// TODO what about keyword
	@Override
	public int getColumn() {
		return _typeOf.parts().get(0).getColumn();
	}

	// TODO what about keyword
	@Override
	public int getLine() {
		return _typeOf.parts().get(0).getLine();
	}

	@Override
	public int getLineEnd() {
		return _typeOf.parts().get(_typeOf.parts().size()).getLineEnd();
	}

	@Override
	public int getColumnEnd() {
		return _typeOf.parts().get(_typeOf.parts().size()).getColumnEnd();
	}

	@Override
	public File getFile() {
		return _typeOf.parts().get(0).getFile();
	}

	// endregion
}

//
//
//
