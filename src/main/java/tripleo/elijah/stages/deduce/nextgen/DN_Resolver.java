package tripleo.elijah.stages.deduce.nextgen;

public interface DN_Resolver {
	void resolve(DN_ResolverResolution aResolution);
	void reject(DN_ResolverRejection aRejection);
}
