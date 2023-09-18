package tripleo.elijah.stages.deduce.post_bytecode;

import org.jetbrains.annotations.NotNull;
import tripleo.elijah.lang.Context;
import tripleo.elijah.stages.gen_fn.GenType;
import tripleo.elijah.stages.gen_fn.TypeTableEntry;
import tripleo.elijah.util.Operation2;

/**
 * Also {@link tripleo.elijah.stages.deduce.post_bytecode.DeduceType3}
 */
public interface DeduceElement3_Type {
	GenType genType();

	@NotNull Operation2<GenType> resolved(Context ectx);

	TypeTableEntry typeTableEntry();
}
