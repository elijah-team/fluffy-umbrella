package tripleo.elijah.stages.deduce;

import org.jdeferred2.DoneCallback;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.OS_Type;
import tripleo.elijah.stages.gen_fn.*;

class Resolve_var_table_entry {
	private final DeduceTypes2 deduceTypes2;

	public Resolve_var_table_entry(DeduceTypes2 aDeduceTypes2) {
		deduceTypes2 = aDeduceTypes2;
	}

	public void act(@NotNull VariableTableEntry vte, BaseGeneratedFunction generatedFunction, Context ctx) {
		if (vte.getResolvedElement() == null)
			return;
		{
			if (vte.type.getAttached() == null && vte.constructable_pte != null) {
				ClassStatement c = vte.constructable_pte.getFunctionInvocation().getClassInvocation().getKlass();
				final @NotNull OS_Type attached = new OS_Type(c);
				// TODO this should have been set somewhere already
				//  typeName and nonGenericTypeName are not set
				//  but at this point probably wont be needed
				vte.type.genType.resolved = attached;
				vte.type.setAttached(attached);
			}
			vte.setStatus(BaseTableEntry.Status.KNOWN, new GenericElementHolder(vte.getResolvedElement()));
			{
				final GenType genType = vte.type.genType;
				if (genType.resolved != null && genType.node == null) {
					deduceTypes2.genCI(genType, genType.nonGenericTypeName);
//					genType.node = makeNode(genType);
					//
					// registerClassInvocation does the job of makeNode, so results should be immediately available
					//
					((ClassInvocation) genType.ci).resolvePromise().then(new DoneCallback<GeneratedClass>() {
						@Override
						public void onDone(GeneratedClass result) {
							genType.node = result;
							if (!vte.typePromise().isResolved()) // HACK
								vte.resolveType(genType);
						}
					});
				}
			}
		}
	}
}
