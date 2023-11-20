package tripleo.elijah.lang;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.stages.deduce.DeduceLookupUtils;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.ResolveError;

import java.io.File;

/**
 * Created 8/16/20 7:42 AM
 */
public class TypeOfTypeName implements TypeName {
	private Context       _ctx;
	private Qualident     _typeOf;
	private TypeModifiers modifiers;

	public TypeOfTypeName(final Context cur) {
		_ctx = cur;
	}

	public void typeOf(final Qualident xy) {
		_typeOf = xy;
	}

	public Qualident typeOf() {
		return _typeOf;
	}

	public void set(final TypeModifiers modifiers_) {
		modifiers = modifiers_;
	}

	@Override
	public void setContext(final Context context) {
		_ctx = context;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public Type kindOfType() {
		return Type.TYPE_OF;
	}

	@Override
	public Context getContext() {
		return _ctx;
	}

	public TypeName resolve(final @NotNull Context ctx, final DeduceTypes2 deduceTypes2) throws ResolveError {
//		tripleo.elijah.util.Stupidity.println2(_typeOf.toString());
		final LookupResultList lrl  = DeduceLookupUtils.lookupExpression(_typeOf, ctx, deduceTypes2);
		final OS_Element       best = lrl.chooseBest(null);
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
