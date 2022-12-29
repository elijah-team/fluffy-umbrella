package tripleo.elijah.stages.gen_c;

import tripleo.elijah.nextgen.outputstatement.EX_Explanation;

public class EX_Rule implements EX_Explanation {
    private final String rule;

    public EX_Rule(final String aRule) {
        rule = aRule;
    }
}
