package tripleo.elijah.nextgen.inputtree

import tripleo.elijah.comp.DeducePipeline.ResolvedNodes
import tripleo.elijah.stages.gen_generic.ICodeRegistrar

internal data class PL_Each_Env(var processParams: EIT_ModuleList._ProcessParams) {
    val codeRegistrar: ICodeRegistrar by lazyOf(processParams.deducePhase.codeRegistrar)

    val resolvedNodes: ResolvedNodes by lazyOf(ResolvedNodes())
}
