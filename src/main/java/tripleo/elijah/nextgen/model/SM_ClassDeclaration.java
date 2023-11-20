package tripleo.elijah.nextgen.model;

public interface SM_ClassDeclaration extends SM_Node {
	SM_Name name();

	SM_ClassSubtype subType();

	SM_ClassInheritance inheritance();

	SM_ClassBody classBody();
}
