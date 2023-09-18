package tripleo.elijah.ci;

import antlr.Token;
import tripleo.elijah.lang.IExpression;

public interface LibraryStatementPart {

	void addDirective(Token token, IExpression iExpression);

	String getDirName();

	CompilerInstructions getInstructions();

	String getName();

	void setDirName(Token dirName);

	void setInstructions(CompilerInstructions instructions);

	void setName(Token i1);

}