package tripleo.elijah.stages.deduce.nextgen;

import tripleo.elijah.stages.gen_fn.BaseTableEntry;
import tripleo.elijah.stages.logging.ElLog;
import tripleo.elijah.util.NotImplementedException;

public class DN_ResolverRejections {
	public static DN_ResolverRejection Code(final int aI) {
		return new DN_ResolverRejection() {
			@Override
			public void print_message(final DN_Resolver aResolver, final BaseTableEntry aBaseTableEntry) {
				throw new NotImplementedException();
			}

			@Override
			protected Object clone() throws CloneNotSupportedException {
				return super.clone();
			}
		};
	}

	public static DN_ResolverRejection PrintingCode(final int code, final String xx, final ElLog LOG) throws RuntimeException {
		return new DN_ResolverRejection() {
			@Override
			public void print_message(final DN_Resolver aResolver, final BaseTableEntry aBaseTableEntry) {
				LOG.info(code + " Can't find element for " + xx);
			}
		};
	}
}
