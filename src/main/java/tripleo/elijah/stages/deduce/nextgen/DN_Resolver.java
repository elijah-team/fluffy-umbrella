package tripleo.elijah.stages.deduce.nextgen;

public interface DN_Resolver {
	void reject(DN_ResolverRejection aRejection);

	void resolve(DN_ResolverResolution aResolution);
}
