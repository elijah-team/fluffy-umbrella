package tripleo.elijah.stages.gen_c;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.OS_Element;
import tripleo.elijah.lang.PropertyStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;

public class GCS_Property_Get implements EG_Statement {
	private final PropertyStatement p;

	public GCS_Property_Get(final PropertyStatement aP) {
		p = aP;
	}

	@Override
	public @NotNull EX_Explanation getExplanation() {
		return EX_Explanation.withMessage("GCS_Property_Get");
	}

	@Override
	public String getText() {
		final OS_Element parent = p.getParent();
		final int        code;

		if (parent instanceof ClassStatement) {
			code = -3;
		} else if (parent instanceof NamespaceStatement) {
			code = -3;
		} else {
//				code = -1;
			throw new IllegalStateException("PropertyStatement can't have other parent than ns or cls. " + parent.getClass().getName());
		}

		// TODO Don't know if get or set!
		final String text2 = String.format("ZP%dget_%s", code, p.name());

		return text2;
	}

}
