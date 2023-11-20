package tripleo.elijah.nextgen.outputstatement;

public interface EX_Explanation {
	static EX_Explanation withMessage(String message) {
		return new EX_Explanation() {
			@Override
			public String getText() {
				return message;
			}
		};
	}

	String getText();
}
