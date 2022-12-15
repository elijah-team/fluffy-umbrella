/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.stages.gen_fn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.stages.deduce.IInvocation;

/**
 * Created 5/31/21 1:32 PM
 */
public class GenType {
	public NamespaceStatement resolvedn;
	public OS_Type typeName; // TODO or just TypeName ??
	public TypeName nonGenericTypeName;
	public @Nullable OS_Type resolved;
	public @Nullable IInvocation ci;
	public GeneratedNode node;

	public GenType(NamespaceStatement aNamespaceStatement) {
		resolvedn = /*new OS_Type*/(aNamespaceStatement);
	}

	public GenType(@NotNull ClassStatement aClassStatement) {
		resolved = new OS_Type(aClassStatement);
	}

	public GenType() {

	}

	public void set(@NotNull OS_Type aType) {
		switch (aType.getType()) {
		case USER:
			typeName = aType;
			break;
		case USER_CLASS:
			resolved = aType;
		default:
			System.err.println("48 Unknown in set: "+aType);
		}
	}

	public void copy(@NotNull GenType aGenType) {
		if (resolvedn == null) resolvedn = aGenType.resolvedn;
		if (typeName == null) typeName = aGenType.typeName;
		if (nonGenericTypeName == null) nonGenericTypeName = aGenType.nonGenericTypeName;
		if (resolved == null) resolved = aGenType.resolved;
		if (ci == null) ci = aGenType.ci;
		if (node == null) node = aGenType.node;
	}
}

//
//
//
