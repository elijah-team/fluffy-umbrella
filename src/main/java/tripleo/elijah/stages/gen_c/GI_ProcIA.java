package tripleo.elijah.stages.gen_c;

import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.instructions.ProcIA;

class GI_ProcIA implements GenerateC_Item {
	private final ProcIA carrier;
	private EvaNode _evaNode;

	public GI_ProcIA(final ProcIA aProcIA) {
		carrier = aProcIA;
	}

	@Override
	public EvaNode getEvaNode() {
		return _evaNode;
	}

	@Override
	public void setEvaNode(final EvaNode a_evaNaode) {
		_evaNode = a_evaNaode;
	}
}
