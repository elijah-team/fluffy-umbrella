package tripleo.elijah.nextgen.inputtree;

import tripleo.elijah.comp.DeducePipeline.ResolvedNodes;
import tripleo.elijah.stages.gen_generic.ICodeRegistrar;

public class PL_Each_Env {
	private final EIT_ModuleList._ProcessParams processParams;
	private       ICodeRegistrar                codeRegistrar;
	private       ResolvedNodes                 resolvedNodes;

	public PL_Each_Env(final EIT_ModuleList._ProcessParams aPlp) {
		processParams = aPlp;
	}

	public ICodeRegistrar codeRegistrar() {
		if (codeRegistrar == null) {
			codeRegistrar = processParams.getDeducePhase().codeRegistrar;
		}
		return codeRegistrar;
	}

	public ResolvedNodes getResolvedNodes() {
		if (resolvedNodes == null) {
			resolvedNodes = new ResolvedNodes();
		}
		return resolvedNodes;
	}

	public EIT_ModuleList._ProcessParams getProcessParams() {
		return processParams;
	}

	public ICodeRegistrar getCodeRegistrar() {
		return codeRegistrar();
	}
}
