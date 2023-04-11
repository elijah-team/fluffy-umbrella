package tripleo.elijah.stages.gen_c;

import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;

import java.util.HashMap;
import java.util.Map;

class Zone {
	private final Map<Object, ZoneMember> members = new HashMap<Object, ZoneMember>();

	public ZoneVTE get(final VariableTableEntry aVarTableEntry, final BaseEvaFunction aGf) {
		if (members.containsKey(aVarTableEntry))
			return (ZoneVTE) members.get(aVarTableEntry);

		final ZoneVTE r = new ZoneVTE(aVarTableEntry, aGf);
		members.put(aVarTableEntry, r);
		return r;
	}
}
