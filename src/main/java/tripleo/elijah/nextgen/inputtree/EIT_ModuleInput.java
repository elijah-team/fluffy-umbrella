package tripleo.elijah.nextgen.inputtree;

import org.jetbrains.annotations.Contract;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.lang.OS_Module;

public class EIT_ModuleInput implements EIT_Input {
    private final OS_Module module;
    private final Compilation c;

    @Contract(pure = true)
    public EIT_ModuleInput(final OS_Module module, final Compilation c) {
        this.module = module;
        this.c = c;
    }

    @Override
    public EIT_InputType getType() {
        return EIT_InputType.ELIJAH_SOURCE;
    }
}
