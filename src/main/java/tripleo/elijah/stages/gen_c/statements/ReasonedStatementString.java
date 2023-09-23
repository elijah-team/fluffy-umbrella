package tripleo.elijah.stages.gen_c.statements;

import tripleo.elijah.nextgen.outputstatement.EG_Statement;

public class ReasonedStatementString implements IReasonedString {
	private final EG_Statement text;
	private final String reason;

	public ReasonedStatementString(final EG_Statement aText, final String aReason) {
		text = aText;
		reason = aReason;
	}

	@Override
	public String reason() {
		return reason;
	}

	@Override
	public String text() {
		return text.getText();
	}
}
