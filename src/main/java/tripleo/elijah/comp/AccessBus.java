package tripleo.elijah.comp;

import org.jdeferred2.DoneCallback;
import org.jdeferred2.impl.DeferredObject;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleList;

public class AccessBus {
    private final Compilation _c;
    private final DeferredObject<PipelineLogic, Void, Void> pipeLineLogicPromise = new DeferredObject<>();
    // private final DeferredObject<List<GeneratedNode>, Void, Void> lgcPromise = new DeferredObject<>();
    // private final DeferredObject<EIT_ModuleList, Void, Void> moduleListPromise = new DeferredObject<>();
    // private final DeferredObject<GenerateResult, Void, Void> generateResultPromise = new DeferredObject<>();

    public AccessBus(Compilation aC) {
        _c = aC;
    }

    public @NotNull Compilation getCompilation() {
        return _c;
    }

    public void subscribePipelineLogic(DoneCallback<PipelineLogic> aPipelineLogicDoneCallback) {
        pipeLineLogicPromise.then(aPipelineLogicDoneCallback);
    }

    void resolvePipelineLogic(PipelineLogic pl) {
        pipeLineLogicPromise.resolve(pl);
    }

    public interface AB_ModuleListListener {
        void mods_slot(final EIT_ModuleList aModuleList);
    }

}
