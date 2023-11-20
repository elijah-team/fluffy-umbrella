package tripleo.elijah.stages.deduce;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.lang.ProcedureCallExpression;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.IElementHolder;

public class DTR_ProcedureCall {
	private final DeduceTypeResolve       deduceTypeResolve;
	private final ProcedureCallExpression procedureCallExpression;

	@Contract(pure = true)
	public DTR_ProcedureCall(final DeduceTypeResolve aDeduceTypeResolve, final ProcedureCallExpression aProcedureCallExpression) {
		deduceTypeResolve       = aDeduceTypeResolve;
		procedureCallExpression = aProcedureCallExpression;
	}

	public void run(final IElementHolder eh, final GenType genType) {
//		final TypeName typeName1 = variableStatement.typeName();
//
//		if (!(typeName1 instanceof NormalTypeName)) {
//			throw new IllegalStateException();
//		}
//
//		final NormalTypeName normalTypeName = (NormalTypeName) typeName1;
//
//		if (normalTypeName.getGenericPart() != null) {
//			normalTypeName_notGeneric(eh, genType, normalTypeName);
//		} else {
//			if (!normalTypeName.isNull()) {
//				normalTypeName_generic_butNotNull(eh, genType, normalTypeName);
//			}
//		}
	}

}
