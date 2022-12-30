package tripleo.elijah.nextgen.outputtree;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.nextgen.inputtree.EIT_Input;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;

import java.util.ArrayList;
import java.util.List;

public class EOT_OutputFile {
    //    private final OS_Module module;
    private final Compilation c;

    private final List<EIT_Input> _inputs = new ArrayList<>();
    private final String _filename;
    private final EOT_OutputType _type;
    private final EG_Statement _sequence; // TODO List<?> ??

    public EOT_OutputFile(final Compilation c,
                          final @NotNull List<EIT_Input> inputs,
                          final String filename,
                          final EOT_OutputType type,
                          final EG_Statement sequence) {
        this.c = c;
        _filename = filename;
        _type = type;
        _sequence = sequence;
        _inputs.addAll(inputs);
    }

/*
    public EOT_OutputFile(final OS_Module module, final Compilation c) {
        this.module = module;
        this.c = c;
    }
*/

    public String getFilename() {
        return _filename;
    }

    public EOT_OutputType getType() {
        return _type;
    }

    public EG_Statement getStatementSequence() {
        return _sequence;
    }

    public List<EIT_Input> getInputs() {
        return _inputs;
    }

    // rules/constraints whatever
}
