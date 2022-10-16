package tripleo.elijah.nextgen.inputtree;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.lang.OS_Module;

import java.util.List;

public class EIT_ModuleList {
	private final List<OS_Module> mods;

	@Contract(pure = true)
	public EIT_ModuleList(List<OS_Module> aMods) {
		mods = aMods;
	}

	public List<OS_Module> getMods() {
		return mods;
	}
}
