package tripleo.elijah.stages.deduce.tastic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.TypeName;
import tripleo.elijah.stages.deduce.ClassInvocation;
import tripleo.elijah.stages.deduce.DeduceTypes2;
import tripleo.elijah.stages.deduce.ForFunction;
import tripleo.elijah.stages.deduce.IInvocation;
import tripleo.elijah.stages.deduce.NamespaceInvocation;
import tripleo.elijah.stages.gen_fn.EvaFunction;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.ProcTableEntry;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.stages.gen_fn.VariableTableEntry;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.util.ReadySupplier_1;

import java.util.List;

public class FT_FCA_FunctionDef {
	private final FunctionDef  fd;
	private final DeduceTypes2 _dt2;

	public FT_FCA_FunctionDef(final FunctionDef aFd, final DeduceTypes2 aDt2) {
		fd   = aFd;
		_dt2 = aDt2;
	}

	void loop2_i(@NotNull FT_FnCallArgs.DoAssignCall aDoAssignCall, final @NotNull ProcTableEntry pte, final @NotNull VariableTableEntry vte, final int instructionIndex) {
		final @Nullable IInvocation invocation;
		if (fd.getParent() == aDoAssignCall.generatedFunction.getFD().getParent()) {
			invocation = aDoAssignCall.dc.getInvocation((EvaFunction) aDoAssignCall.generatedFunction);
		} else {
			if (fd.getParent() instanceof NamespaceStatement) {
				NamespaceInvocation ni = aDoAssignCall.dc.registerNamespaceInvocation((NamespaceStatement) fd.getParent());
				invocation = ni;
			} else if (fd.getParent() instanceof final @NotNull ClassStatement classStatement) {
				@Nullable ClassInvocation     ci          = _inj().new_ClassInvocation(classStatement, null, new ReadySupplier_1<>(_dt2));
				final @NotNull List<TypeName> genericPart = classStatement.getGenericPart();
				if (!genericPart.isEmpty()) {
					// TODO handle generic parameters somehow (getInvocationFromBacklink?)

				}
				ci         = aDoAssignCall.dc.registerClassInvocation(ci);
				invocation = ci;
			} else
				throw new NotImplementedException();
		}
		aDoAssignCall.dc.forFunction(aDoAssignCall.dc.newFunctionInvocation(fd, pte, invocation), new ForFunction() {
			@Override
			public void typeDecided(@NotNull GenType aType) {
				if (!vte.typeDeferred_isPending()) {
					if (vte.resolvedType() == null) {
						final @Nullable ClassInvocation ci = aDoAssignCall.dc.genCI(aType, null);
						vte.getType().genTypeCI(ci);
						ci.resolvePromise().then(vte::resolveTypeToClass);
					}
					aDoAssignCall.LOG.err("2041 type already found " + vte);
					return; // type already found
				}
				// I'm not sure if below is ever called
				@NotNull TypeTableEntry tte = aDoAssignCall.generatedFunction.newTypeTableEntry(TypeTableEntry.Type.TRANSIENT, aDoAssignCall.dc.gt(aType), pte.__debug_expression(), pte);
				vte.addPotentialType(instructionIndex, tte);
			}
		});
	}

	private DeduceTypes2.DeduceTypes2Injector _inj() {
		return _dt2._inj();
	}
}
