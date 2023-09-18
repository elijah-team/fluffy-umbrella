package tripleo.elijah.stages.gen_c.statements;

import java.util.function.Supplier;

class ReasonedSuppliedString implements IReasonedString {
	Supplier<String> textSupplier;
	String           reason;

	public ReasonedSuppliedString(final Supplier<String> aText, final String aReason) {
		textSupplier = aText;
		reason       = aReason;
	}

	@Override
	public String reason() {
		return reason;
	}

	@Override
	public String text() {
		return textSupplier.get();
	}
}
