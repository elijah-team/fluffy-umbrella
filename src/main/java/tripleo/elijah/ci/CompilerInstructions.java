package tripleo.elijah.ci;

import antlr.Token;

import java.util.List;

public interface CompilerInstructions {

	void add(GenerateStatement generateStatement);

	void add(LibraryStatementPartImpl libraryStatementPart);

	String genLang();

	String getFilename();

	List<LibraryStatementPart> getLibraryStatementParts();

	String getName();

	IndexingStatement indexingStatement();

	void setFilename(String filename);

	void setName(String name);

	void setName(Token name);

}
