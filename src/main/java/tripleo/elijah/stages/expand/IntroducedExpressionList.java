package tripleo.elijah.stages.expand;

import java.util.ArrayList;
import java.util.List;

public class IntroducedExpressionList {
	private final List<FunctionPrelimInstruction> items = new ArrayList<FunctionPrelimInstruction>();

	public void add(final FunctionPrelimInstruction i) {
		items.add(i);
	}

	@Override
	public String toString() {
		return items.toString();
	}
}
