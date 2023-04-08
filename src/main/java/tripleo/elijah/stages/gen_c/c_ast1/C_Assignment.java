package tripleo.elijah.stages.gen_c.c_ast1;

import java.text.MessageFormat;

public class C_Assignment {
	private String left;
	private C_ProcedureCall right_pc;

	public void setLeft(final String aString) {
		left = aString;
	}

	public void setRight(final C_ProcedureCall aProcedureCall) {
		right_pc = aProcedureCall;
	}

	public String getString() {
		final String str = MessageFormat.format("{0} = {1}", left, right_pc.getString());
		return str;
	}
}
