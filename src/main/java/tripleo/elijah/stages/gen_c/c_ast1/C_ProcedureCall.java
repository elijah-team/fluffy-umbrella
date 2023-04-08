package tripleo.elijah.stages.gen_c.c_ast1;

import tripleo.elijah.util.Helpers;

import java.text.MessageFormat;
import java.util.List;

public class C_ProcedureCall {
	private String targetName;
	private List<String> args;

	public void setTargetName(final String aS) {
		targetName = aS;
	}

	public void setArgs(final List<String> aArgs) {
		args = aArgs;
	}

	public String getString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(targetName);
		sb.append("(");
		sb.append(Helpers.String_join(", ", args)); // FIXME
		sb.append(")");

		final String str = sb.toString();
		return str;
	}
}
