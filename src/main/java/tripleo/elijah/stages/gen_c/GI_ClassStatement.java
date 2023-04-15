package tripleo.elijah.stages.gen_c;

import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.IdentTableEntry;

class GI_ClassStatement implements GenerateC_Item {
	private final ClassStatement e;
	private final GI_Repo        giRepo;
	private EvaNode _evaNaode;
	private IdentTableEntry _ite;

	public GI_ClassStatement(final ClassStatement aE, final GI_Repo aGIRepo) {
		e = aE;
		giRepo = aGIRepo;
	}

	@Override
	public void setEvaNode(final EvaNode aEvaNode) {
		_evaNaode = aEvaNode;
	}

	@Override
	public EvaNode getEvaNode() {
		return _evaNaode;
	}

	public void setITE(final IdentTableEntry ite) {
		EvaNode resolved = null;

		if (ite.type != null)
			resolved = ite.type.resolved();
		if (resolved == null)
			resolved = ite.resolvedType();

		_ite = ite;
		_evaNaode = resolved;
	}
}
