package tripleo.elijah.lang.nextgen.names.impl;

import com.google.common.collect.ImmutableList;
import tripleo.elijah.lang.Context;
import tripleo.elijah.lang.LookupResultList;
import tripleo.elijah.lang.nextgen.names.i.EN_Understanding;

import java.util.List;
import java.util.stream.Collectors;

public class ENU_LookupResult implements EN_Understanding {

	private int                    level;
	private LookupResultList       lrl;
	private ImmutableList<Context> contexts;

	public ENU_LookupResult(LookupResultList lrl2) {
		this.lrl   = lrl2;
		this.level = -10000;

		final List<Context> collect = lrl2.results().stream().map(lr -> lr.getContext()).collect(Collectors.toList());
		this.contexts = ImmutableList.copyOf(collect);
	}

	public ENU_LookupResult(LookupResultList aLrl, int aLevel, ImmutableList<Context> aContexts) {
		this.lrl      = aLrl;
		this.level    = aLevel;
		this.contexts = aContexts;
	}

}
