package tripleo.elijah.stages.deduce;

public class PromiseExpectation<B> {

	private final DeduceTypes2                 deduceTypes2;
	private final DeduceTypes2.ExpectationBase base;
	private final String                       desc;
	private       boolean                      _printed;
	private       long                         counter;
	private       B                            result;
	private       boolean                      satisfied;

	public PromiseExpectation(final DeduceTypes2 aDeduceTypes2, DeduceTypes2.ExpectationBase aBase, String aDesc) {
		deduceTypes2 = aDeduceTypes2;
		base         = aBase;
		desc         = aDesc;
	}

	public void fail() {
		if (!_printed) {
			deduceTypes2.LOG.err(String.format("Expectation (%s, %d) not met: %s", deduceTypes2, counter, desc));
			_printed = true;
		}
	}

	public boolean isSatisfied() {
		return satisfied;
	}

	public void satisfy(B aResult) {
		final String satisfied_already = satisfied ? " already" : "";
		//assert !satisfied;
		if (!satisfied) {
			result    = aResult;
			satisfied = true;
			deduceTypes2.LOG.info(String.format("Expectation (%s, %d)%s met: %s %s", deduceTypes2, counter, satisfied_already, desc, base.expectationString()));
		}
	}

	public void setCounter(long aCounter) {
		counter = aCounter;

///////			LOG.info(String.format("Expectation (%s, %d) set: %s %s", DeduceTypes2.this, counter, desc, base.expectationString()));
	}
}
