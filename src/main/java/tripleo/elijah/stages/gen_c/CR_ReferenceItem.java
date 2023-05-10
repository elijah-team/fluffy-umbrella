package tripleo.elijah.stages.gen_c;

import tripleo.elijah.nextgen.outputstatement.EG_Statement;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.instructions.InstructionArgument;

public interface CR_ReferenceItem {
	String getArg();

	void setArg(String aArg);

	GenerateC_Item getGenerateCItem();

	void setGenerateCItem(GenerateC_Item aGenerateCItem);

	InstructionArgument getInstructionArgument();

	void setInstructionArgument(InstructionArgument aInstructionArgument);

	CReference.Eventual<GenerateC_Item> getPrevious();

	void setPrevious(CReference.Eventual<GenerateC_Item> aPrevious);

	String getText();

	void setText(String aText);

	CReference.Connector getConnector();

	void setConnector(CReference.Connector aConnector);

	Operation2<EG_Statement> getStatement();

	void setStatement(Operation2<EG_Statement> aStatement);

	CReference.Reference getReference();

	void setReference(CReference.Reference aReference);
}
