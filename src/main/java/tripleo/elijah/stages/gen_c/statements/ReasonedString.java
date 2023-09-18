package tripleo.elijah.stages.gen_c.statements;

class ReasonedString implements IReasonedString {
	String text;
	String reason;

	public ReasonedString(final String aText, final String aReason) {
		text   = aText;
		reason = aReason;
	}

	@Override
	public String reason() {
		return reason;
	}

	@Override
	public String text() {
		return text;
	}
}
