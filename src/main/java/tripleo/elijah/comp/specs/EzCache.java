package tripleo.elijah.comp.specs;

import tripleo.elijah.ci.CompilerInstructions;

import java.util.Optional;

public interface EzCache {
	Optional<CompilerInstructions> get(String aAbsolutePath);

	void put(EzSpec aSpec, String aAbsolutePath, CompilerInstructions aR);
}
