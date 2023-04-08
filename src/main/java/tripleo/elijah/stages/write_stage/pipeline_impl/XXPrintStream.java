package tripleo.elijah.stages.write_stage.pipeline_impl;

import java.io.PrintStream;

public class XXPrintStream implements XPrintStream {

	private final PrintStream p;

	public XXPrintStream(final PrintStream aP) {
		p = aP;
	}

	@Override
	public void println(final String aS) {
		p.println(aS);
	}
}
