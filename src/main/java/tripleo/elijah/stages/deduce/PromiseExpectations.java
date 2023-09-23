package tripleo.elijah.stages.deduce;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

class PromiseExpectations {
	long counter = 0;

	@NotNull List<PromiseExpectation> exp = new ArrayList<>();

	public void add(@NotNull PromiseExpectation aExpectation) {
		counter++;
		aExpectation.setCounter(counter);
		exp.add(aExpectation);
	}

	public void check() {
		for (@NotNull PromiseExpectation promiseExpectation : exp) {
			if (!promiseExpectation.isSatisfied())
				promiseExpectation.fail();
		}
	}
}
