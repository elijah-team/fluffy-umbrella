/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 * 
 * The contents of this library are released under the LGPL licence v3, 
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 * 
 */
/*
 * Created on Sep 1, 2005 8:16:32 PM
 * 
 * $Id$
 *
 */
package tripleo.elijah.lang;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;

public class ParserClosure extends ProgramClosure {

	public ParserClosure(final String fn, @NotNull final Compilation compilation) {
		module = new OS_Module();
		module.setFileName(fn);
		module.setParent(compilation); // TODO take a look at all this here
		compilation.addModule(module, fn);
	}

	private OS_Module module() {
		return module;
	}

	public OS_Package defaultPackageName(final Qualident aPackageName) {
//		assert module.packageName == null;
		module.pushPackageName(aPackageName);
		return module.parent.makePackage(aPackageName);
	}

	public void packageName(final Qualident aPackageName) {
		//assert module.packageName ==null;
		module.pushPackageName(aPackageName);
	}

	public final OS_Module module;

	public IndexingStatement indexingStatement() {
		final IndexingStatement indexingStatement = new IndexingStatement();
		indexingStatement.setModule(module());
		return indexingStatement;
	}

}

