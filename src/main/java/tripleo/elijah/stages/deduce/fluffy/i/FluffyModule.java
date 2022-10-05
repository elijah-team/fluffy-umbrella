package tripleo.elijah.stages.deduce.fluffy.i;

import java.util.List;

public interface FluffyModule {
	FluffyLsp lsp();

	String name();

	List<FluffyMember> members();
}
