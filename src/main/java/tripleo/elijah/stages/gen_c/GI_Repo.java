package tripleo.elijah.stages.gen_c;

import tripleo.elijah.lang.*;
import tripleo.elijah.stages.instructions.ProcIA;

import java.util.HashMap;
import java.util.Map;

class GI_Repo {
	private final Map<Object, GenerateC_Item> items = new HashMap<>();

	public GenerateC_Item itemFor(final ProcIA aProcIA) {
		final GI_ProcIA gi_proc;
		if (items.containsKey(aProcIA)) {
			gi_proc = (GI_ProcIA) items.get(aProcIA);
		} else {
			gi_proc = new GI_ProcIA(aProcIA);
			items.put(aProcIA, gi_proc);
		}
		return gi_proc;
	}

	public GenerateC_Item itemFor(final OS_Element e) {
		if (e instanceof ClassStatement) {
			return new GI_ClassStatement((ClassStatement) e, this);
		} else if (e instanceof FunctionDef) {
			return new GI_FunctionDef((FunctionDef) e, this);
		} else if (e instanceof PropertyStatement) {
			return new GI_PropertyStatement((PropertyStatement) e, this);
		} else if (e instanceof VariableStatement) {
			return new GI_VariableStatement((VariableStatement) e, this);
		}

		return null;
	}
}
