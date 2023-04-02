package tripleo.elijah.nextgen.outputtree;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.Compilation;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.nextgen.inputtree.EIT_Input;
import tripleo.elijah.nextgen.inputtree.EIT_ModuleInput;
import tripleo.elijah.nextgen.outputstatement.EG_CompoundStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Naming;
import tripleo.elijah.nextgen.outputstatement.EG_SequenceStatement;
import tripleo.elijah.nextgen.outputstatement.EG_SingleStatement;
import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.outputstatement.EX_Explanation;
import tripleo.elijah.stages.gen_generic.GenerateResultItem;
import tripleo.util.buffer.Buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static tripleo.elijah.util.Helpers.List_of;

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
	    this.c    = c;
	    _filename = filename;
	    _type     = type;
	    _sequence = sequence;
	    _inputs.addAll(inputs);
    }

	public static EOT_OutputFile grToOutputFile(final Compilation aC, final GenerateResultItem ab) {
		final List<EIT_Input> inputs = List_of(new EIT_ModuleInput(ab.node.module(), aC));

		final EG_SingleStatement beginning = new EG_SingleStatement("", new EX_Explanation() {
		});
		final EG_SingleStatement middle = new EG_SingleStatement(ab.buffer.getText(), new EX_Explanation() {
		});
		final EG_SingleStatement ending = new EG_SingleStatement("", new EX_Explanation() {
		});
		final EX_Explanation explanation = new EX_Explanation() {
			public String getText() {
				return "generate-result-item";
			}
		};

		final EG_CompoundStatement seq = new EG_CompoundStatement(beginning, ending, middle, false, explanation);

		final EOT_OutputFile eof = new EOT_OutputFile(aC, inputs, ab.output, EOT_OutputType.SOURCES, seq);
		return eof;
	}

	public static @NotNull EOT_OutputFile bufferSetToOutputFile(final String aFilename,
	                                                            final @NotNull Collection<Buffer> aBuffers,
	                                                            final Compilation comp,
	                                                            final OS_Module aModule) {
		final List<EIT_Input> inputs = List_of(new EIT_ModuleInput(aModule, comp));

		final List<EG_Statement> statementStream = aBuffers.stream()
		                                                   .map(buffer ->
		                                                     new EG_SingleStatement(buffer.getText(), new EX_Explanation() {
		                                                     })
		                                                   ).collect(Collectors.toList());
		final EG_SequenceStatement seq = new EG_SequenceStatement(new EG_Naming("yyy"), statementStream);

		final EOT_OutputFile eof = new EOT_OutputFile(comp, inputs, aFilename, EOT_OutputType.SOURCES, seq);
		return eof;
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
