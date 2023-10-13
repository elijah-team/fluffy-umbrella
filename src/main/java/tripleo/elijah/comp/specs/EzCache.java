package tripleo.elijah.comp.specs;

import tripleo.elijah.ci.*;

import java.util.*;

public interface EzCache {
	Optional<CompilerInstructions> get(String aAbsolutePath);

	void put(EzSpec aSpec, String aAbsolutePath, CompilerInstructions aR);
}
