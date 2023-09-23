package tripleo.elijah.stages.deduce;

import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.FormalArgListItem;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.stages.gen_fn.BaseEvaFunction;
import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.gen_fn.IdentIaResolveable;
import tripleo.elijah.stages.instructions.IdentIA;
import tripleo.elijah.util.Helpers;

public class DT_Resolvabley  implements IdentIaResolveable {
	private final List<DT_Resolvable> x;

	public DT_Resolvabley(final List<DT_Resolvable> aX) {
		x = aX;
	}

	public @NotNull String getNormalPath(final @NotNull BaseEvaFunction generatedFunction, final IdentIA identIA) {
		final List<String> rr = new LinkedList<>();

		for (final DT_Resolvable resolvable : x) {
			final OS_Element element = resolvable.element();
			if (element == null && resolvable.deduceItem() instanceof final FunctionInvocation fi) {
				final var fd = fi.getFunction();
				rr.add("%s".formatted(fd.getNameNode().getText()));
				//rr.add("%s()".formatted(fd.getNameNode().getText()));
				continue;
			}

			if (element instanceof final ClassStatement cs) {
				if (resolvable.deduceItem() instanceof final FunctionInvocation fi) {
					if (fi.getFunction() instanceof final ConstructorDef cd) {
						rr.add("%s()".formatted(cs.getName()));
						continue;
					}
				}
			}
			if (element instanceof final FunctionDef fd) {
				if (resolvable.deduceItem() == null) {
					// when ~ is folders.forEach, this is null (fi not set yet)
					rr.add("%s".formatted(fd.getNameNode().getText()));
					continue;
				}

				if (resolvable.deduceItem() instanceof final FunctionInvocation fi) {
					if (fi.getFunction() == fd) {
						rr.add("%s".formatted(fd.getNameNode().getText()));
//						rr.add("%s(...)".formatted(fd.getNameNode().getText()));
						continue;
					}
				}
			}
			if (element instanceof final VariableStatement vs) {
				rr.add(vs.getName());
				continue;
			}
			if (element instanceof final FormalArgListItem fali) {
				rr.add(fali.name());
				continue;
			}
			if (resolvable.instructionArgument() instanceof final IdentIA identIA2) {
				final var ite = identIA2.getEntry();

				if (ite._callable_pte() != null) {
					final var cpte = ite._callable_pte();

					assert cpte.getStatus() != BaseTableEntry.Status.KNOWN;

					rr.add("%s".formatted(ite.getIdent().getText()));
					continue;
				}
			}
		}

		final String r = Helpers.String_join(".", rr);

		final String z = generatedFunction.getIdentIAPathNormal(identIA);

		//assert r.equals(z);
		if (!r.equals(z)) {
			//08/13
			System.err.println("----- 67 Should be " + z);
		}

		return r;
	}
}
