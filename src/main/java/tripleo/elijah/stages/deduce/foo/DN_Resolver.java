package tripleo.elijah.stages.deduce.foo;

public interface DN_Resolver {
	void resolve(DN_ResolverResolution aResolution);

	void reject(DN_ResolverRejection aRejection);
}
