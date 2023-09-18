package tripleo.elijah.stages.gen_fn;

import tripleo.elijah.lang.IExpression;

public class EvaExpression<T extends IExpression> {
	private final BaseTableEntry entry;
	private final T              expression;

	public EvaExpression(final T aExpression, final BaseTableEntry aBaseTableEntry) {
		expression = aExpression;
		entry      = aBaseTableEntry;
	}

	public BaseTableEntry getEntry() {
		return entry;
	}

	public T getExpression() {
		return expression;
	}
}
