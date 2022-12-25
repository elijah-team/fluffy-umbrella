/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.lang;

public abstract class ProgramClosure {
	public ClassStatement classStatement(final OS_Element aParent, final Context ctx) {
		final ClassStatement classStatement = new ClassStatement(aParent, ctx);
		return classStatement;
	}

//	public ImportStatement importStatement(OS_Element aParent) {
//		final ImportStatement importStatement = new ImportStatement(aParent);
//		return importStatement;
//	}

	public NamespaceStatement namespaceStatement(final OS_Element aParent, final Context ctx) {
		final NamespaceStatement namespaceStatement = new NamespaceStatement(aParent, ctx);
		return namespaceStatement;
	}

	public AliasStatement aliasStatement(final OS_Element aParent) {
		final AliasStatement aliasStatement = new AliasStatement(aParent);
		return aliasStatement;
	}

}

//
//
//
