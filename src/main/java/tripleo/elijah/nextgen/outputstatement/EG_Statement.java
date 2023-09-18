/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package tripleo.elijah.nextgen.outputstatement;

/**
 * @author Tripleo Nova
 */
public interface EG_Statement {
	static EG_Statement of(String aText, EX_Explanation aEXExplanation) {
		return new EG_Statement() {
			@Override
			public String getText() {
				return aText;
			}

			@Override
			public EX_Explanation getExplanation() {
				return aEXExplanation;
			}
		};
	}

	String getText();

	EX_Explanation getExplanation();
}
