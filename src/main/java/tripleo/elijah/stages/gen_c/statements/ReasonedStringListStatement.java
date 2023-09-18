package tripleo.elijah.stages.gen_c.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;

public class ReasonedStringListStatement implements EG_Statement {
	private final List<IReasonedString> rss = new ArrayList<>();

	public void append(final EG_Statement aText, final String aReason) {
		rss.add(new ReasonedStatementString(aText, aReason));
	}

	public void append(final String aText, final String aReason) {
		rss.add(new ReasonedString(aText, aReason));
	}

	public void append(final Supplier<String> aText, final String aReason) {
		rss.add(new ReasonedSuppliedString(aText, aReason));
	}

	@Override
	public EX_Explanation getExplanation() {
		return EX_Explanation.withMessage("xyz");
	}

	@Override
	public @Nullable String getText() {
		final StringBuilder sb2 = new StringBuilder();
		for (IReasonedString reasonedString : rss) {
			sb2.append(reasonedString.text());
		}
		return sb2.toString();
	}
}
