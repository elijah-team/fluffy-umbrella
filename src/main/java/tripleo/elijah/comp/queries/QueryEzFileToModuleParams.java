package tripleo.elijah.comp.queries;

import java.io.InputStream;

public class QueryEzFileToModuleParams {

	public String      sourceFilename;
	public InputStream inputStream;

	public QueryEzFileToModuleParams(final String aSourceFilename, final InputStream aInputStream) {
		sourceFilename = aSourceFilename;
		inputStream    = aInputStream;
	}
}
