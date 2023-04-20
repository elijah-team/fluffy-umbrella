/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tripleo.elijah.nextgen.outputstatement;

import org.jetbrains.annotations.NotNull;

/**
 * @author Tripleo Nova
 */
public class EG_CompoundStatement implements EG_Statement {

	private final EG_SingleStatement beginning;
	private final EG_SingleStatement ending;
	private final EG_Statement       middle;
	private final boolean            indent;
	private final EX_Explanation     explanation;

	public EG_CompoundStatement(final @NotNull EG_SingleStatement aBeginning,
	                            final @NotNull EG_SingleStatement aEnding,
	                            final @NotNull EG_Statement aMiddle,
	                            final boolean aIndent,
	                            final @NotNull EX_Explanation aExplanation) {
		beginning   = aBeginning;
		ending      = aEnding;
		middle      = aMiddle;
		indent      = aIndent;
		explanation = aExplanation;
	}

	@Override
	public String getText() {

		final String sb = beginning.getText() +
		  middle.getText() +
		  ending.getText();

		return sb;
	}

	@Override
	public EX_Explanation getExplanation() {
		return explanation;
	}
}
