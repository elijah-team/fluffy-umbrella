package tripleo.elijah;

import tripleo.elijah.comp.Compilation;

public interface SimpleTest {
	SimpleTest setFile(String aS);

	SimpleTest run() throws Exception;

	int errorCount();

	default boolean assertLiveClass(String aClassName) {
		throw new UnintendedUseException();
	}

	default AssertingLiveClass assertingLiveClass(String aClassName) {
		throw new UnintendedUseException();
//		return null;
	}

	Compilation c();
}
