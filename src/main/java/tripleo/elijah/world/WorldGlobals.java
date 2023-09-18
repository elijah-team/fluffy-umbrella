package tripleo.elijah.world;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.OS_Package;
import tripleo.elijah.util.Helpers;

public enum WorldGlobals {
	;

	public final static IdentExpression emptyConstructorName = Helpers.string_to_ident("<>");

	// TODO override name() ??
	public final static  ConstructorDef defaultVirtualCtor = new ConstructorDef(null, null, null);
	private static final OS_Package     _dp                = new OS_Package(null, 0);

	public static @NotNull OS_Package defaultPackage() {
		return _dp;
	}

}
