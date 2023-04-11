package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang2.SpecialVariables;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;

class ZoneVTE implements ZoneMember {
	private final VariableTableEntry varTableEntry;
	private final BaseEvaFunction    gf;
	private final String             _realTargetName;

	public ZoneVTE(final VariableTableEntry aVarTableEntry, final BaseEvaFunction aGf) {
		varTableEntry   = aVarTableEntry;
		gf              = aGf;
		_realTargetName = __getRealTargetName();
	}

	public String getRealTargetName() {
		return Emit.emit("/*879*/") + _realTargetName;
	}

	@NotNull
	private String __getRealTargetName() {
		final String vte_name = varTableEntry.getName();
		switch (varTableEntry.vtt) {
		case TEMP -> {
			if (varTableEntry.getName() == null) {
				return "vt" + varTableEntry.tempNum;
			} else {
				return "vt" + varTableEntry.getName();
			}
		}
		case ARG -> {
			return "va" + vte_name;
		}
		default -> {
			if (SpecialVariables.contains(vte_name)) {
				return SpecialVariables.get(vte_name);
			} else if (GenerateC.isValue(gf, vte_name)) {
				return "vsc->vsv";
			} else {
				return "vv" + vte_name;
			}
		}
		}
	}
}
