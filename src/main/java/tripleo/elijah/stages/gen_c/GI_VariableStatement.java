package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.BaseFunctionDef;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.VariableSequence;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.gen_fn.EvaNode;
import tripleo.elijah.stages.gen_fn.IEvaFunctionBase;

public class GI_VariableStatement implements GenerateC_Item {
	private final VariableStatement variableStatement;
	private final GI_Repo           repo;
	private       EvaNode           _evaNode;

	public GI_VariableStatement(final VariableStatement aVariableStatement, final GI_Repo aRepo) {
		variableStatement = aVariableStatement;
		repo              = aRepo;
	}

	@Override
	public EvaNode getEvaNode() {
		return _evaNode;
	}

	@Override
	public void setEvaNode(final EvaNode aEvaNode) {
		_evaNode = aEvaNode;
	}

	public void __CReference_getIdentIAPath_IdentIAHelper(final @NotNull CReference aCReference,
														  final @NotNull IEvaFunctionBase generatedFunction,
														  final @Nullable String value) {
		final CReference.Reference r = ___CReference_getIdentIAPath_IdentIAHelper(generatedFunction, value);
		aCReference.addRef(r);
	}

	private CReference.Reference ___CReference_getIdentIAPath_IdentIAHelper(final @NotNull IEvaFunctionBase generatedFunction, final @Nullable String value) {
		final String text2 = variableStatement.getName();

		// first getParent is VariableSequence
		final VariableSequence variableSequence = (VariableSequence) variableStatement.getParent();
		final OS_Element       parent           = variableSequence.getParent();

		final BaseFunctionDef fd = generatedFunction.getFD();

		if (parent == fd.getParent()) {
			// A direct member value. Doesn't handle when indirect
//				text = Emit.emit("/*124*/")+"vsc->vm" + text2;
			return new CReference.Reference(text2, CReference.Ref.DIRECT_MEMBER, value);
		} else if (parent == fd) {
			return new CReference.Reference(text2, CReference.Ref.LOCAL);
		}

		//if (parent instanceof NamespaceStatement) {
		//	int y=2;
		//}

		return new CReference.Reference(text2, CReference.Ref.MEMBER, value);
	}
}
