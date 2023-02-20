package tripleo.elijah.lang;

import antlr.Token;
import tripleo.elijah.lang2.ElElementVisitor;

public interface AccessNotation extends OS_Element {
	void setShortHand(Token shorthand);

	void setTypeNames(TypeNameList tnl);

	@Override
	void visitGen(ElElementVisitor visit);

	@Override
	Context getContext();

	@Override
	OS_Element getParent();

	Token getCategory();

	void setCategory(Token category);
}
