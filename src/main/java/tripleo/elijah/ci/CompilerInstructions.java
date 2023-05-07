/*
 *   -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 **Elijjah compiler,copyright Tripleo<oluoluolu+elijah@gmail.com>
 **
 **The contents of this library are released under the LGPL licence v3,
 **the GNU Lesser General Public License text was downloaded from
 **http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 **
 *
 *
 */
package tripleo.elijah.ci;

import antlr.Token;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface CompilerInstructions {
	CiIndexingStatement indexingStatement();

	void add(GenerateStatement generateStatement);

	void add(LibraryStatementPart libraryStatementPart);

	String getFilename();

	void setFilename(String filename);

	@Nullable
	String genLang();

	String getName();

	void setName(String name);

	void setName(Token name);
}
