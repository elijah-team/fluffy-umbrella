/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tripleo.elijah.nextgen.outputstatement;

/**
 * @author Tripleo Nova
 */
public class EG_SingleStatement implements EG_Statement {
	private final String         text;
	private final EX_Explanation explanation;

	public EG_SingleStatement(final String aText, final EX_Explanation aExplanation) {
		text        = aText;
		explanation = aExplanation;
	}

	public EG_SingleStatement(final String aText) {
		text        = aText;
		explanation = null;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public EX_Explanation getExplanation() {
		return explanation;
	}

	public EG_SingleStatement tag(final String aS, final int aI) {
		return this;
	}

	public EG_SingleStatement rule(final String aS, final int aI) {
		return this;
	}
}
