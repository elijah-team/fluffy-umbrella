package tripleo.elijah.world.impl;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.stages.garish.GarishNamespace;
import tripleo.elijah.stages.gen_c.GenerateC;
import tripleo.elijah.stages.gen_fn.EvaNamespace;
import tripleo.elijah.stages.gen_generic.GenerateResult;
import tripleo.elijah.stages.gen_generic.pipeline_impl.GenerateResultSink;
import tripleo.elijah.world.i.LivingNamespace;

//import static com.ibm.j9ddr.StructureHeader.BlobID.node;

public class DefaultLivingNamespace implements LivingNamespace {
	private final EvaNamespace    node;
	private       GarishNamespace _garish;
	private int _code;

	@Contract(pure = true)
	public DefaultLivingNamespace(final EvaNamespace aNode) {
		node = aNode;
	}

	@Override
	public EvaNamespace evaNode() {
		return node;
	}

	@Override
	public void garish(final GenerateC aGenerateC, final GenerateResult gr, final GenerateResultSink aResultSink) {
		getGarish().garish(aGenerateC, gr, aResultSink);
	}

	@Override
	public int getCode() {
		return _code;
	}

	@Override
	public NamespaceStatement getElement() {
		return (NamespaceStatement) node.getElement();
	}

	@Override
	public @NotNull GarishNamespace getGarish() {
		if (_garish == null) {
			_garish = new GarishNamespace(/*this*/);
		}
		return _garish;
	}

	@Override public void setCode(final int aCode) {
		_code = aCode;
	}
}
