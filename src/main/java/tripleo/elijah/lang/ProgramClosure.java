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

public abstract class ProgramClosure {
	public @NotNull ClassStatement classStatement(final OS_Element aParent, Context ctx) {
		final @NotNull ClassStatement classStatement = new ClassStatement(aParent, ctx);
		return classStatement;
	}

//	public ImportStatement importStatement(OS_Element aParent) {
//		final ImportStatement importStatement = new ImportStatement(aParent);
//		return importStatement;
//	}

	public @NotNull NamespaceStatement namespaceStatement(final OS_Element aParent, Context ctx) {
		final @NotNull NamespaceStatement namespaceStatement = new NamespaceStatement(aParent, ctx);
		return namespaceStatement;
	}

	public @NotNull AliasStatement aliasStatement(final @NotNull OS_Element aParent) {
		final @NotNull AliasStatement aliasStatement = new AliasStatement(aParent);
		return aliasStatement;
	}

}

//
//
//
