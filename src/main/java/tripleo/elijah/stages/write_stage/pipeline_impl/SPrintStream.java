package tripleo.elijah.stages.write_stage.pipeline_impl;

import tripleo.elijah.stages.write_stage.pipeline_impl.XPrintStream;

public class SPrintStream implements XPrintStream {
	private final StringBuilder sb = new StringBuilder();

	@Override
	public void println(final String aS) {
		sb.append(aS);
		sb.append('\n');
	}

	public String getString() {
		return sb.toString();
	}
}