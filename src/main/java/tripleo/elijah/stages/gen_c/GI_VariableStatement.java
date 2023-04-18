package tripleo.elijah.stages.gen_c;

import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.gen_fn.EvaNode;

public class GI_VariableStatement implements GenerateC_Item {
	private final VariableStatement variableStatement;
	private final GI_Repo repo;
	private EvaNode _evaNaode;

	public GI_VariableStatement(final VariableStatement aVariableStatement, final GI_Repo aRepo) {
		variableStatement = aVariableStatement;
		repo              = aRepo;
	}

	@Override
	public void setEvaNode(final EvaNode aEvaNode) {
		_evaNaode = aEvaNode;
	}

	@Override
	public EvaNode getEvaNode() {
		return _evaNaode;
	}
}
