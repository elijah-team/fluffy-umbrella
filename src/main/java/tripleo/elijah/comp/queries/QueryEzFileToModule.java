package tripleo.elijah.comp.queries;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import tripleo.elijah.ci.CompilerInstructions;
import tripleo.elijah.comp.Operation;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.query.QueryDatabase;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijjah.EzLexer;
import tripleo.elijjah.EzParser;

import java.io.InputStream;

public class QueryEzFileToModule {
	private final QueryEzFileToModuleParams params;

	public QueryEzFileToModule(final QueryEzFileToModuleParams aParams) {
		params = aParams;
	}

	public OS_Module load(final QueryDatabase qb) {
		throw new NotImplementedException();
	}

	public Operation<CompilerInstructions> calculate() {
		final String      f = params.sourceFilename;
		final InputStream s = params.inputStream;

		final EzLexer lexer = new EzLexer(s);
		lexer.setFilename(f);
		final EzParser parser = new EzParser(lexer);
		parser.setFilename(f);
		try {
			parser.program();
		} catch (final RecognitionException aE) {
			return Operation.failure(aE);
		} catch (final TokenStreamException aE) {
			return Operation.failure(aE);
		}
		final CompilerInstructions instructions = parser.ci;
		instructions.setFilename(f);
		return Operation.success(instructions);
	}


}
