package tripleo.elijah.stages.deduce.post_bytecode;

import tripleo.elijah.lang.Context;
import tripleo.elijah.nextgen.query.Operation2;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;

public interface DeduceElement3_Type {
	TypeTableEntry typeTableEntry();

	GenType genType();

	Operation2<GenType> resolved(Context ectx);
}
