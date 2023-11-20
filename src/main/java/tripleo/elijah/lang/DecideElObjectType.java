/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

import org.jetbrains.annotations.NotNull;

/**
 * Created 6/22/21 12:59 AM
 */
public class DecideElObjectType {
	@NotNull
	public static ElObjectType getElObjectType(/*@NotNull*/ final OS_Element input) {
		if (input instanceof ClassStatement)
			return ElObjectType.CLASS;
		else if (input instanceof NamespaceStatement)
			return ElObjectType.NAMESPACE;
		else if (input instanceof VariableSequence)
			return ElObjectType.VAR_SEQ;
		else if (input instanceof VariableStatement)
			return ElObjectType.VAR;
		else if (input instanceof ConstructorDef)
			return ElObjectType.CONSTRUCTOR;
		else if (input instanceof FunctionDef)
			return ElObjectType.FUNCTION;
		else if (input instanceof FormalArgListItem)
			return ElObjectType.FORMAL_ARG_LIST_ITEM;
		else if (input instanceof OS_Module)
			return ElObjectType.MODULE;
		return ElObjectType.UNKNOWN;
	}

}

//
//
//
